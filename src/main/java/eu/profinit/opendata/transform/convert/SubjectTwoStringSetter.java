package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import eu.profinit.opendata.transform.Cell;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the "authorityIdentifier" of a Record to "categoryCode - serialNumber", where both are keys in the sourceValues
 * map. The fieldName property is ignored.
 */
@Component
public class SubjectTwoStringSetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        record.setSubject(getSubjectFromSourceValues(sourceValues));
    }

    public String getSubjectFromSourceValues(Map<String, Cell> sourceValues) {
        String subjectNumber = "";
        try {
            subjectNumber = sourceValues.get("inputNumber").getStringCellValue();
        } catch (IllegalStateException ex) {
            Double subjectNr = sourceValues.get("inputNumber").getNumericCellValue();
            subjectNumber = Integer.toString(subjectNr.intValue());
        }
        String description = sourceValues.get("inputString").getStringCellValue();

        return subjectNumber + " - " + description;
    }
}
