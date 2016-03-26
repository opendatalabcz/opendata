package eu.profinit.opendata.institution.mocr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mocr.MOCRHandler;
import eu.profinit.opendata.institution.mfcr.rest.JSONClient;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageList;
import eu.profinit.opendata.institution.mfcr.rest.JSONPackageListResource;
import eu.profinit.opendata.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of the MO handler. Uses the ministry's REST API to keep track
 * of DataInstances. Nothing needs to be done manually for this ministry, unless
 * file formats change.
 */
@Component
public class MOCRHandlerImpl extends GenericDataSourceHandler implements MOCRHandler {

    @Autowired
    private JSONClient jsonClient;

    @Autowired
    private DownloadService downloadService;

    @Value("${mocr.json.invoices.identifier}")
    private String invoices_identifier;

    @Value("${mocr.json.contracts.identifier}")
    private String contracts_identifier;

    @Value("${mocr.mapping.invoices}")
    private String invoices_mapping_file;

    @Value("${mocr.mapping.contracts}")
    private String contracts_mapping_file;

    private final Logger log = LogManager.getLogger(MOCRHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch (ds.getRecordType()) {
            case INVOICE:
                updateInvoicesDataInstance(ds);
                break;
            case CONTRACT:
                updateOrdersOrContractsDataInstance(ds, contracts_identifier, contracts_mapping_file);
                break;
            default:
                break;
        }
    }

