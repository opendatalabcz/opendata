package eu.profinit.opendata.institution.justice;

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

/**
 * The handler for MSp. Only generates data instances for invoices according to a pre-determined URL scheme. Otherwise,
 * behavior is the same as for any other generic handler.
 */
@Component
public class JusticeHandler extends GenericDataSourceHandler {

    @Value("${justice.invoices.url.scheme}")
    private String urlScheme;

    @Value("${justice.invoices.url.scheme2}")
    private String urlScheme2;

    @Value("${justice.invoices.mapping.file}")
    private String mappingFile;

    @Value("${justice.invoices.old.mapping.file}")
    private String mappingOldFile;

    @Value("${justice.invoices.2015.mapping.file}")
    private String mapping2015File;

    @Value("${justice.invoices.2018.mapping.file}")
    private String mapping2018File;

    private Logger log = LogManager.getLogger(JusticeHandler.class);

    @Override
    public void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case INVOICE: updateInvoicesDataInstance(ds); break;
            case CONTRACT: break;
            default: break;
        }
    }

    /**
     * Tries to create invoice data instances for all years from the present going back to 2009. If the data instances
     * are already present but aren't at least a year old, does nothing. If they are older and have already been
     * processed, they are expired.
     * @param ds The MSp invoice data source
     */
    private void updateInvoicesDataInstance(DataSource ds) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(Integer i = currentYear; i >= 2009; i--) {
            String url = urlScheme.replace("{year}", i.toString());
            String urlx = url + "x"; // xlsx document
            String url2 = urlScheme2.replace("{year}", i.toString());

            Optional<DataInstance> oldDataInstance = ds.getDataInstances().stream().filter(d -> d.getUrl().equals(url))
                    .findAny();

            if(oldDataInstance.isPresent()) {
                // Expire processed data instances where at least two years have elapsed since the file's year
                if(currentYear - i > 1 && oldDataInstance.get().getLastProcessedDate() != null) {
                    oldDataInstance.get().expire();
                    em.merge(oldDataInstance.get());
                    log.info("Expired MSp invoices data instance for year " + i.toString());
                }
            }
            else if(Util.isXLSFileAtURL(url)) {
                createDataInstance(i, url, ds);
            }
            // try xlsx document
            else if (Util.isXLSFileAtURL(urlx)){
                createDataInstance(i, urlx, ds);
            // try scheme2
            } else if (Util.isXLSFileAtURL(url2)){
                createDataInstance(i, url2, ds);
            } else {
                log.warn("Can't find an XLS document at the url " + url);
            }
        }
    }

    private void createDataInstance(Integer year, String url, DataSource ds) {
        String file = mappingFile;
        if (year == 2018) {
            file = mapping2018File;
        } else if (year == 2015) {
            file = mapping2015File;
        } else if (year < 2015) {
            file = mappingOldFile;
        }
        DataInstance di = new DataInstance();
        di.setDataSource(ds);
        ds.getDataInstances().add(di);

        di.setFormat("xls");
        di.setPeriodicity(Periodicity.QUARTERLY);
        di.setUrl(url);
        di.setDescription("Faktury MSp za rok " + year.toString());
        di.setMappingFile(file);
        di.setIncremental(false);

        log.debug("Adding new data instance for MSp invoices in " + year.toString());
        em.persist(di);
    }
}
