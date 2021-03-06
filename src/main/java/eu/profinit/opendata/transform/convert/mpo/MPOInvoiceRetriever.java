package eu.profinit.opendata.transform.convert.mpo;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.AllAmountSetter;
import eu.profinit.opendata.transform.convert.DateFormatException;
import eu.profinit.opendata.transform.convert.SplitIdentifierSetter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieves MPO invoices based on the partner name, subject and originalCurrencyAmount. Only an exact match in all
 * considered attributes counts.
 * Throws a FATAL exception if more than one candidate record is found.
 */
@Component
public class MPOInvoiceRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Autowired
    private SplitIdentifierSetter splitIdentifierSetter;

    @Autowired
    private AllAmountSetter allAmountSetter;


        @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger)
            throws TransformException, DateFormatException {
        try {
            // Get filter values

            String authorityIdentifier = splitIdentifierSetter.getIdentifierFromSourceValues(sourceValues);

            Double amount = allAmountSetter.getAmountFromSourceValues(sourceValues);

            // Get all MK contracts from the DB
            List<Record> allContracts = em.createQuery(
                    "Select r from Record r where r.authority = :authority and r.recordType in :types ", Record.class)
                    .setParameter("authority", currentRetrieval.getDataInstance().getDataSource().getEntity())
                    .setParameter("types", Arrays.asList(RecordType.PAYMENT, RecordType.INVOICE))
                    .getResultList();

            // Filter by authId, partner name, subject and date created
            List<Record> filtered;
            filtered = allContracts.stream().filter(i ->
                    i.getAuthorityIdentifier().equals(authorityIdentifier)
                            && i.getOriginalCurrencyAmount().equals(amount)
            ).collect(Collectors.toList());


            // Return whatever we find
            if (!filtered.isEmpty()) {
                if (filtered.size() > 1) {
                    throw new TransformException("More than one old candidate contract has been found",
                            TransformException.Severity.FATAL);
                } else {
                    return filtered.get(0);
                }
            }
        } catch (Exception ex) {
            // We'll just return null
            logger.warn("Old record retrieval failed", ex);
        }
        return null;
    }
}
