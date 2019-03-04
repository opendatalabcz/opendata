package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.Row;

import java.util.Iterator;

/**
 * Implementation of a row for Excel files.
 *
 * Delegates to a {@link org.apache.poi.ss.usermodel.Row}
 */
public class WorkbookRowImpl implements Row, Iterable<Cell> {

    private org.apache.poi.ss.usermodel.Row row;

    public WorkbookRowImpl(org.apache.poi.ss.usermodel.Row row) {
        this.row = row;
    }

    @Override
    public int getFirstCellNum() {
        return row.getFirstCellNum();
    }

    @Override
    public int getLastCellNum() {
        return row.getLastCellNum();
    }

    @Override
    public Cell getCell(int cellNum) {
        return new WorkbookCellImpl(row.getCell(cellNum));
    }

    @Override
    public Iterator<Cell> cellIterator() {
        return new Iterator<Cell>() {

            private Iterator<org.apache.poi.ss.usermodel.Cell> innerIterator;

            {
                innerIterator = row.cellIterator();
            }

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @Override
            public Cell next() {
                return new WorkbookCellImpl(innerIterator.next());
            }
        };
    }

    @Override
    public Iterator<Cell> iterator() {
        return this.cellIterator();
    }

}
