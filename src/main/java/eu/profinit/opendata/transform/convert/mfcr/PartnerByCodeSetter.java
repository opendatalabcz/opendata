package eu.profinit.opendata.transform.convert.mfcr;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.query.PartnerQueryService;
import eu.profinit.opendata.transform.Cell;
import eu.profinit.opendata.transform.RecordPropertyConverter;
import eu.profinit.opendata.transform.TransformException;
import eu.profinit.opendata.transform.convert.DateFormatException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sets the partner Entity for an old MFCR invoice extracted from a partner list. Expects a source cell with
 * argumentName = "code" containing the partner code.
 * @see PartnerQueryService#findFromPartnerList(Entity, String)
 */
@Component
public class PartnerByCodeSetter implements RecordPropertyConverter {

    @Autowired
    private PartnerQueryService partnerQueryService;

    @Override
    public void updateRecordProperty(Record record, Map<String, Cell> sourceValues, String fieldName, Logger logger)
            throws TransformException, DateFormatException {

        String code = sourceValues.get("code").getStringCellValue();

        Entity authority = record.getRetrieval().getDataInstance().getDataSource().getEntity();
        Entity partner = partnerQueryService.findFromPartnerList(authority, code.substring(0, code.length() - 3));

        record.setPartner(partner);
    }
}
