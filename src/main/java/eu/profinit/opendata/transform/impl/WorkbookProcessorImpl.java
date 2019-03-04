package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Row;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.WorkbookProcessor;
import eu.profinit.opendata.transform.jaxb.MappedSheet;
import eu.profinit.opendata.transform.jaxb.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

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

            Row headerRow = findHeaderRow(sheet, mappingSheet);

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

            processSheetData(startRowNum, sheet, mappingSheet, mapping, retrieval, columnNames);
            log.info("Sheet finished");
        }
    }

    private void processSheetData(int startRowNum, Sheet sheet, MappedSheet mappingSheet, Mapping mapping, Retrieval retrieval,
                             Map<String, Integer> columnNames) throws TransformException {

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

    private Row findHeaderRow(Sheet sheet, MappedSheet mappingSheet) {
        int headerNumber = mappingSheet.getHeaderRow().intValue();
        Row row = new WorkbookRowImpl(sheet.getRow(headerNumber));
        int i = headerNumber;
        while (row.getCell(1).getCellType() == 3){
            row = new WorkbookRowImpl(sheet.getRow(i++));
        }
        if (i > 0) {
            headerNumber = i - 1;
        }
        mappingSheet.setHeaderRow(BigInteger.valueOf(headerNumber));
        return new WorkbookRowImpl(sheet.getRow(headerNumber));
    }

}
