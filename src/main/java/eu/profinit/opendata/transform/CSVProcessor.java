package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.commons.csv.CSVParser;
import org.apache.logging.log4j.Logger;

/**
 * The component responsible for the processing of a CSV file.
 * Extends common data frame processor.
 */
public interface CSVProcessor extends DataFrameProcessor {

    /**
     * Processes a CSV file. Reads the loaded mapping and reads rows of the file. For
     * each row, it instantiates and calls components (filters, retrievers and converters) in the order defined by the
     * mapping. At the end of this process, a fully former Record corresponding to the row values is either saved or
     * updated. The processing of each sheet ends when the last row is reached. Empty rows are ignored.
     * @param parser The Apache Commons CSV parser object to be processed.
     * @param mapping The mapping to be used.
     * @param retrieval The current retrieval metadata object.
     * @param log The transform logger
     * @throws TransformException Only FATAL exceptions will be thrown by this method.
     */
    void processCSVSheet(CSVParser parser, Mapping mapping, Retrieval retrieval, Logger log) throws TransformException;

}
