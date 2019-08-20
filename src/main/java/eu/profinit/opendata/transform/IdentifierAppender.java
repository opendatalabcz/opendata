package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Record;
import eu.profinit.opendata.model.Retrieval;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

@Component
public class IdentifierAppender {
    @PersistenceContext
    EntityManager em;

    public void append(Record record) {
        String originalId = record.getAuthorityIdentifier();
        Entity authority = record.getAuthority();
        String appendingDelimiter = "__";

        String appendedId = createNewIdentifier(originalId, appendingDelimiter);

        List<Record> result = queryByAuthorityIdAndEntity(record.getAuthorityIdentifier(), authority);

        while (!result.isEmpty()) {
            result = queryByAuthorityIdAndEntity(appendedId, authority);
            appendedId = createNewIdentifier(appendedId, appendingDelimiter);
        }

        record.setAuthorityIdentifier(appendedId);
    }

    private String createNewIdentifier(String originalId, String appendingDelimiter) {
        String[] orderSplit = originalId.split(appendingDelimiter);
        String appendedId;

        if (orderSplit.length > 1 && StringUtils.isNumeric(orderSplit[orderSplit.length-1])) {
            int order = Integer.parseInt(orderSplit[orderSplit.length-1]);
            order++;

            appendedId = String.join("", Arrays.copyOf(orderSplit, orderSplit.length - 1));
            appendedId += appendingDelimiter + order;
        } else {
            appendedId = originalId + appendingDelimiter + 1;
        }
        return appendedId;
    }

    private List<Record> queryByAuthorityIdAndEntity(String authorityIdentifier, Entity authority) {
        return em.createNamedQuery("findByAuthorityIdAndEntity", Record.class)
                .setParameter("authorityIdentifier", authorityIdentifier)
                .setParameter("authority", authority)
                .getResultList();
    }
}
