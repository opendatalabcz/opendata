package eu.profinit.opendata.institution.mmr.czt;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

public interface CZTHandler extends DataSourceHandler {

    void updateDataInstances(DataSource ds);
}
