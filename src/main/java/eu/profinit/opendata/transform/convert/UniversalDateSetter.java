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
import java.util.*;

@Component
public class UniversalDateSetter implements RecordPropertyConverter {
    @Autowired
    private DateSetter dateSetter;
    private List<String> delimiters;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

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
                DateFormat dateFormat = getDateFormat(dateString);
                date = dateFormat.parse(dateString);
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

    private DateFormat getDateFormat(String date) {
        delimiters = new ArrayList<>();
        delimiters.add("\\.");
        delimiters.add("/");
        delimiters.add("-");

        for (int i = 0; i < delimiters.size(); i++) {
            if (date.split(delimiters.get(i)).length > 2) {
                String delimiter = delimiters.get(i).replace("\\", "");
                return new SimpleDateFormat("dd" + delimiter + "MM" + delimiter + "yyyy");
            }
        }
        throw new UnknownFormatConversionException("Date format is not supported.");
    }
}
