package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class YearTypeNumberIdentifierSetter implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {
        record.setAuthorityIdentifier(getIdentifierFromSourceValues(sourceValues));
    }

    public String getIdentifierFromSourceValues(Map<String, Cell> sourceValues) {
        StringBuilder identifier = new StringBuilder();
        String date = sourceValues.get("dateCreated").getStringCellValue();
        String type = sourceValues.get("inputType").getStringCellValue();
        String serialNumber = sourceValues.get("record").getStringCellValue();

        String year = getYear(date);

        identifier.append(year);
        identifier.append("-");
        identifier.append(type);
        identifier.append("-");
        identifier.append(serialNumber);

        return identifier.toString();
    }

    private String getYear(String date) {
        return date.substring(date.length() - 4);
    }
}
