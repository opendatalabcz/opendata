package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Row;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.WorkbookProcessor;
import eu.profinit.opendata.transform.convert.DateFormatException;
import eu.profinit.opendata.transform.jaxb.MappedSheet;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Workbook implementation of a data frame processor.
 * @see Workbook
 */
@Component
public class WorkbookProcessorImpl extends DataFrameProcessorImpl implements WorkbookProcessor {

    // The default value is only used for testing, it's overwritten in doRetrieval
    protected Logger log = LogManager.getLogger(WorkbookProcessorImpl.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = {TransformException.class, RuntimeException.class})
    public void processWorkbook(Workbook workbook, Mapping mapping, Retrieval retrieval, Logger logger)
            throws TransformException {

        if(logger != null) {
            log = logger;
        }

        log.info("Started processing workbook");

        for(MappedSheet mappingSheet : mapping.getMappedSheet()) {

            Sheet sheet = getSheetFromWorkbook(workbook, mappingSheet);

            // Create the column name mapping - if two or more columns share the same name, a numeric suffi
            // is appended going from left to right. First occurrence gets no suffix, second gets 01, etc.
            log.trace("Mapping column names to their indexes");

            Row headerRow =  new WorkbookRowImpl(sheet.getRow(mappingSheet.getHeaderRow().intValue()));

            int startRowNum = mappingSheet.getHeaderRow().intValue() + 1;
            if (retrieval.getDataInstance().getLastProcessedRow() != null) {
                startRowNum = retrieval.getDataInstance().getLastProcessedRow() + 1;
            }
            log.info("First data row will be " + startRowNum);

            for (int i = 1; Util.isRowEmpty(headerRow); i++) {
                headerRow = new WorkbookRowImpl(sheet.getRow(mappingSheet.getHeaderRow().intValue() + i));
                if (retrieval.getDataInstance().getLastProcessedRow() == null) {
                    startRowNum++;
                }
            }

            Map<String, Integer> columnNames = getColumnNamesFromHeaderRow(headerRow);
            if(headerIsBroken(columnNames)) {
                addBrokenHeaderNames(columnNames, new WorkbookRowImpl(sheet.getRow(startRowNum++)));
            }
            try {
                processSheetData(startRowNum, sheet, mappingSheet, mapping, retrieval, columnNames);
            } catch (DateFormatException ex) {
                log.error("Date format exception. ", ex);
            }
            log.info("Sheet finished");
        }
    }

    private void processSheetData(int startRowNum, Sheet sheet, MappedSheet mappingSheet, Mapping mapping, Retrieval retrieval,
                             Map<String, Integer> columnNames) throws TransformException, DateFormatException {

        for (int i = startRowNum; i <= sheet.getLastRowNum(); i++) {
            log.debug("Processing row " + i);
            try {
                Optional<Record> optRow = processRow(
                        new WorkbookRowImpl(sheet.getRow(i)), mappingSheet, mapping.getPropertySet(), retrieval, columnNames);

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

    /**
     * Sometimes the header in a file is broken - some header names are located in the header row set in the mapping file,
     * while some other header names are located in the following row (the reason for this can be that some header cells
     * are merged but some are not - this is obviously a bad formatted file).
     * @param columnNames - the map of column names
     * @return - true if the header is broken indeed
     */
    private boolean headerIsBroken(Map<String, Integer> columnNames) {
        if (columnNames.containsKey("")) {
            // sometimes the first empty cell is still considered as part of the header row and the header row is still ok
            boolean lastIsEmpty = columnNames.get("") == (columnNames.size() - 1);
            return !lastIsEmpty;
        }
        return  false;
    }

    /**
     * It collects additional header names and adds them to the current map of header rows.
     * @param columnNames - the map of column names
     * @param row - the row from which additional header names should be collected
     */
    private void addBrokenHeaderNames(Map<String, Integer> columnNames, Row row) {
        Map<String, Integer> additionalNames = getColumnNamesFromHeaderRow(row);
        columnNames.forEach(additionalNames::putIfAbsent);
        // Broken header names causes additional invalid header names such as an empty name or only numerical names
        Map<String, Integer> m = additionalNames.entrySet().stream()
                .filter(e -> !e.getKey().matches(".*01\\d*.*"))
                .filter(e -> !e.getKey().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        columnNames.clear();
        columnNames.putAll(m);
    }
}
