package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

/**
 * Sets a date field specified by the fieldName. Expects a date cell with
 * argumentName "inputDate".
 */
@Component
public class DateSetter implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        try {
            Date inputDate = null;
            Cell cellInputDate = sourceValues.get("inputDate");
            if (cellInputDate != null) {
                try {
                    inputDate = cellInputDate.getDateCellValue();
                } catch (IllegalStateException ex) {
                    throw new TransformException("Couldn't set date property - bad cell format", ex,
                            TransformException.Severity.PROPERTY_LOCAL);
                }
            }
            if (inputDate == null) {
                inputDate = new java.sql.Date(0L);
            }
            setField(record, inputDate, fieldName, logger);
        } catch (Exception e) {
            String message = "Couldn't set java.sql.Date value for field " + fieldName;
            throw new TransformException(message, e, TransformException.Severity.PROPERTY_LOCAL);
        }

    }

    public void setField(Record record, Date date, String fieldName, Logger logger) throws Exception {
        Field field = Record.class.getDeclaredField(fieldName);
        Class<?> fieldType = field.getType();
        if (!fieldType.isAssignableFrom(java.sql.Date.class)) {
            throw new TransformException("Field " + fieldName + " doesn't have type java.sql.Date", TransformException.Severity.FATAL);
        }
        field.setAccessible(true);

        if (date == null) {
            logger.trace("Couldn't set Date - input String is null");
            return;
        }
        java.sql.Date dateToSet = new java.sql.Date(date.getTime());

        field.set(record, dateToSet);
    }
}
