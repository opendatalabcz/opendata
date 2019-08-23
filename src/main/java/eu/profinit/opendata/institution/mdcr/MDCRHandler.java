package eu.profinit.opendata.institution.mdcr;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.control.GenericDataSourceHandler;
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
public class MDCRHandler extends GenericDataSourceHandler {

    @Value("${md.invoices.url.scheme}")
    private String invoicesUrlScheme;

    @Value("${md.invoices.mapping.file}")
    private String invoicesMappingFile;

    @Value("${md.contracts.url.scheme}")
    private String contractsUrlScheme;

    @Value("${md.contracts.mapping.file}")
    private String contractsMappingFile;

    private Logger log = LogManager.getLogger(MDCRHandler.class);

    @Override
    protected void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case INVOICE: updateInvoicesDataInstance(ds); break;
            case CONTRACT: updateContractDataInstance(ds); break;
            default: break;
        }
    }

    private void updateInvoicesDataInstance(DataSource ds) {
        updateDataInstance(ds, invoicesUrlScheme, invoicesMappingFile, "Faktury MDČR");
    }

    private void updateContractDataInstance(DataSource ds) {
        updateDataInstance(ds, contractsUrlScheme, contractsMappingFile, "Smlouvy MDČR");
    }

    private void createDataInstance(Integer year, String url, DataSource ds, String format, String mappingFile, String description) {
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

    private void updateDataInstance(DataSource ds, String urlScheme, String mappingFile, String description) {
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
