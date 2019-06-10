package eu.profinit.opendata.institution.mzcr.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.mzcr.MZCRHandler;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

@Component
public class MZCRHandlerImpl extends GenericDataSourceHandler implements MZCRHandler {

    @Value("${mzcr.invoices.url.scheme}")
    private String urlScheme;

    @Value("${mzcr.invoices.mapping.file}")
    private String mappingFile;

    private Logger log = LogManager.getLogger(MZCRHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case INVOICE:
                updateInvoicesDataInstance(ds);
                break;
            case CONTRACT:
                throw new UnsupportedOperationException("Contracts of MZCR are not yet implemented.");
            default:
                throw new UnsupportedOperationException("Invalid record type " + ds.getRecordType() + ".");
        }
    }

    private void updateInvoicesDataInstance(DataSource ds) {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        DataInstance mostRecentlyAdded = getMostRecentlyAddedDataInstance(ds);

        for(Integer year = 2018; year <= currentYear; year++) {
            for(Integer month = 1; month < 13; month++) {
                if((year == currentYear) && (month == currentMonth)) {
                    break;
                }
                String url = urlScheme
                        .replace("{year}", year.toString())
                        .replace("{month}", String.format("%02d", month));

                Optional<DataInstance> oldDataInstance = ds.getDataInstances().stream().filter(d -> d.getUrl().equals(url))
                        .findAny();


                if(oldDataInstance.isPresent()) {

                    // the most recently added dataset could have not included all records
                    if (mostRecentlyAdded.getUrl().equals(url)) {
                        oldDataInstance.get().expire();
                        em.merge(oldDataInstance.get());
                        log.info("Expired MZ invoices data instance for year " + year.toString() + ", month " + month.toString());
                    }

                    // Expire processed data instances where at least two years have elapsed since the file's year
                    if(currentYear - year > 1 && currentMonth - month > 1 && oldDataInstance.get().getLastProcessedDate() != null) {
                        oldDataInstance.get().expire();
                        em.merge(oldDataInstance.get());
                        log.info("Expired MZ invoices data instance for year " + year.toString() + ", month " + month.toString());
                    }
                } else if(Util.isFileAtURL(url)) {
                    createDataInstance(year, month, url, ds, "csv");
                } else {
                    log.warn("Can't find a document at the url " + url);
                }
            }
        }
    }

    private DataInstance getMostRecentlyAddedDataInstance(DataSource ds) {
        Optional<DataInstance> mzInstances = ds.getDataInstances().stream()
                .filter(d -> d.getDescription().matches("Faktury MZ 2.*"))
                .max(Comparator.comparing(i -> getDescriptionDate(i.getDescription())));
        return mzInstances.get();
    }

    private Date getDescriptionDate(String description) {
        String[] split = description.split("-");
        int year = Integer.parseInt(split[0].substring(split[0].length()-4));
        int month = Integer.parseInt(split[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        return cal.getTime();
    }

    private void createDataInstance(Integer year, Integer month, String url, DataSource ds, String format) {
        String dateIdentification = year.toString() + "-" + String.format("%02d", month);
        String file = mappingFile;

        DataInstance di = new DataInstance();
        di.setDataSource(ds);
        ds.getDataInstances().add(di);

        di.setFormat(format);
        di.setPeriodicity(Periodicity.MONTHLY);
        di.setUrl(url);
        di.setDescription("Faktury MZ " + dateIdentification);
        di.setMappingFile(file);
        di.setIncremental(false);

        log.debug("Adding new data instance for MZ invoices for " + dateIdentification);
        em.persist(di);
    }
}
