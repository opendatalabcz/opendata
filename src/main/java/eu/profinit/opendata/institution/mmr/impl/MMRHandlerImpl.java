package eu.profinit.opendata.institution.mmr.impl;

import eu.profinit.opendata.institution.mmr.MMRHandler;
import eu.profinit.opendata.institution.rest.JSONClient;
import eu.profinit.opendata.institution.rest.kanapi.KANAPIHandler;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MMRHandlerImpl extends KANAPIHandler implements MMRHandler {

    @Value("${mmr.json.api.url}")
    private String jsonApiUrl;

    @Value("${mmr.json.packages.url}")
    private String packagesPath;

    @Value("${mmr.json.invoices.identifier}")
    private String invoicesIdentifier;

    @Value("${mmr.mapping.invoices}")
    private String invoicesMappingFile;

    @Value("${mmr.mapping.csv.invoices}")
    private String csvInvoicesMappingFile;

    @Value("${mmr.mapping.csv.2015.invoices}")
    private String csv2015InvoicesMappingFile;

    private final Logger log = LogManager.getLogger(MMRHandler.class);

    @Autowired
    private JSONClient jsonClient;

    private final String patternRegex = "^Faktury_(?<year>\\d{4})\\.csv$";

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

    @Override
    protected String getMappingFile(String name) {
        String[] splitName = name.split("\\.");
        String format = splitName[1];
        String year = splitName[0].substring(splitName[0].length() - 4);
        String mappingFile;

        if(format.equalsIgnoreCase("csv")) {
            if (year.equals("2015")) {
                mappingFile = csv2015InvoicesMappingFile;
            } else {
                mappingFile = csvInvoicesMappingFile;
            }
        } else {
            mappingFile = invoicesMappingFile;
        }
        return mappingFile;
    }

    @Override
    protected String getInstanceName(String name) {
        String[] splitName = name.split("_");
        String recordName = splitName[0];
        String year = splitName[1].substring(0,4);

        StringBuilder instanceName = new StringBuilder();
        instanceName.append(recordName);
        instanceName.append(" MMR ");
        instanceName.append(year);

        return instanceName.toString();
    }
}
