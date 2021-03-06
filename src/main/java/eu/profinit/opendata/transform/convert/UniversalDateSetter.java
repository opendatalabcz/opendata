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

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        Date date = getDateFromSourceValue(sourceValues.get("inputDateString"), true);
        try {
            dateSetter.setField(record, date, fieldName, logger);
        } catch (Exception e) {
            throw new TransformException("Date setter can't set a field.", e, TransformException.Severity.PROPERTY_LOCAL);
        }
    }

    public static int getYearFromSourceValue(Cell sourceValue) throws TransformException, DateFormatException {
        Date date = getDateFromSourceValue(sourceValue, false);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static Date getDateFromSourceValue(Cell sourceValue, boolean dayMonthCheck) throws TransformException, DateFormatException {
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
                DateFormat dateFormat = getDateFormat(dateString, dayMonthCheck);
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

    private static DateFormat getDateFormat(String date, boolean dayMonthCheck) throws DateFormatException {
        String delimiter = getDelimiter(date);
        String splitDelimiter = delimiter.equals(".") ? "\\." : delimiter;
        String[] dateSplit = date.split(splitDelimiter);

        return new SimpleDateFormat(getPattern(dateSplit, delimiter, dayMonthCheck));
    }

    public static String getDelimiter(String date) throws DateFormatException {
        List<String> delimiters = new ArrayList<>();
        delimiters.add("\\.");
        delimiters.add("/");
        delimiters.add("-");

        for (int i = 0; i < delimiters.size(); i++) {
            String[] dateSplit = date.split(delimiters.get(i));
            if (dateSplit.length > 2) {
                return delimiters.get(i).replace("\\", "");
            }
        }
        throw new UnknownFormatConversionException("Date format is not supported.");
    }

    private static String getPattern(String[] dateSplit, String delimiter, boolean dayMonthCheck) throws DateFormatException {
        if (dateSplit[0].length() == 4) {
            return "yyyy" + delimiter + "MM" + delimiter + "dd";
        }

        String expectedFormat = "dd" + delimiter + "MM" + delimiter + "yyyy";

        if (dayMonthCheck && Integer.parseInt(dateSplit[1]) > 12) {
            throw new DateFormatException("Expected format '" + expectedFormat + "' is wrong. " +
                    "Change to 'MM" + delimiter + "dd" + delimiter + "yyyy.");
        }
        return expectedFormat;
    }
}
