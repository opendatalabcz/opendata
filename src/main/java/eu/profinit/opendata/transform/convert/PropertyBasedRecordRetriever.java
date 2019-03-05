package eu.profinit.opendata.transform.convert;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.RecordType;
import eu.profinit.opendata.model.Retrieval;
import eu.profinit.opendata.query.RecordQueryService;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordRetriever;
import eu.profinit.opendata.transform.TransformException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Tries to find an existing Record with specified attribute values (contained in the sourceValues map). If one is
 * found, it is returned.
 * @see RecordQueryService#findRecordsByFilter(Map, Retrieval)
 */
@Component
public class PropertyBasedRecordRetriever implements RecordRetriever {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RecordQueryService recordQueryService;

    @Override
    public Record retrieveRecord(Retrieval currentRetrieval, Map<String, Cell> sourceValues, Logger logger) throws TransformException {
        HashMap<String, String> filters = new HashMap<>();

        for(Entry<String, Cell> entry : sourceValues.entrySet()) {
            entry.getValue().setCellType(Cell.CELL_TYPE_STRING);
            filters.put(entry.getKey(), entry.getValue().getStringCellValue());
        }
        
        return retrieveRecordByStrings(currentRetrieval, filters,
                                       currentRetrieval.getDataInstance().getDataSource().getRecordType());
    }

    public Record retrieveRecordByStrings(Retrieval currentRetrieval, Map<String, String> filters, RecordType type)
            throws TransformException {

        List<Record> found = recordQueryService.findRecordsByFilter(filters, currentRetrieval);
        Entity retrievalEntity = currentRetrieval.getDataInstance().getDataSource().getEntity();
        found = found.stream()
                .filter(i -> i.getAuthority().equals(retrievalEntity)
                          && i.getRecordType().equals(type))
                .collect(Collectors.toList());

        if(!found.isEmpty()) {
            // There should only be one at this point - if there are more, it indicates a problem with the mapping
            if(found.size() > 1) {
                throw new TransformException("More than one candidate record has been found",
                        TransformException.Severity.FATAL);
            }
            return found.get(0);
        }

        return null;

    }
}
