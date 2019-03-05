package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.PropertyBasedRecordRetriever;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieves old MFCR invoices (or payments, but both come from the same document) based on their authorityIdentifier
 * and type. Expects two source cells, "inputType" with value either "Přijaté faktury" or "Ostatní platby" and
 * "authorityIdentifier". The need for a separate retriever is due to the distinction between the two types.
 * @see PropertyBasedRecordRetriever
 */
@Component
public class InvoiceRetriever implements RecordRetriever {

    private static final String AUTHORITY_IDENTIFIER = "authorityIdentifier";
    @Autowired
    private PropertyBasedRecordRetriever propertyBasedRecordRetriever;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException {

        String type = sourceValues.get("inputType").getStringCellValue();
        RecordType recordType = type.equals("Přijaté faktury") ? RecordType.INVOICE : RecordType.PAYMENT;

        HashMap<String, String> filters = new HashMap<>();
        sourceValues.get(AUTHORITY_IDENTIFIER).setCellType(Cell.CELL_TYPE_STRING);
        filters.put(AUTHORITY_IDENTIFIER, sourceValues.get(AUTHORITY_IDENTIFIER).getStringCellValue());

        return propertyBasedRecordRetriever.retrieveRecordByStrings(currentRetrieval, filters, recordType);
    }
}
