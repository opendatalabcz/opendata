package eu.profinit.opendata.transform;

import org.apache.poi.ss.usermodel.CellType;

import java.util.Date;

/**
 * Interface imitates a subset of {@link org.apache.poi.ss.usermodel.Cell}.
 */
public interface Cell{

    int CELL_TYPE_NUMERIC = CellType.NUMERIC.getCode();
    int CELL_TYPE_STRING = CellType.STRING.getCode();
    int CELL_TYPE_FORMULA =CellType.FORMULA.getCode();
    int CELL_TYPE_BLANK = CellType.BLANK.getCode();
    int CELL_TYPE_BOOLEAN =CellType.BOOLEAN.getCode();
    int CELL_TYPE_ERROR = CellType.ERROR.getCode();

    int getCellType();

    String getStringCellValue();

    int getColumnIndex();

    double getNumericCellValue();

    Date getDateCellValue();

    void setCellType(int type);

    boolean isCellNull();

}
