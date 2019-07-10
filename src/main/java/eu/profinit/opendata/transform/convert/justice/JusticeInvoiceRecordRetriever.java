package eu.profinit.opendata.transform.convert.justice;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.DateFormatException;
import eu.profinit.opendata.transform.convert.PropertyBasedRecordRetriever;
import eu.profinit.opendata.transform.convert.SplitIdentifierSetter;
import eu.profinit.opendata.transform.convert.TripleSplitIdentifierSetter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieves MSp in voices based on their authorityIdentifier, which is composed from two source cells.
 * @see SplitIdentifierSetter
 * @see PropertyBasedRecordRetriever
 */
@Component
public class JusticeInvoiceRecordRetriever implements RecordRetriever {

    @Autowired
    private PropertyBasedRecordRetriever propertyBasedRecordRetriever;

    @Autowired
    private TripleSplitIdentifierSetter tripleSplitIdentifierSetter;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException, DateFormatException {

        String identifier = tripleSplitIdentifierSetter.getIdentifierFromSourceValues(sourceValues);
        Map<String, String> stringFilters = new HashMap<>();
        stringFilters.put("authorityIdentifier", identifier);
        return propertyBasedRecordRetriever.retrieveRecordByStrings(currentRetrieval, stringFilters, RecordType.INVOICE);
    }
}
