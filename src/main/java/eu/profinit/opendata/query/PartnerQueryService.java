package eu.profinit.opendata.query;

import eu.profinit.opendata.model.Entity;
import eu.profinit.opendata.model.EntityType;
import eu.profinit.opendata.model.PartnerListEntry;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static eu.profinit.opendata.common.Util.isNullOrEmpty;

/**
 * Created by dm on 12/16/15.
 */
@Component
public class PartnerQueryService {

    @PersistenceContext
    private EntityManager em;

    public Entity findEntityByAuthorityId(String authorityId) {
        return em.createNamedQuery("findByAuthId", Entity.class)
                .setParameter("authId", authorityId)
                .getSingleResult();
    }

    public PartnerListEntry findOrCreatePartnerListEntry(Entity authority, Entity partner, String code) {
        PartnerListEntry found = em.createNamedQuery("findByAuthorityAndCode", PartnerListEntry.class)
                                                .setParameter("authority", authority)
                                                .setParameter("code", code)
                                                .getSingleResult();

        if(found == null) {
            PartnerListEntry result = new PartnerListEntry();
            result.setAuthority(authority);
            result.setPartner(partner);
            result.setCode(code);
            em.persist(result);
            return result;
        }
        else return found;
    }

    public Entity findFromPartnerList(Entity authority, String code) {
        PartnerListEntry found = em.createNamedQuery("findByAuthorityAndCode", PartnerListEntry.class)
                .setParameter("authority", authority)
                .setParameter("code", code)
                .getSingleResult();

        if(found != null) {
            return found.getPartner();
        }
        return null;
    }

    public Entity findOrCreateEntity(String name, String ico, String dic) {

        Entity found = findEntity(name, ico, dic);
        // Pokud nemame ani jednoho kandidata, musime vytvorit noveho

        // Pokud vracime kandidata, ktery ma neco nevyplnene, doplnime a zmergujeme
        if(found != null) {
            if(!isNullOrEmpty(dic) && found.getDic() == null) {
                found.setDic(dic);
            }
            if(!isNullOrEmpty(ico) && found.getIco() == null) {
                found.setIco(ico);
            }
            em.merge(found);
        }

        if(found == null) {
            found = new Entity();
            found.setName(name);
            found.setEntityType(EntityType.COMPANY); // TODO: Automatická detekce toho, co je to za EntityType?
            found.setDic(dic);
            found.setIco(ico);
            found.setPublic(false);
            em.persist(found);
        }

        return found;
    }

    public Entity findEntity(String name, String ico, String dic) {
        List<Entity> candidates = new ArrayList<>();
        // Pokud mame ico a dic, zkusime napred podle obou
        // Pokud jsme jeste nic nenalezli, zkusime podle dic
        // Pokud jsme jeste nic nenalezli, zkusime podle ico
        // Mame-li kandidata, vracime ho.

        if(!isNullOrEmpty(ico) && !isNullOrEmpty(dic)) {
            candidates = em.createNamedQuery("findByICOAndDIC", Entity.class)
                    .setParameter("ico", ico)
                    .setParameter("dic", dic)
                    .getResultList();
        }
        if(candidates.isEmpty() && !isNullOrEmpty(ico)) {
            candidates = em.createNamedQuery("findByICO", Entity.class)
                    .setParameter("ico", ico)
                    .getResultList();
        }
        if(candidates.isEmpty() && !isNullOrEmpty(dic)) {
            candidates = em.createNamedQuery("findByDIC", Entity.class)
                    .setParameter("dic", ico)
                    .getResultList();
        }

        // Pokud mame jenom jmeno nebo jsme jeste nenasli,
        // hledame podle jmena
        if(candidates.isEmpty()) {
            Entity fromName = findMatchingEntityByName(name);
            if(fromName != null) {
                candidates.add(fromName);
            }
        }

        if(!candidates.isEmpty()) {
            return candidates.get(0);
        }
        return null;
    }

    private Entity findMatchingEntityByName(String name) {
        // TODO: Tady je potreba brat v potaz mozne preklepy, substringy, zkratky apod.
        // Vyber z vice kandidatu a tak
        // Muzeme pak nejak zajistit deduplikaci? Treba v nejakem GUI - databaze si ale pak musi pamatovat,
        // co deduplikovala na co a na jake zaznamy se to vztahuje
        // Pokud mame vic kandidatu, vezmeme zatim prvniho

        String query = name.toLowerCase().trim();
        List<Entity> candidates = em.createNamedQuery("findByName", Entity.class)
                .setParameter("name", "%" + query + "%").getResultList();

        if(!candidates.isEmpty()) {
            return  candidates.get(0);
        }

        return null;
    }

}
