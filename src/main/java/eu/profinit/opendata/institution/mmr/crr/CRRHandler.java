package eu.profinit.opendata.institution.mmr.crr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

public interface CRRHandler  extends DataSourceHandler {
    void updateDataInstances(DataSource ds);
}