    /**
     * Updates the data intances associated with the MO orders or contracts data
     * source. Assumes that the JSON API only returns a single xls(x) resource.
     * If the URL changes, a new DataInstance is created and old ones are
     * expired.
     *
     * @param ds The MO orders or contracts DataSource
     * @param identifier The JSON API package identifier (for orders or
     * contracts)
     * @param mappingFile The path to the mapping file that should be used for
     * newly created DataInstances
     */
    public void updateOrdersOrContractsDataInstance(DataSource ds, String identifier, String mappingFile) {
        log.info("Updating information about data instances containing orders");

        //Load list of resources from the JSON API
        JSONPackageList packageList = jsonClient.getPackageList(identifier);
        if (packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for (JSONPackageListResource resource : resourceList) {

            //Check if we found an xls resource
            if (resource.getFormat().equals("xls") || resource.getFormat().equals("xlsx")) {

                //Ignore if there is a data instance with the same URL
                String newUrl = resource.getUrl();
                log.debug("Received metadata for xls(x) resource at " + newUrl);

                if (currentInstances.stream()
                        .filter(i -> i.getUrl().toLowerCase().equals(newUrl.toLowerCase())).count() == 0) {

                    //All current data instances must be marked as expired
                    for (DataInstance i : currentInstances) {
                        log.debug("Marking existing DataInstance " + i.getDataInstanceId() + " as expired.");
                        i.expire();
                        em.merge(i);
                    }

                    //Recreate a new active data instance
                    DataInstance di = new DataInstance();
                    di.setDataSource(ds);
                    di.setFormat(resource.getFormat());
                    di.setPeriodicity(Periodicity.MONTHLY);
                    di.setUrl(resource.getUrl());
                    di.setDescription(resource.getName());
                    di.setMappingFile(mappingFile);

                    if (identifier.equals(contracts_identifier)) {
                        di.setIncremental(false);
                    }

                    ds.getDataInstances().add(di);
                    log.trace("Persisting new DataInstance");
                    em.persist(di);

                } else {
                    log.debug("Resource with given URL already exists as a DataInstance, nothing to do.");
                }
            }
        }
    }

    /**
     * Updates the invoice DataInstances from the JSON API. There should be one
     * for every year since 2010, with files from 2015 onwards having a
     * different format (and a different mapping file). Also finds the partner
     * list used to extract entities for the older format and initiates its
     * processing. Old files that have been updated and processed after their
     * year has ended are expired.
     *
     * @param ds The MF invoices DataSources
     */
    public void updateInvoicesDataInstance(DataSource ds) {
        log.info("Updating information about data instances containing invoices");

        JSONPackageList packageList = jsonClient.getPackageList(invoices_identifier);
        if (packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for (JSONPackageListResource resource : resourceList) {

            // Check if we found an xls resource
            if (resource.getFormat().equals("xls") || resource.getFormat().equals("xlsx")) {
                try {
                    // Check for "uhrazene faktury" and "za rok {YYYY}" and not "privatizace"
                    String name = resource.getName();

                    if (name.contains("Seznam partnerů")) {
                        processPartnerListDataInstance(ds, resource);
                    }

                    Pattern pattern = Pattern.compile("^Uhrazené faktury(?: MO)? za rok (?<year>\\d{4})(?: včetně položky rozpočtu)?$");
                    Matcher matcher = pattern.matcher(name);
                    if (!matcher.find()) {
                        continue;
                    }

                    Integer year = Integer.parseInt(matcher.group("year"));
                    DataInstance dataInstance = new DataInstance();

                    // Check if we already have a data instance with the same given id - if yes, simply update the URL
                    // If not, create a new one
                    Optional<DataInstance> sameIds = currentInstances.stream()
                            .filter(i -> i.getAuthorityId().equals(resource.getId())).findFirst();
                    if (sameIds.isPresent()) {
                        dataInstance = sameIds.get();
                        dataInstance.setUrl(resource.getUrl());
                    } else {

                        dataInstance.setMappingFile(invoices_mapping_file);

                        dataInstance.setDataSource(ds);
                        dataInstance.setUrl(resource.getUrl());
                        dataInstance.setAuthorityId(resource.getId());
                        dataInstance.setFormat(resource.getFormat());
                        dataInstance.setDescription(resource.getName());
                        dataInstance.setPeriodicity(Periodicity.MONTHLY);
                        ds.getDataInstances().add(dataInstance);
                        em.persist(dataInstance);
                    }

                    // Get the year the data instance is holding data from - if in the past and has already been processed
                    // after its last modification that occurred after the year's end, expire it
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                    java.util.Date lastModifiedDate = dateFormat.parse(resource.getLast_modified());
                    Timestamp lmd = new Timestamp(lastModifiedDate.getTime());
                    Integer currentYear = new GregorianCalendar().get(Calendar.YEAR);
                    Timestamp lpd = dataInstance.getLastProcessedDate();
                    Timestamp firstJanuary = new Timestamp(
                            new GregorianCalendar(currentYear, Calendar.JANUARY, 1).getTimeInMillis());
                    if (currentYear > year && lpd != null && lpd.after(lmd) && lmd.after(firstJanuary)) {
                        dataInstance.expire();
                    }

                    em.merge(dataInstance);
                } catch (ParseException ex) {
                    java.util.logging.Logger.getLogger(MOCRHandlerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * Downloads the MF list of partners. A new DataInstance is created for it
     * but is set to APERIODIC to make sure it's never processed by other parts
     * of the application. Once downloaded, the file is passed to a
     * PartnerListProcessor for Entity extraction.
     *
     * @param ds The MF invoices DataSource
     * @param resource The JSON API resource pointing to the MF list of
     * partners.
     * @see PartnerListProcessor
     */
    public void processPartnerListDataInstance(DataSource ds, JSONPackageListResource resource) {
        log.info("Will download and process list of partners. This will take a few minutes.");
        Optional<DataInstance> oldPartnerInstance = ds.getDataInstances().stream()
                .filter(i -> i.getDescription().contains("Seznam partnerů")).findFirst();

        DataInstance toProcess = new DataInstance();
        if (oldPartnerInstance.isPresent()) {
            toProcess = oldPartnerInstance.get();
            toProcess.setUrl(resource.getUrl());
            em.merge(toProcess);
        } else {
            toProcess.setDataSource(ds);
            toProcess.setUrl(resource.getUrl());
            toProcess.setFormat("xlsx");
            toProcess.setPeriodicity(Periodicity.APERIODIC);
            toProcess.setDescription("Seznam partnerů MOČR");
            ds.getDataInstances().add(toProcess);
            em.persist(toProcess);
        }

        Timestamp lpd = toProcess.getLastProcessedDate();
        if (lpd == null || Util.hasEnoughTimeElapsed(lpd, Duration.ofDays(30))) {
            try {
                InputStream is = downloadService.downloadDataFile(toProcess.getUrl());
            } catch (IOException e) {
                log.error("Couldn't download or process list of partners", e);
            }
        }
        toProcess.setLastProcessedDate(Timestamp.from(Instant.now()));
        log.info("List of partners has been processed");
        em.merge(toProcess);
    }

}