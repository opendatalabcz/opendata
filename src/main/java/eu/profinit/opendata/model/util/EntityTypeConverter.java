package eu.profinit.opendata.model.util;

import eu.profinit.opendata.model.EntityType;
import javax.persistence.AttributeConverter;

public class EntityTypeConverter implements AttributeConverter<EntityType, String> {

    @Override
    public String convertToDatabaseColumn(EntityType entityType) {
        if (entityType.equals(EntityType.MINISTRYORGANIZATION)) {
            return "ministry-organization";
        }
        return entityType.name().toLowerCase();
    }

    @Override
    public EntityType convertToEntityAttribute(String s) {
        s = s.replace("-", "");
        return EntityType.valueOf(s.toUpperCase());
    }
}