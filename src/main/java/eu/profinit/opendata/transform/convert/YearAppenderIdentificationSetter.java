package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class YearAppenderIdentificationSetter implements RecordPropertyParameterConverter {

    private String year;

    public YearAppenderIdentificationSetter() {
        this.year = null;
    }

    public YearAppenderIdentificationSetter(String year) {
        this.year = year;
    }

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        record.setAuthorityIdentifier(getAuthorityIdentifierFromSourceValues(sourceValues));
    }

    private String getAuthorityIdentifierFromSourceValues(Map<String, Cell> sourceValues) {
        String categoryCode = sourceValues.get("categoryCode").getStringCellValue();
        Double serialNumber = sourceValues.get("serialNumber").getNumericCellValue();
        String serialNumberString = Integer.toString(serialNumber.intValue());
        return year + "-" + categoryCode + "-" + serialNumberString;
    }

    @Override
    public void setParameter(String parameter) {
        year = parameter;
    }
}
