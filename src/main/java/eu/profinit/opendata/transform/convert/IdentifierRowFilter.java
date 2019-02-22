package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.SourceRowFilter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Filter disqualifies rows not containing a valid record.
 * Not valid record here means empty identifier field, specified by sourceFileColumn element in the mapping XML.
 */
@Component
public class IdentifierRowFilter implements SourceRowFilter {

    @Override
    public boolean proceedWithRow(Retrieval currentRetrieval, Map<String, Cell> sourceValues) throws TransformException {
        if (sourceValues.containsValue(null)) {
            return false;
        }
        return true;
    }

}
