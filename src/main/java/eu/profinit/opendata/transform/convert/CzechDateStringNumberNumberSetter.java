package eu.profinit.opendata.transform.convert;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Sets a Date property from a string cell containing a date in the Czech format (dd.MM.yyyy). The fieldName attribute
 * is used to specify which field should be set. Expects a string cell with argumentName "inputDateString".
 */
@Component
public class CzechDateStringNumberNumberSetter extends DateStringNumberSetter {

    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    protected DateFormat getDateFormat() {
        return dateFormat;
    }
}
