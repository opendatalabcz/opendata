package eu.profinit.opendata.institution.mmr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

public interface MMRHandler extends DataSourceHandler {
    /**
     * Internal method, exposed for testing purposes. Not to be called directly.
     * @param ds data source
     */
    void updateDataInstances(DataSource ds);

}
