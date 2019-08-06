package eu.profinit.opendata.transform.impl;

import eu.profinit.opendata.common.Util;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.CurrentRetrievalExistingRecordException;
import eu.profinit.opendata.transform.*;
import eu.profinit.opendata.transform.convert.DateFormatException;
import eu.profinit.opendata.transform.convert.RecordPropertyParameterConverter;
import eu.profinit.opendata.transform.jaxb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implements data format independent processing logic.
 */
@Component
public abstract class DataFrameProcessorImpl implements DataFrameProcessor {

    @PersistenceContext
    protected EntityManager em;

    @Value("${record.requiredFields}")
    protected String recordRequiredFields;

    @Autowired
    protected ComponentFactory converterFactory;

    @Autowired
    protected IdentifierAppender identifierAppender;

    DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    // The default value is only used for testing, it's overwritten in doRetrieval
    private Logger log = LogManager.getLogger(DataFrameProcessorImpl.class);

    protected void persistAndUpdateRetrieval(Retrieval retrieval, int i, Record record) {
        if (retrieval.equals(record.getRetrieval()) && !retrieval.getRecords().contains(record)) {
            retrieval.getRecords().add(record);
            retrieval.setNumRecordsInserted(retrieval.getNumRecordsInserted() + 1);
        } else if (record.getRecordId() != null) {
            em.merge(record);
        }
        if (retrieval.getDataInstance().isIncremental()) {
            retrieval.getDataInstance().setLastProcessedRow(i);
        }
    }

