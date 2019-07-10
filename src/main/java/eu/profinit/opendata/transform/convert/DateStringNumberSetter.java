package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public abstract class DateStringNumberSetter implements RecordPropertyConverter {
    @Autowired
    private DateSetter dateSetter;

    protected abstract DateFormat getDateFormat();

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        String dateString = "";
        Date date = null;
        try {
            dateString = sourceValues.get("inputDateString").getStringCellValue();
        } catch (IllegalStateException ex) {
            date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(sourceValues.get("inputDateString").getNumericCellValue());
        }
        if(Util.isNullOrEmpty(dateString) && date == null) {
            throw new TransformException("Couldn't set date value, date is null or empty",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        try {
            if (date == null) {
                date = getDateFormat().parse(dateString);
            }
            dateSetter.setField(record, date, fieldName, logger);
        } catch (ParseException e) {
            throw new TransformException("Couldn't set date value because of a parse error", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        } catch (Exception e) {
            throw new TransformException("Couldn't set date value because of an unknown error", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }
    }
}
