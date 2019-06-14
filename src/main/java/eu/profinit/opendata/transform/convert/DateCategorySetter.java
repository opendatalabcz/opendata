package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class DateCategorySetter  implements RecordPropertyConverter {

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        record.setAuthorityIdentifier(getIdentifierFromSourceValues(sourceValues, logger));
    }

    public String getIdentifierFromSourceValues(Map<String, Cell> sourceValues, Logger logger) {
        String categoryCode = sourceValues.get("serialNumber").getStringCellValue();
        Double serialNumber = sourceValues.get("date").getNumericCellValue();

        try {
            Date date = UniversalDateSetter.getDateFromSourceValue(sourceValues.get("date"));
            int year = getYearFromDate(date);
            return year + "-" + categoryCode + "-" + serialNumber;
        } catch (TransformException e) {
            logger.error("Exception thrown when a date being retrieved for authority identifier. " +
                    "Setting the authority identifier with category code and serial number only.");
            return categoryCode + "-" + serialNumber;
        }

    }

    private int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
}
