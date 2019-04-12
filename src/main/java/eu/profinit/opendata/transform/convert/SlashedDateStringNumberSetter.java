package eu.profinit.opendata.transform.convert;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Component
public class SlashedDateStringNumberSetter  extends DateStringNumberSetter {
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    protected DateFormat getDateFormat() {
        return dateFormat;
    }
}
