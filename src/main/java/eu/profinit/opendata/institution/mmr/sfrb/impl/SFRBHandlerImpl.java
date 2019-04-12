package eu.profinit.opendata.institution.mmr.sfrb.impl;

import eu.profinit.opendata.institution.mmr.MMRHandler;
import eu.profinit.opendata.institution.mmr.sfrb.SFRBHandler;
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
public class SFRBHandlerImpl  extends KANAPIHandler implements SFRBHandler {

    @Value("${mmr.json.api.url}")
    private String jsonApiUrl;

    @Value("${mmr.json.packages.url}")
    private String packagesPath;

    @Value("${mmr.sfrb.json.invoices.identifier}")
    private String invoicesIdentifier;

    @Value("${mmr.sfrb.mapping.invoices}")
    private String invoicesMappingFile;

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
        return invoicesMappingFile;
    }

    @Override
    protected String getInstanceName(String name) {
        String[] splitName = name.split("_");
        String recordName = splitName[0];
        String year = splitName[1].substring(0,4);

        StringBuilder instanceName = new StringBuilder();
        instanceName.append(recordName);
        instanceName.append(" MMR SFRB ");
        instanceName.append(year);

        return instanceName.toString();
    }
}
