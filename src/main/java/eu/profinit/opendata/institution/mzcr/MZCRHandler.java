package eu.profinit.opendata.institution.mzcr;

import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;

public interface MZCRHandler extends DataSourceHandler {

    /**
     * Internal method, exposed for testing purposes. Not to be called directly.
     * @param ds
     */
    void updateDataInstances(DataSource ds);

}