    protected Map<String, Integer> getColumnNamesFromHeaderRow(Row headerRow) {
        Map<String, Integer> columnNames = new HashMap<>();
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            String columnName = cell.getStringCellValue().trim();
            int i = 1;
            while (columnNames.containsKey(columnName)) {
                columnName += String.format("%02d", i);
                i++;
            }
            columnNames.put(columnName, cell.getColumnIndex());
            log.trace("Name: " + cell.getStringCellValue() + ", Index: " + cell.getColumnIndex());
        }
        return columnNames;
    }

    protected Sheet getSheetFromWorkbook(Workbook workbook, MappedSheet mappingSheet) {
        Sheet sheet = null;
        if(mappingSheet.getName() != null) {
            sheet = workbook.getSheet(mappingSheet.getName());
            log.info("Processing sheet " + mappingSheet.getName());
        }
        else {
            sheet = workbook.getSheetAt(mappingSheet.getNumber().intValue());
            log.info("Processing sheet " + mappingSheet.getNumber());
        }
        return sheet;
    }

    protected Optional<Record> processRow(Row row, MappedSheet mapping, List<PropertySet> propertySets,
                              Retrieval retrieval, Map<String, Integer> columnNames) throws TransformException, DateFormatException {

        if (Util.isRowEmpty(row)) {
            return Optional.empty();
        }

        if(!rowFilterPassed(row, mapping, retrieval, columnNames)) {
            return Optional.empty();
        }

        Optional<Record> record = Optional.of(new Record());
        try {
            record = invokeOldRecordRetriever(row, mapping, retrieval, columnNames);
        } catch (CurrentRetrievalExistingRecordException e) {
            log.info("Appending to authority identifier: " + record.get().getAuthorityIdentifier());
            record.ifPresent(r -> identifierAppender.append(r));
        }

        //Create the Record
        boolean newRecord;
        if(!record.isPresent()) {
            log.debug("Creating new record");
            record = Optional.of(createRecord(retrieval));
            newRecord = true;
        }
        else {
            log.debug("Retriever has returned an old record to update with id " + record.get().getRecordId());
            newRecord = false;
        }

        // For each element in the sheet mapping
        for(Object recordPropertyOrSet : mapping.getPropertyOrPropertySet()) {

            // If the mapping specifies a PropertySet, retrieve it, unpack it and process each Property in turn
            if(recordPropertyOrSet instanceof PropertySetRef) {
                String name = ((PropertySetRef) recordPropertyOrSet).getRef();
                Optional<PropertySet> propertySet = propertySets.stream()
                        .filter(i -> i.getName().equals(name)).findAny();

                if(!propertySet.isPresent()) {
                    throw new TransformException("PropertySet " + name + " is not present in the mapping",
                            TransformException.Severity.FATAL);
                }
                for(RecordProperty recordProperty : propertySet.get().getProperty()) {
                    setRecordProperty(record, recordProperty, newRecord, row, columnNames);
                }
            }
            // Otherwise just process the single Property
            else {
                setRecordProperty(record, (RecordProperty) recordPropertyOrSet, newRecord, row, columnNames);
            }

        }

        log.debug("Finished processing row");
        return record;
    }

    protected Record createRecord(Retrieval retrieval) {
        Record recordInstance = new Record();
        recordInstance.setRetrieval(retrieval);
        recordInstance.setAuthority(retrieval.getDataInstance().getDataSource().getEntity());
        return recordInstance;
    }

    /** 
     * Invoke an old record retriever, if there is one in the mapping
     * */
    protected Optional<Record> invokeOldRecordRetriever(Row row, MappedSheet mapping, Retrieval retrieval, Map<String, Integer> columnNames)
            throws TransformException, DateFormatException, CurrentRetrievalExistingRecordException {
        if(mapping.getRetriever() != null) {
            log.trace("Mapping specifies a retriever with class " + mapping.getRetriever().getClassName() + ", instantiating");
            RecordRetriever retriever = (RecordRetriever) instantiateComponent(mapping.getRetriever().getClassName());
            return Optional.ofNullable(retriever.retrieveRecord(retrieval,
                    getCellMapForArguments(row, mapping.getRetriever().getSourceFileColumn(), columnNames), log));
        }
        return Optional.empty();
    }

    /** 
     * Apply a filter, if there is one in the mapping
     * */
    protected boolean rowFilterPassed(Row row, MappedSheet mapping, Retrieval retrieval, Map<String, Integer> columnNames)
            throws TransformException {
        if(mapping.getFilter() != null) {
            for(RowFilter rowFilter : mapping.getFilter()) {
                log.trace("Mapping specifies a filter with class " + rowFilter.getClassName() + ", instantiating");
                SourceRowFilter filter = (SourceRowFilter) instantiateComponent(rowFilter.getClassName());
                if(!filter.proceedWithRow(retrieval, getCellMapForArguments(row, rowFilter.getSourceFileColumn(), columnNames))) {
                    log.debug("Filter " + rowFilter.getClassName() + " has disqualified this row");
                    return false;
                }
            }
        }
        return true;
    }

    protected void setRecordProperty(Optional<Record> record, RecordProperty recordProperty, boolean newRecord,
                                   Row row, Map<String, Integer> columnNames) throws TransformException, DateFormatException{

        if(record.isPresent()) {
            log.trace("Updating property " + recordProperty.getName());
    
            //In case we're updating a record that's already been inserted
            if(!newRecord && recordProperty.isOnlyNewRecords()) {
                log.trace("Skipping property as it can only be applied to new records");
                return;
            }
    
            //For each property, either set the corresponding fixed value by resolving a string
            if(recordProperty.getValue() != null) {
                setFixedValue(record.get(), recordProperty);
            }
            else {
                //Or instantiate and call the corresponding converter with a hashmap of arguments
                setProcessedValue(recordProperty, record.get(), row, columnNames);
            }
        }
    }

    /**
     * Instantiates a RecordPropertyConverter and invokes its updateRecordProperty method with arguments retrieved from
     * the source workbook row being processed.
     * @param recordProperty The RecordProperty defined in the mapping XML.
     * @param record The currently processed Record.
     * @param row The currently processed Row.
     * @param columnNames The mapping of column names to column indices in the workbook
     * @throws TransformException
     * @see RecordPropertyConverter#updateRecordProperty(Record, Map, String, Logger)
     */
    protected void setProcessedValue(RecordProperty recordProperty, Record record, Row row,
                                   Map<String, Integer> columnNames) throws TransformException, DateFormatException {

        log.trace("Instantiating property converter " + recordProperty.getConverter());

        RecordPropertyConverter rpc;
        String converterParameter = recordProperty.getConverterParameter();
        if (converterParameter != null) {
            rpc = (RecordPropertyParameterConverter) instantiateComponent(recordProperty.getConverter(), converterParameter);
            ((RecordPropertyParameterConverter) rpc).setParameter(converterParameter); // when instantiation has already proceeded before
        } else {
            rpc = (RecordPropertyConverter) instantiateComponent(recordProperty.getConverter());
        }
        Map<String, Cell> argumentMap = getCellMapForArguments(row, recordProperty.getSourceFileColumn(), columnNames);

        try {
            rpc.updateRecordProperty(record, argumentMap, recordProperty.getName(), log);
        } catch (TransformException e) {
            if(e.getSeverity().equals(TransformException.Severity.PROPERTY_LOCAL)) {
                log.warn(e.getMessage(), e);
            }
            else throw e;
        }
    }

    /**
     * Sets a fixed value for a Record property, as defined by the RecordProperty object in the mapping file. The method
     * will automatically convert the value to the appropriate type based on the type of the field being set. This can
     * be a primitive type, a String or an enum value.
     * @param record The record on which to set the property.
     * @param recordProperty The RecordProperty defined in the mapping XML.
     * @throws TransformException In case the data type conversion fails.
     */
    protected void setFixedValue(Record record, RecordProperty recordProperty) throws TransformException {
        try {
            Field field = Record.class.getDeclaredField(recordProperty.getName());
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            field.set(record, getValueFromString(recordProperty.getValue(), fieldType));
        }
        catch (IllegalAccessException | NoSuchFieldException | TransformException | RuntimeException e) {
            String message = "Couldn't set a fixed value for field " + recordProperty.getName();
            if (recordProperty.isRequired()) {
                //For IllegalAccess and NoSuchField, this will happen every time
                TransformException.Severity severity = TransformException.Severity.RECORD_LOCAL;
                if(!RuntimeException.class.equals(e.getClass())) {
                    severity = TransformException.Severity.FATAL;
                }
                throw new TransformException(message, e, severity);

            } else {
                log.warn(message, e);
            }
        }
    }

    @Override
    public void checkRecordIntegrity(Record record) throws TransformException {
        List<Field> offendingFields = new ArrayList<>();
        String[] requiredFields = recordRequiredFields.split(",");

        for(String fieldName : requiredFields) {
            try {
                Field field = Record.class.getDeclaredField(fieldName);
                field.setAccessible(true); //Is this a Bad Idea(TM)?
                if(field.get(record) == null) {
                    offendingFields.add(field);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.warn("Couldn't check integrity of field " + fieldName, e);
            }
        }

        if(!offendingFields.isEmpty()) {
            StringBuilder buffer = new StringBuilder("Finished Record is missing required values for columns: ");
            for(Field field : offendingFields) {
                buffer.append(field.getName()).append(", ");
            }
            buffer.delete(buffer.length() - 2, buffer.length() - 1);

            throw new TransformException(buffer.toString(), TransformException.Severity.RECORD_LOCAL);
        }

    }


    @SuppressWarnings("unchecked")
    protected <T> T getValueFromString(String string, Class<T> type) throws TransformException {
        //Primitive, wrapper or string
        if(isSimpleType(type)) {
            return type.cast(toObject(type, string));
        }

        //Enum value
        if(type.isEnum()) {
            Enum<?> value = Enum.valueOf((Class<Enum>)type, string);
            return type.cast(value);
        }

        throw new TransformException("Cannot set a fixed value for a non-primitive, non-enum field",
                TransformException.Severity.FATAL);
    }


    /**
     * Creates the mapping between arguments passed to TransformComponents and actual workbook cells.
     * @param row The currently processed workbook row
     * @param sourceColumns SourceColumns defined in the mapping for a single element
     * @param columnNames The mapping of column names to column indices in the workbook
     * @return A map of "argumentName: cell" retrieved from the workbook row
     * @throws TransformException
     */
    protected Map<String, Cell> getCellMapForArguments(Row row, List<SourceColumn> sourceColumns,
                                                     Map<String, Integer> columnNames) {
        Map<String, Cell> argumentMap = new HashMap<>();
        for(SourceColumn sourceColumn : sourceColumns) {
            Integer columnIndex = columnNames.get(sourceColumn.getOriginalName());
            if(columnIndex == null) {
                if(sourceColumn.getNumber() != null) {
                    columnIndex = sourceColumn.getNumber();
                }
                else {
                    log.trace("Couldn't find source coulumn with name " + sourceColumn.getOriginalName());
                    continue;
                }
            }
            argumentMap.put(sourceColumn.getArgumentName(), row.getCell(columnIndex));
        }
        return argumentMap;
    }


    protected TransformComponent instantiateComponent(String className) throws TransformException {
        try {
            return converterFactory.getComponent(className);
        } catch (ClassNotFoundException | ClassCastException e) {
            String message = "Could not instantiate component " + className;
            throw new TransformException(message, e, TransformException.Severity.FATAL);
        }
    }

    protected TransformComponent instantiateComponent(String className, String parameter) throws TransformException {
        try {
            return converterFactory.getComponent(className, parameter);
        } catch(ClassNotFoundException | ClassCastException e) {
            String message = "Could not instantiate component " + className;
            throw new TransformException(message, e, TransformException.Severity.FATAL);
        }
    }

    //Utility
    protected static final Set<Class<?>> SIMPLE_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            String.class, Boolean.TYPE, Character.TYPE, Byte.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE));

    protected static boolean isSimpleType(Class<?> clazz) {
        return SIMPLE_TYPES.contains(clazz);
    }

    protected static Object toObject( Class<?> clazz, String value ) {
        if( Boolean.class == clazz || Boolean.TYPE == clazz ) return Boolean.parseBoolean( value );
        if( Byte.class == clazz    || Byte.TYPE    == clazz ) return Byte.parseByte( value );
        if( Short.class == clazz   || Short.TYPE   == clazz ) return Short.parseShort( value );
        if( Integer.class == clazz || Integer.TYPE == clazz ) return Integer.parseInt( value );
        if( Long.class == clazz    || Long.TYPE    == clazz ) return Long.parseLong( value );
        if( Float.class == clazz   || Float.TYPE   == clazz ) return Float.parseFloat( value );
        if( Double.class == clazz  || Double.TYPE  == clazz ) return Double.parseDouble( value );
        return value;
    }


    //Test
    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEm() {
        return em;
    }
}
