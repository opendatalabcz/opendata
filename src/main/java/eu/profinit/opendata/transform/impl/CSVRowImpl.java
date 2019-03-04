package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.Row;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

/**
 * Implementation of a row for CSV files.
 *
 * Inspired by {@link org.apache.poi.xssf.usermodel.XSSFRow}
 */
public class CSVRowImpl implements Row, Iterable<Cell> {

    private CSVRecord record;

    public CSVRowImpl(CSVRecord record) {
        this.record = record;
    }

    /**
     * First cell num is always zero.
     * @return 0
     */
    @Override
    public int getFirstCellNum() {
        return 0;
    }

    /**
     * Returns number of values in row.
     * @return Size of row
     */
    @Override
    public int getLastCellNum() {
        return record.size();
    }

    /**
     * Rerturns cell by index.
     * @param cellNum
     * @return
     * @see CSVCellImpl
     */
    @Override
    public Cell getCell(int cellNum) {
        return new CSVCellImpl(record.get(cellNum), cellNum);
    }

    /**
     * Delegates the iteration to the inner implementation (CSVRecord) as a proxy layer.
     * @return
     * @see org.apache.commons.csv.CSVRecord
     */
    @Override
    public Iterator<Cell> cellIterator() {
        return new Iterator<Cell>() {

            private Iterator<String> innerIterator;

            private int idx = 0;

            {
                innerIterator = record.iterator();
            }

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @Override
            public Cell next() {
                return new CSVCellImpl(innerIterator.next(), idx++);
            }
        };
    }

    @Override
    public Iterator<Cell> iterator() {
        return this.cellIterator();
    }
}
