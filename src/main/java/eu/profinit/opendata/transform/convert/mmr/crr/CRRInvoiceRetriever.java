package eu.profinit.opendata.transform.convert.mmr.crr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.DateCategorySetter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CRRInvoiceRetriever implements RecordRetriever {
    @Autowired
    private DateCategorySetter dateCategorySetter;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException {
        String authorityIdentifier = dateCategorySetter.getIdentifierFromSourceValues(sourceValues, logger);

        List<Record> allContracts = em.createQuery(
                "Select r from Record r where r.authority = :authority and r.recordType in :types ", Record.class)
                .setParameter("authority", currentRetrieval.getDataInstance().getDataSource().getEntity())
                .setParameter("types", Arrays.asList(RecordType.PAYMENT, RecordType.INVOICE))
                .getResultList();

        List<Record> filtered;
        filtered = allContracts.stream().filter(i ->
                i.getAuthorityIdentifier().equals(authorityIdentifier)
        ).collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            if (filtered.size() > 1) {
                throw new TransformException("More than one old candidate contract has been found",
                        TransformException.Severity.FATAL);
            } else {
                return filtered.get(0);
            }
        }

        return null;
    }
}
