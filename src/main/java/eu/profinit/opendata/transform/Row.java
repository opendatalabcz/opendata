package eu.profinit.opendata.transform;

import java.util.Iterator;

/**
 * Interface imitates a subset of {@link org.apache.poi.ss.usermodel.Row}.
 */
public interface Row {

    int getFirstCellNum();

    int getLastCellNum();

    Cell getCell(int cellNum);

    Iterator<Cell> cellIterator();

}
