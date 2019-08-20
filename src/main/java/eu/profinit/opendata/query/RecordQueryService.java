package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.IdentifierAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A service for querying records in the database. Isn't designed as a single access point to Records and
 * other components may query the database directly. The queries are run on the database, but also on Records
 * saved in a current Retrieval and not yet persisted.
 */
@Component
public class RecordQueryService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    protected IdentifierAppender identifierAppender;

    /**
     * Finds records in the database and/or in the specified Retrieval.
     * @param filter A map of attribute-value pairs to be used as filters.
     * @param currentRetrieval The Retrieval to be searched in along with the database.
     * @return A list of found records.
     */
    public List<Record> findRecordsByFilter(Map<String, String> filter, Retrieval currentRetrieval) {
        // Look in the retrieval first
        Collection<Record> finishedRecords = currentRetrieval.getRecords();
        Stream<Record> stream = finishedRecords.stream();
        for(Entry<String, String> entry : filter.entrySet()) {
            stream = stream.filter(new RecordPropertyPredicate(entry.getKey(), entry.getValue()));
        }
        List<Record> found = stream.collect(Collectors.toList());

        // Then try older records
        if(found.isEmpty()) {
            found = findRecordsByFilter(filter);
        } else {
            addAnotherRecord(found);
            // If there is a record with given values in the current retrieval, we want to store the same one
            // anyway (several same items) with adjusted authority identifier
        }

        return found;

    }

    private void addAnotherRecord(List<Record> found) {
        Record lastCopy = found.get(found.size()-1).copy();
        lastCopy.setMasterId(UUID.randomUUID().toString());
        identifierAppender.append(lastCopy);
        found.add(lastCopy);
    }

    /**
     * Finds records in the database. Supports querying by any String attribute.
     * @param filter A map of attribute-value pairs to be used as filters.
     * @return A list of found records.
     */
    public List<Record> findRecordsByFilter(Map<String, String> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Record> qr = cb.createQuery(Record.class);
        Root<Record> root = qr.from(Record.class);
        qr.select(root);
        javax.persistence.criteria.Predicate predicate = cb.conjunction();

        for(Entry<String, String> entry : filter.entrySet()) {
            predicate = cb.and(predicate, cb.equal(root.get(entry.getKey()), entry.getValue()));
        }
        qr = qr.where(predicate);

        return em.createQuery(qr).getResultList();
    }

    static class RecordPropertyPredicate implements Predicate<Record> {

        String property;
        String value;

        public RecordPropertyPredicate(String property, String value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean test(Record record) {
            switch(property) {
                case "authorityIdentifier":
                    return record.getAuthorityIdentifier() != null
                            && (record.getAuthorityIdentifier().equals(value)
                            || record.getAuthorityIdentifier().contains(value + "__")); // BUG 52483
                case "budgetCategory":
                    return record.getBudgetCategory() != null
                            && record.getBudgetCategory().equals(value);
                case "subject":
                    return record.getSubject() != null
                            && record.getSubject().equals(value);
                case "amountCzk":
                    return record.getAmountCzk() != null
                            && record.getAmountCzk().toString().equals(value);
                default:
                    return false;
            }
        }
    }
}
