package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.transform.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.util.Date;

/**
 * Implementation of a cell for Excel files.
 *
 * Delegates to a {@link org.apache.poi.ss.usermodel.Cell}
 */
public class WorkbookCellImpl implements Cell {

    private org.apache.poi.ss.usermodel.Cell cell;

    public WorkbookCellImpl(org.apache.poi.ss.usermodel.Cell cell) {
        this.cell = cell;
    }

    @Override
    public int getCellType() {
        return cell.getCellType().getCode();
    }

    @Override
    public String getStringCellValue() {
        return cell.getStringCellValue();
    }

    @Override
    public int getColumnIndex() {
        return cell.getColumnIndex();
    }

    @Override
    public double getNumericCellValue() {
        return cell.getNumericCellValue();
    }

    @Override
    public Date getDateCellValue() {
        return cell.getDateCellValue();
    }

    @Override
    public void setCellType(int type) {
        cell.setCellType(CellType.forInt(type));
    }

    public boolean isCellNull() { return cell == null;}
}
