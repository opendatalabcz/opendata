package eu.profinit.opendata.institution.mmr.czt.impl;

import eu.profinit.opendata.institution.mmr.czt.CZTHandler;
import eu.profinit.opendata.institution.rest.kanapi.KANAPIHandler;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CZTHandlerImpl extends KANAPIHandler implements CZTHandler {
    @Value("${mmr.json.api.url}")
    private String jsonApiUrl;

    @Value("${mmr.json.packages.url}")
    private String packagesPath;

    @Value("${mmr.czt.json.invoices.identifier}")
    private String invoicesIdentifier;

    @Value("${mmr.czt.mapping.invoices}")
    private String invoicesMappingFile;

    private final Logger log = LogManager.getLogger(CZTHandler.class);

    private final String patternRegex = "^CzT_faktury_(?<year>\\d{4})\\.csv$";

    @Override
    protected String getMappingFile(String name) {
        return invoicesMappingFile;
    }

    @Override
    protected String getInstanceName(String name) {
        String[] splitName = name.split("_");
        String year = splitName[2].substring(0,4);

        return "Faktury MMR CZT " + year;
    }

    @Override
    public void updateDataInstances(DataSource ds) {
        switch (ds.getRecordType()) {
            case INVOICE:
                updateInvoicesDataInstance(ds, patternRegex, Periodicity.MONTHLY,
                        jsonApiUrl, packagesPath, invoicesIdentifier, invoicesMappingFile, log);
                break;
            default:
                throw new UnsupportedOperationException("Other type for update than INVOICE is not supported yet. " +
                        "Requested type: " + ds.getRecordType());

        }
    }
}
