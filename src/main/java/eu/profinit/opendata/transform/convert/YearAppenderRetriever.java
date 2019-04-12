package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class YearAppenderRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException {
        try {
            String categoryType = sourceValues.get("categoryType").getStringCellValue();
            String serialNumber = sourceValues.get("serialNumber").getStringCellValue();
            String year = getInvoiceYear(currentRetrieval.getDataInstance());
            String authorityIdentifier = year + "-" + categoryType + "-" + serialNumber; //TODO refactor - at je to na jednom miste

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
            logger.warn("Record retrieval failed", ex);
        }
        return null;
    }

    /**
     *
     * @param dataInstance
     * @return
     */
    private String getInvoiceYear(DataInstance dataInstance) throws InvalidPropertiesFormatException {
        String name = dataInstance.getDescription();
        if (name.length() < 4) {
            throw new InvalidPropertiesFormatException("Invoice year is expected to be located after '_' sign.");
        }
        return name.substring(name.length() - 4);
    }
}
