package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Record;

import javax.persistence.EntityManager;

/**
 * The component responsible for the processing of a data frame (eg. Excel workbook, CSV file). Proceeds row-by-row, inserting and updating Records in
 * the database until the whole document has been read. The DataFrameProcessor requests its own database transaction so
 * that it can be rolled back safely in case of an error. During this transaction, no new Records are persisted -
 * instead, they are added to the Retrieval object that is being filled. The persist of the Retrieval will then cascade
 * to Records. However, Entities and other support objects may be inserted into the database directly during the course
 * of workbook processing.
 */
public interface DataFrameProcessor {

    /**
     * Verifies a Record by checking all mandatory attributes are non-null. Throws a RECORD_LOCAL exception if the Record
     * isn't valid.
     * @param record The record to be checked.
     * @throws TransformException
     */
    void checkRecordIntegrity(Record record) throws TransformException;

    //Testing purposes
    void setEm(EntityManager em);
    EntityManager getEm();

}
