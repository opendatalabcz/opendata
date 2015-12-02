package eu.profinit.opendata.handler.mfcr;

import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.Periodicity;
import eu.profinit.opendata.model.Retrieval;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dm on 11/28/15.
 */
@Component
@PropertySource("classpath:mfcr.properties")
public class MFCRHandler extends GenericDataSourceHandler {

    @Autowired
    private JSONClient jsonClient;

    @Value("${mfcr.json.orders.identifier}")
    private String orders_identifier;

    @Value("${mfcr.mapping.orders}")
    private String order_mapping_file;

    @PersistenceContext(unitName = "postgres")
    private EntityManager em;

    @Override
    protected void checkForNewDataInstance(DataSource ds) {
        switch(ds.getRecordType()) {
            case ORDER: updateOrdersDataInstance(ds); break;
            default: break;
        }
    }

    @Override
    protected String getMappingFileForDataInstance(DataInstance di) {
        switch(di.getDataSource().getRecordType()) {
            case ORDER: return order_mapping_file;
            default: return null;
        }
    }


    /**
     * Updates the data intances associated with the specified ORDERS data source.
     * Assumes that the JSON API only returns a single xls(x) resource (true as of Nov 2015)
     * @param ds An ORDERS DataSource
     */
    private void updateOrdersDataInstance(DataSource ds) {

        //Load list of resources from the JSON API
        JSONPackageList packageList = jsonClient.getPackageList(orders_identifier);
        if(packageList == null) {
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for(JSONPackageListResource resource : resourceList) {

            //Check if we found an xls resource
            if(resource.getFormat().equals("xls") || resource.getFormat().equals("xlsx")) {

                //Ignore if there is a data instance with the same URL
                String newUrl = resource.getUrl();
                if(currentInstances.stream()
                        .filter(i -> i.getUrl().toLowerCase().equals(newUrl.toLowerCase())).count() == 0) {

                    em.getTransaction().begin();

                    //All current data instances must be marked as expired
                    for(DataInstance i : currentInstances) {
                        i.expire();
                        em.merge(i);
                    }

                    //Recreate a new active data instance
                    DataInstance di = new DataInstance();
                    di.setDataSource(ds);
                    di.setFormat(resource.getFormat());
                    di.setPeriodicity(Periodicity.MONTHLY);
                    di.setUrl(resource.getUrl());

                    ds.getDataInstances().add(di);
                    em.persist(di);
                    em.getTransaction().commit();

                }
            }
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}