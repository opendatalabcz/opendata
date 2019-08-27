package eu.profinit.opendata.institution.mmr.crr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.institution.mmr.MMRHandler;
import eu.profinit.opendata.institution.mmr.crr.CRRHandler;
import eu.profinit.opendata.institution.rest.kanapi.KANAPIHandler;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Optional;

@Component
public class CRRHandlerImpl  extends KANAPIHandler implements CRRHandler {

    @Value("${mmr.crr.mapping.invoices}")
    private String invoicesMappingFile;

    @Value("${mmr.crr.json.invoices.identifier}")
    private String invoicesIdentifier;

    @Value("${mmr.json.api.url}")
    private String jsonApiUrl;

    @Value("${mmr.json.packages.url}")
    private String packagesPath;

    @Value("${mmr.crr.invoices.url.scheme}")
    private String urlScheme;

    private final Logger log = LogManager.getLogger(MMRHandler.class);

    private final String patternRegex = "^FA_(?<year>\\d{4})\\.csv$";

    @Override
    protected String getMappingFile(String name) {
        return invoicesMappingFile;
    }

    @Override
    protected String getInstanceName(String name) {
        String[] splitName = name.split("_");
        String recordName = "Faktury";
        String year = splitName[1].substring(0,4);

        StringBuilder instanceName = new StringBuilder();
        instanceName.append(recordName);
        instanceName.append(" MMR CRR ");
        instanceName.append(year);

        return instanceName.toString();
    }

    @Override
    public void updateDataInstances(DataSource ds) {
        switch (ds.getRecordType()) {
            case INVOICE:
                // 2016, 2017, 2018
                loadOlderDatasets(ds);
                // 2019 and hopefully later
                updateInvoicesDataInstance(ds, patternRegex, Periodicity.MONTHLY,
                        jsonApiUrl, packagesPath, invoicesIdentifier, invoicesMappingFile, log);
                break;
            default:
                throw new UnsupportedOperationException("Other type for update than INVOICE is not supported yet. " +
                        "Requested type: " + ds.getRecordType());

        }
    }

    /**
     * Loader of CRR invoice datasets covering years 2016 - 2018.
     * @param ds
     */
    private void loadOlderDatasets(DataSource ds) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (Integer year = 2016; year < 2019; year++) {
            String url = urlScheme.replace("{year}", year.toString());

            Optional<DataInstance> oldDataInstance = ds.getDataInstances().stream().filter(d -> d.getUrl().equals(url))
                    .findAny();

            if(oldDataInstance.isPresent()) {
                if (currentYear - year > 1 && oldDataInstance.get().getLastProcessedDate() != null) {
                    oldDataInstance.get().expire();
                    em.merge(oldDataInstance.get());
                    log.info("Expired CRR invoices data instance for year " + year.toString());
                }
            } else if(Util.isFileAtURL(url)) {
                createDataInstance(year, url, ds, "csv");
            } else {
                log.warn("Can't find a document at the url " + url);
            }


        }
    }

    private void createDataInstance(Integer year, String url, DataSource ds, String format) {
        String file = invoicesMappingFile;
        DataInstance di = new DataInstance();
        di.setDataSource(ds);
        ds.getDataInstances().add(di);

        di.setFormat(format);
        di.setPeriodicity(Periodicity.YEARLY);
        di.setUrl(url);
        if (year == 2016) { // yep, only 2016 file is in windows-1250, others have utf-8 encoding
            di.setEncoding("windows-1250");
        }
        di.setDescription("Faktury MMR CRR " + year.toString());
        di.setMappingFile(file);
        di.setIncremental(false);

        log.debug("Adding new data instance for MRR CRR invoices in " + year.toString());
        em.persist(di);
    }
}
