package eu.profinit.opendata.institution.mdcr;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Optional;

@Component
public abstract class MDCRHandler extends GenericDataSourceHandler {

    protected Logger log = LogManager.getLogger(MDCRHandler.class);

    public abstract void updateDataInstances(DataSource ds);

    public void createDataInstance(Integer year, String url, DataSource ds, String format, String mappingFile, String description) {
        String file = mappingFile;

        DataInstance di = new DataInstance();
        di.setDataSource(ds);
        ds.getDataInstances().add(di);

        di.setFormat(format);
        di.setPeriodicity(Periodicity.YEARLY);
        di.setUrl(url);
        di.setDescription(description + " " + year.toString());
        di.setMappingFile(file);
        di.setIncremental(false);

        log.debug("Adding new data instance for " + description + " in " + year.toString());
        em.persist(di);
    }

    protected void updateDataInstance(DataSource ds, String urlScheme, String mappingFile, String description) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (Integer i = 2015; i <= currentYear; i++) {
            String url = urlScheme.replace("{year}", i.toString());

            Optional<DataInstance> oldDataInstance = ds.getDataInstances().stream().filter(d -> d.getUrl().equals(url))
                    .findAny();

            if(oldDataInstance.isPresent()) {
                // Expire processed data instances where at least two years have elapsed since the file's year
                if(currentYear - i > 1 && oldDataInstance.get().getLastProcessedDate() != null) {
                    oldDataInstance.get().expire();
                    em.merge(oldDataInstance.get());
                    log.info("Expired MSp " + description + " data instance for year " + i.toString());
                }
            }
            else if(Util.isXLSFileAtURL(url)) {
                createDataInstance(i, url, ds, "xlsx", mappingFile, description);
            } else {
                log.warn("Can't find an XLS document at the url " + url);
            }
        }
    }
}
