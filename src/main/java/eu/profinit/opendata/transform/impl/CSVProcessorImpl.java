package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.CSVProcessor;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.MappedSheet;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

/**
 * CSV implementation of a data frame processor.
 * @see CSVParser
 */
@Component
public class CSVProcessorImpl extends DataFrameProcessorImpl implements CSVProcessor {

    // The default value is only used for testing, it's overwritten in doRetrieval
    private Logger log = LogManager.getLogger(CSVProcessorImpl.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = {TransformException.class, RuntimeException.class})
    public void processCSVSheet(InputStream inputStream, InputStream initialStream, Mapping mapping, Retrieval retrieval, Logger logger)
            throws TransformException, IOException {

        if(logger != null) {
            log = logger;
        }

        log.info("Started processing sheet");

        String encoding = getEncoding(retrieval);


        for(MappedSheet mappingSheet : mapping.getMappedSheet()) {
            CSVParser parser = createCsvParser(inputStream, initialStream, mappingSheet, encoding);

            int startRowNum = mappingSheet.getHeaderRow().intValue() + 1;
            if (retrieval.getDataInstance().getLastProcessedRow() != null) {
                startRowNum = retrieval.getDataInstance().getLastProcessedRow() + 1;
            }
            log.info("First data row will be " + startRowNum);

            // Create the column name mapping - if two or more columns share the same name, a numeric suffi
            // is appended going from left to right. First occurrence gets no suffix, second gets 01, etc.
            log.trace("Mapping column names to their indexes");

            Map<String, Integer> headerMap = parser.getHeaderMap();

            for (CSVRecord row: parser) {
                int i = (int)row.getRecordNumber();
                log.debug("Processing row " + i);
                try {
                    Optional<Record> optRow = processRow(
                            new CSVRowImpl(row), mappingSheet, mapping.getPropertySet(), retrieval, headerMap);

                    if (!optRow.isPresent()) {
                        log.warn("Encountered empty row at index " + i + ", skipping");
                        continue;
                    }

                    Record record = optRow.get();

                    //A call to persist will throw a PersistenceException if all required attributes aren't filled
                    //Which means the whole transaction will blow up. We need to check manually
                    checkRecordIntegrity(record);

                    log.debug("Record finished, persisting");
                    persistAndUpdateRetrieval(retrieval, i, record);
                } catch (TransformException ex) {
                    if (ex.getSeverity().equals(TransformException.Severity.FATAL)) {
                        throw ex;
                    } else {
                        //Property local exceptions should get caught deeper down, these are record local
                        //In case we are updating an old record, we should be fine, since we don't save or merge the record
                        //The bad row will still count as a bad record though!
                        log.warn("A record-local exception occurred, skipping row " + i + " as bad record", ex);
                        retrieval.setNumBadRecords(retrieval.getNumBadRecords() + 1);
                    }
                }
            }
        }
        log.info("Sheet finished");
    }

    private String getEncoding(Retrieval retrieval) {
        String encoding = retrieval.getDataInstance().getEncoding();
        return (encoding == null) ? "UTF-8" : encoding;
    }


    /**
     * Creates a CSV parser. It determines which delimiter is used in the file and returns such parser.
     * So far, semicolon, comma and tabulator are supported, however any other delimiter can be easily added.
     * @param inputStream input stream of the downloading file
     * @param mappingSheet current mapping sheet
     * @return CSVParser CSV parser with correct delimiter
     */
    private CSVParser createCsvParser(InputStream inputStream, InputStream initialStream, MappedSheet mappingSheet, String encoding) throws IOException {
        CSVParser csvParser = null;
        List<Character> delimiters = new ArrayList<>();
        delimiters.add(';');
        delimiters.add(',');
        delimiters.add('\t');

        try {
            csvParser = getCsvParser(inputStream, delimiters.get(0), mappingSheet.getHeaderRow().intValue(), encoding);
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            int mappedProperties = mappingSheet.getPropertyOrPropertySet().size();
            int headerProperties = headerMap.keySet().size();

            if (headerProperties < 2) {
                for(int i = 1; i < delimiters.size(); i++) {
                    String headerLine = headerMap.keySet().iterator().next();
                    if (isSeparatedWithDelimiter(headerLine, delimiters.get(i), mappedProperties)) {
                        csvParser = getCsvParser(initialStream, delimiters.get(i), mappingSheet.getHeaderRow().intValue(), encoding);
                        break;
                    }
                }
            }
        } catch(Exception e) {
            log.error("Unable to create CSVParser. " + e.getMessage());
            throw e;
        }
        return csvParser;
    }

    private CSVParser getCsvParser(InputStream fileStream, Character delimiter, Integer headerRow, String encoding) throws IOException {
        Reader reader = getReader(fileStream, headerRow, encoding);
        return new CSVParser(reader, CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withQuote('\"'));
    }

    private Reader getReader(InputStream fileStream, Integer headerRow, String encoding) throws IOException {
        Reader reader = new InputStreamReader(fileStream, encoding);
        if (headerRow == 0) {
            return reader;
        }
        for (int i = 0; i < headerRow; i++){
            int ch = reader.read();
            while(!isEOL(ch)) {
                ch = reader.read();
            }
        }
        return reader;
    }

    private boolean isEOL(int ch) {
        return ch == '\n'; // watch out - mac os not handled (that is, CR char)
    }

    /**
     * Determines whether the header is formatted with the provided delimiter. Note, that the method is
     * not bullet proof, a header containing f.e. as many commas as is the number of mapped properties
     * would return true although the delimiter could be different. The method expects reasonable input
     * as is the case of all the contract/order/invoice files so far.
     * @param header - header line of a file
     * @param delimiter - delimiter to be checked
     * @param mappedPropertiesSize - number of mapped properties in a mapping file
     * @return
     */
    private boolean isSeparatedWithDelimiter(String header, Character delimiter, int mappedPropertiesSize) {
        String[] delimitedColumns = header.split(String.valueOf(delimiter));
        int columnsSize = delimitedColumns.length;
        return columnsSize >= mappedPropertiesSize - 3;
    }

}
