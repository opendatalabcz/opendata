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

import java.util.Calendar;

@Component
public class MMRHandlerImpl extends KANAPIHandler implements MMRHandler {

    @Value("${mmr.json.api.url}")
    private String jsonApiUrl;

    @Value("${mmr.json.packages.url}")
    private String packagesPath;

    @Value("${mmr.json.invoices.identifier1}")
    private String invoicesIdentifier1;
    @Value("${mmr.json.invoices.identifier2}")
    private String invoicesIdentifier2;
    @Value("${mmr.json.invoices.identifier3}")
    private String invoicesIdentifier3;

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
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        switch (ds.getRecordType()) {
            case INVOICE:
                for (int year = 2016; year <= currentYear; year++) {

                    String invoicesId = getInvoicesId(invoicesIdentifier1, year);
                    if (jsonClient.checkUrlOK(jsonApiUrl, packagesPath, invoicesId)) {
                        updateInvoicesDataInstance(ds, patternRegex, Periodicity.MONTHLY,
                                jsonApiUrl, packagesPath, invoicesIdentifier1, invoicesMappingFile, log);
                    }

                    invoicesId = getInvoicesId(invoicesIdentifier2, year);
                    if (jsonClient.checkUrlOK(jsonApiUrl, packagesPath, invoicesId)) {
                        updateInvoicesDataInstance(ds, patternRegex, Periodicity.MONTHLY,
                                jsonApiUrl, packagesPath, invoicesIdentifier2, invoicesMappingFile, log);
                    }
                }
                // 2018 file has a different identifier.. -_-
                updateInvoicesDataInstance(ds, patternRegex, Periodicity.MONTHLY,
                        jsonApiUrl, packagesPath, invoicesIdentifier3, invoicesMappingFile, log);
                break;
            default:
                throw new UnsupportedOperationException("Other type for update than INVOICE is not supported yet. " +
                        "Requested type: " + ds.getRecordType());
        }
    }

    private String getInvoicesId(String identifierPattern, Integer year) {
        return identifierPattern.replace("{year}", year.toString());
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
