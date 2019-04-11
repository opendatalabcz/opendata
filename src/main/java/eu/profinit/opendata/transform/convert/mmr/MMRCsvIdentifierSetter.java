package eu.profinit.opendata.transform.convert.mmr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MMRCsvIdentifierSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        record.setAuthorityIdentifier(getIdentifierFromSourceValues(sourceValues));
    }

    private String getIdentifierFromSourceValues(Map<String, Cell> sourceValues) {
        StringBuilder identifier = new StringBuilder();

        Double year = sourceValues.get("invoiceYear").getNumericCellValue();
        String categoryType = sourceValues.get("categoryType").getStringCellValue();
        Double recordNumber = sourceValues.get("recordNumber").getNumericCellValue();
        Double itemNumber = sourceValues.get("itemNumber").getNumericCellValue();

        identifier.append(year.intValue());
        identifier.append("-");
        identifier.append(categoryType);
        identifier.append("-");
        identifier.append(recordNumber.intValue());
        identifier.append("-");
        identifier.append(itemNumber.intValue());

        return identifier.toString();
    }
}
