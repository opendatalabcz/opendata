package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.transform.Cell;

import java.util.Date;

/**
 * Implementation of a cell for CSV files.
 *
 * Inspired by {@link org.apache.poi.xssf.usermodel.XSSFCell}
 */
public class CSVCellImpl implements Cell {

    private String value;

    private int colIdx;

    public CSVCellImpl(String value, int colIdx) {
        this.value = value;
        this.colIdx = colIdx;
    }

    /**
     * @return CSV is always string type.
     */
    @Override
    public int getCellType() {
        return CELL_TYPE_STRING;
    }

    @Override
    public String getStringCellValue() {
        return value;
    }

    @Override
    public int getColumnIndex() {
        return colIdx;
    }

    /**
     * Parses numeric value from the string representation.
     * @return numeric representation of value
     */
    @Override
    public double getNumericCellValue() {
        return Double.valueOf(value);
    }

    /**
     * This is never used, so not implemented.
     * @throws UnsupportedOperationException
     */
    @Override
    public Date getDateCellValue() {
        throw new UnsupportedOperationException();
    }

    /**
     * Type is always string, does nothing.
     * @param type expects only string type
     * @throws IllegalArgumentException
     */
    @Override
    public void setCellType(int type) {
        if (type != this.CELL_TYPE_STRING) {
            throw new IllegalArgumentException();
        }
    }

    public boolean isCellNull() {return value == null;}
}
