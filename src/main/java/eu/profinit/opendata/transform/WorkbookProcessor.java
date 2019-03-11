package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * The component responsible for the processing of a Workbook.
 * Extends common data frame processor.
 */
public interface WorkbookProcessor extends DataFrameProcessor {

    /**
     * Processes a Workbook. Reads the loaded mapping and for each sheet that it defines, reads rows of the sheet. For
     * each row, it instantiates and calls components (filters, retrievers and converters) in the order defined by the
     * mapping. At the end of this process, a fully former Record corresponding to the row values is either saved or
     * updated. The processing of each sheet ends when the last row is reached. Empty rows are ignored.
     * @param workbook The Apache POI Workbook object to be processed.
     * @param mapping The mapping to be used.
     * @param retrieval The current retrieval metadata object.
     * @param log The transform logger
     * @throws TransformException Only FATAL exceptions will be thrown by this method.
     */
    void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval, Logger log) throws TransformException;

}
