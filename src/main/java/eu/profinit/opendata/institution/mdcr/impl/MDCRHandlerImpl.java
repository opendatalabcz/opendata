package eu.profinit.opendata.institution.mdcr.impl;

import eu.profinit.opendata.institution.mdcr.MDCRHandler;
import eu.profinit.opendata.model.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MDCRHandlerImpl extends MDCRHandler {

    @Value("${md.invoices.url.scheme}")
    private String invoicesUrlScheme;

    @Value("${md.invoices.mapping.file}")
    private String invoicesMappingFile;

    @Value("${md.contracts.url.scheme}")
    private String contractsUrlScheme;

    @Value("${md.contracts.mapping.file}")
    private String contractsMappingFile;

    @Override
    public void updateDataInstances(DataSource ds) {
        switch(ds.getRecordType()) {
            case INVOICE:
                updateInvoicesDataInstance(ds);
                break;
            case CONTRACT:
                updateContractDataInstance(ds);
                break;
            default: break;
        }
    }

    private void updateInvoicesDataInstance(DataSource ds) {
        updateDataInstance(ds, invoicesUrlScheme, invoicesMappingFile, "Faktury MDČR");
    }

    private void updateContractDataInstance(DataSource ds) {
        updateDataInstance(ds, contractsUrlScheme, contractsMappingFile, "Smlouvy MDČR");
    }
}
