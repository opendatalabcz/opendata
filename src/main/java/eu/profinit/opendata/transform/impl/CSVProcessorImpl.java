package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.CSVProcessor;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.jaxb.MappedSheet;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

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
    public void processCSVSheet(CSVParser parser, Mapping mapping, Retrieval retrieval, Logger logger)
            throws TransformException {

        if(logger != null) {
            log = logger;
        }

        log.info("Started processing sheet");

        for(MappedSheet mappingSheet : mapping.getMappedSheet()) {

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

}
