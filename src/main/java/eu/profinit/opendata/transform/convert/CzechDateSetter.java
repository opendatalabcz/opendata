package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by dm on 2/27/16.
 */
@Component
public class CzechDateSetter implements RecordPropertyConverter {

    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private DateSetter dateSetter;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        String dateString = sourceValues.get("inputDateString").getStringCellValue();
        if(Util.isNullOrEmpty(dateString)) {
            throw new TransformException("Couldn't set date value, date string is null or empty",
                    TransformException.Severity.PROPERTY_LOCAL);
        }

        try {
            Date date = dateFormat.parse(dateString);
            dateSetter.setField(record, date, fieldName, logger);
        } catch (ParseException e) {
            throw new TransformException("Couldn't set date value because of a parse error", e,
                    TransformException.Severity.PROPERTY_LOCAL);
        }

    }
}
