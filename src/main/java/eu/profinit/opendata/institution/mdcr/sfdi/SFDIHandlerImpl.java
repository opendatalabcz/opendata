package eu.profinit.opendata.institution.mdcr.sfdi;

import eu.profinit.opendata.institution.mdcr.MDCRHandler;
import eu.profinit.opendata.model.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SFDIHandlerImpl extends MDCRHandler {

    @Value("${sfdi.invoices.url.scheme}")
    private String invoicesUrlScheme;

    @Value("${sfdi.invoices.mapping.file}")
    private String invoicesMappingFile;

    @Value("${sfdi.contracts.url.scheme}")
    private String contractsUrlScheme;

    @Value("${sfdi.contracts.mapping.file}")
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
        updateDataInstance(ds, invoicesUrlScheme, invoicesMappingFile, "Faktury SFDI");
    }

    private void updateContractDataInstance(DataSource ds) {
        updateDataInstance(ds, contractsUrlScheme, contractsMappingFile, "Smlouvy SFDI");
    }
}
