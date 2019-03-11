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
public class SubjectCategorySetter implements RecordPropertyConverter{
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        record.setSubject(getSubjectFromSourceValues(sourceValues));
    }

    public String getSubjectFromSourceValues(Map<String, Cell> sourceValues) {
        Double subjectNumber = sourceValues.get("inputNumber").getNumericCellValue();
        String description = sourceValues.get("inputString").getStringCellValue();

        String serialNumberString = Integer.toString(subjectNumber.intValue());
        return serialNumberString + " - " + description;
    }
}
