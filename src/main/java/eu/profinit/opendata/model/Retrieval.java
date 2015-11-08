package eu.profinit.opendata.model;

import javax.persistence.*;
import javax.persistence.Entity;
import java.sql.Timestamp;

/**
 * Created by DM on 8. 11. 2015.
 */
@Entity
public class Retrieval {
    private Timestamp date;
    private String failureReason;
    private int numBadRecords;
    private int numRecordsInserted;
    private boolean success;
    private int retrievalId;
    private DataInstance retrievals;

    @Basic
    @Column(name = "date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Basic
    @Column(name = "failure_reason")
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Basic
    @Column(name = "num_bad_records")
    public int getNumBadRecords() {
        return numBadRecords;
    }

    public void setNumBadRecords(int numBadRecords) {
        this.numBadRecords = numBadRecords;
    }

    @Basic
    @Column(name = "num_records_inserted")
    public int getNumRecordsInserted() {
        return numRecordsInserted;
    }

    public void setNumRecordsInserted(int numRecordsInserted) {
        this.numRecordsInserted = numRecordsInserted;
    }

    @Basic
    @Column(name = "success")
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Id
    @Column(name = "retrieval_id")
    public int getRetrievalId() {
        return retrievalId;
    }

    public void setRetrievalId(int retrievalId) {
        this.retrievalId = retrievalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Retrieval retrieval = (Retrieval) o;

        if (numBadRecords != retrieval.numBadRecords) return false;
        if (numRecordsInserted != retrieval.numRecordsInserted) return false;
        if (retrievalId != retrieval.retrievalId) return false;
        if (success != retrieval.success) return false;
        if (date != null ? !date.equals(retrieval.date) : retrieval.date != null) return false;
        if (failureReason != null ? !failureReason.equals(retrieval.failureReason) : retrieval.failureReason != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (failureReason != null ? failureReason.hashCode() : 0);
        result = 31 * result + numBadRecords;
        result = 31 * result + numRecordsInserted;
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + retrievalId;
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "data_instance_id", referencedColumnName = "data_instance_id")
    public DataInstance getRetrievals() {
        return retrievals;
    }

    public void setRetrievals(DataInstance retrievals) {
        this.retrievals = retrievals;
    }
}
