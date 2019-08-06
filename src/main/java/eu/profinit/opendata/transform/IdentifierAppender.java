package eu.profinit.opendata.transform;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.Record;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Component
public class IdentifierAppender {
    @PersistenceContext
    EntityManager em;

    public void append(Record record) {
        String originalId = record.getAuthorityIdentifier();
        Entity authority = record.getAuthority();
        String appendedId = originalId;
        int order = 1;

        List<Record> result = queryByAuthorityIdAndEntity(record.getAuthorityIdentifier(), authority);

        while (!result.isEmpty()) {
            appendedId = originalId + "_" + order;
            result = queryByAuthorityIdAndEntity(appendedId, authority);
            order++;
        }

        record.setAuthorityIdentifier(appendedId);
    }

    private List<Record> queryByAuthorityIdAndEntity(String authorityIdentifier, Entity authority) {
        return em.createNamedQuery("findByAuthorityIdAndEntity", Record.class)
                .setParameter("authorityIdentifier", authorityIdentifier)
                .setParameter("authority", authority)
                .getResultList();
    }
}
