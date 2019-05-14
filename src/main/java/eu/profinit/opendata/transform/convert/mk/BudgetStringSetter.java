package eu.profinit.opendata.transform.convert.mk;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BudgetStringSetter  implements RecordPropertyConverter {
    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger) throws TransformException {
        record.setSubject(getSubjectFromSourceValues(sourceValues));
    }

    public String getSubjectFromSourceValues(Map<String, Cell> sourceValues) {
        String departmentOld = getStringValue(sourceValues, "departmentOld");
        Double departmentNumber = getNumericValue(sourceValues, "departmentNumber");
        String departmentString = getStringValue(sourceValues, "departmentString");
        Double inputItemNumber = getNumericValue(sourceValues, "inputItemNumber");
        String inputItemString = getStringValue(sourceValues, "inputItemString");

        if (departmentOld != null && !departmentOld.isEmpty()) {
            return getOldSubject(departmentOld);
        } else {
            return getNewSubject(departmentNumber, departmentString, inputItemNumber, inputItemString);
        }
    }

    private String getStringValue(Map<String, Cell> sourceValues, String key) {
        Cell cell = sourceValues.get(key);
        if (cell != null && !cell.isCellNull()) {
            return cell.getStringCellValue();
        }
        return null;
    }

    private Double getNumericValue(Map<String, Cell> sourceValues, String key) {
        Cell cell = sourceValues.get(key);
        if (cell != null && !cell.isCellNull() && !cell.getStringCellValue().isEmpty()) {
            return cell.getNumericCellValue();
        }
        return null;
    }

    private String getOldSubject(String department) {
        return "Středisko - " + department;
    }

    private String getNewSubject(Double departmentNumber, String departmentString, Double inputItemNumber, String inputItemString) {
        StringBuilder subject = new StringBuilder();

        if (departmentNumber != null) {
            subject.append(departmentNumber.intValue());
        }
        if (!subject.toString().isEmpty() && departmentString != null) {
            subject.append(" - ");
        }
        if (departmentString != null) {
            subject.append(departmentString);
        }
        if (!subject.toString().isEmpty() && inputItemNumber != null) {
            subject.append(" / ");
        }
        if(inputItemNumber != null) {
            subject.append(inputItemNumber.intValue());
        }
        if (!subject.toString().isEmpty() & inputItemString != null) {
            subject.append(" - ");
        }
        subject.append(inputItemString);

        return subject.toString();
    }
}
