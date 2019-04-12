package eu.profinit.opendata.institution.mmr.sfrb;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

public interface SFRBHandler extends DataSourceHandler {
    /**
     * Internal method, exposed for testing purposes. Not to be called directly.
     * @param ds data source
     */
    void updateDataInstances(DataSource ds);

}
