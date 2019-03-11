package eu.profinit.opendata.transform;

import java.util.Date;

/**
 * Interface imitates a subset of {@link org.apache.poi.ss.usermodel.Cell}.
 */
public interface Cell{

    int CELL_TYPE_NUMERIC = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
    int CELL_TYPE_STRING = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;
    int CELL_TYPE_FORMULA = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA;
    int CELL_TYPE_BLANK = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
    int CELL_TYPE_BOOLEAN = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
    int CELL_TYPE_ERROR = org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR;

    int getCellType();

    String getStringCellValue();

    int getColumnIndex();

    double getNumericCellValue();

    Date getDateCellValue();

    void setCellType(int type);

}
