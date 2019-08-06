package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.query.CurrentRetrievalExistingRecordException;
import eu.profinit.opendata.query.RecordQueryService;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.DateFormatException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tries to set the parent of a MFCR contract. Expects a source cell with argumentName = "type", which contains either
 * "S" (for a standalone contract) or "D" (for an amendment). If the value is "D", uses the authority identifier prefix
 * to find the parent contract to which the amendment is tied. The fieldName argument is ignored.
 */
@Component
public class ContractParentSetter implements RecordPropertyConverter {

    @Autowired
    private RecordQueryService recordQueryService;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        String sd = sourceValues.get("type").getStringCellValue();

        // Any amendments to a contract have a two-digit suffix. We search for their parents here.
        if(sd.equals("D")) {
            Map<String, String> filter = new HashMap<>();
            filter.put("authorityIdentifier", record.getAuthorityIdentifier()
                    .substring(0, record.getAuthorityIdentifier().length() - 2));

            List<Record> found = new ArrayList<>();
            try {
                found = recordQueryService.findRecordsByFilter(filter, record.getRetrieval());
            } catch (CurrentRetrievalExistingRecordException e) {
                // the exception does not have any effect here
            }
            if(!found.isEmpty()) {
                record.setParentRecord(found.get(0));
            }
        }
    }
}
