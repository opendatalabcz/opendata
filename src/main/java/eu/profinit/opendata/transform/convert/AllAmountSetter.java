package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the originalCurrencyAmount. If the currency is currently set to "CZK",
 * sets the same amount into the amountCzk property as well. Expects a non-null
 * numeric cell with argument "inputAmount" (eventually parses the number from string). Ignores the fieldName argument.
 */
@Component
public class AllAmountSetter implements RecordPropertyConverter {

    private static final String INPUT_AMOUNT = "inputAmount";

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException {

        Double amount = 0d;
        //fix for excels without values
        if (sourceValues.get(INPUT_AMOUNT) != null) {
            if (sourceValues.get(INPUT_AMOUNT).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                amount = sourceValues.get(INPUT_AMOUNT).getNumericCellValue();
            } else if (sourceValues.get(INPUT_AMOUNT).getCellType() == Cell.CELL_TYPE_STRING) {
                String rawVal = sourceValues.get(INPUT_AMOUNT).getStringCellValue();
                String formattedVal = rawVal.trim().replace(" ","").replace(',','.');
                amount = Double.parseDouble(formattedVal);
            }
        }
        record.setOriginalCurrencyAmount(amount);
        if (record.getCurrency().equals("CZK")) {
            record.setAmountCzk(amount);
        }
    }
}
