package eu.profinit.opendata.transform.convert.mk;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.DateFormatException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TypeSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {
        record.setRecordType(getRecordType(sourceValues));
    }

    public RecordType getRecordType(Map<String, Cell> sourceValues) {
        String type = sourceValues.get("inputType").getStringCellValue();
        type = type.replaceAll("\"$|^\"", "");

        switch (type) {
            case "F": return RecordType.INVOICE;
            case "Z": return RecordType.TRANSFERORDER;
            case "C": return RecordType.DEPOSIT;
            case "DBP": return RecordType.CREDITNOTE;
            default: return RecordType.INVOICE;
        }
    }
}
