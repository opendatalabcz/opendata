package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class MMddyyyyDateSetter implements RecordPropertyConverter {
    @Autowired
    private DateSetter dateSetter;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {
        Date date = getDateFromSourceValue(sourceValues.get("inputDateString"));
        try {
            dateSetter.setField(record, date, fieldName, logger);
        } catch (Exception e) {
            throw new TransformException("Date setter can't set a field.", e, TransformException.Severity.PROPERTY_LOCAL);
        }
    }

    public Date getDateFromSourceValue(Cell sourceValue) throws TransformException, DateFormatException {
        String dateString = "";
        Date date = null;
        if (sourceValue != null && !sourceValue.isCellNull()) {
            try {
                dateString = sourceValue.getStringCellValue();
            } catch (IllegalStateException ex) {
                date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(sourceValue.getNumericCellValue());
            }
        }
        if(Util.isNullOrEmpty(dateString) && date == null) {
            throw new TransformException("Couldn't set date value, date is null or empty",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        try {
            if (date == null) {
                DateFormat dateFormat = getDateFormat(dateString);
                date = dateFormat.parse(dateString);
            }
            return date;
        } catch (ParseException e) {
            throw new TransformException("Couldn't set date value because of a parse error", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        } catch (DateFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformException("Couldn't set date value because of an unknown error", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }
    }

    private DateFormat getDateFormat(String date) throws DateFormatException {
        String delimiter = UniversalDateSetter.getDelimiter(date);
        String pattern = "MM" + delimiter + "dd" + delimiter + "yyyy";

        return new SimpleDateFormat(pattern);
    }
}
