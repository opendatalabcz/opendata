package eu.profinit.opendata.institution.rest.kanapi;

import eu.profinit.opendata.control.GenericDataSourceHandler;
import eu.profinit.opendata.institution.rest.JSONClient;
import eu.profinit.opendata.institution.rest.JSONPackageListResource;
import eu.profinit.opendata.institution.rest.JSONPackageListStrict;
import eu.profinit.opendata.model.DataInstance;
import eu.profinit.opendata.model.DataSource;
import eu.profinit.opendata.model.DataSourceHandler;
import eu.profinit.opendata.model.Periodicity;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class KANAPIHandler extends GenericDataSourceHandler implements DataSourceHandler {

    @Autowired
    private JSONClient jsonClient;

    protected abstract String getMappingFile(String name);
    protected abstract String getInstanceName(String name);

    public void updateInvoicesDataInstance(DataSource ds, String patternRegex, Periodicity periodicity,
                                           String jsonApiUrl, String packagesPath, String invoicesIdentifier, String invoicesMappingFile, Logger log) {
        log.info("Updating information about data instances containing invoices");

        JSONPackageListStrict packageList = jsonClient.getPackageListStrict(jsonApiUrl, packagesPath, invoicesIdentifier);
        if (packageList == null) {
            log.warn("JSONClient returned null package list. Exiting.");
            return;
        }

        List<DataInstance> currentInstances = new ArrayList<>(ds.getDataInstances());
        List<JSONPackageListResource> resourceList = packageList.getResult().getResources();

        for (JSONPackageListResource resource : resourceList) {
            if (resource.getFormat().equalsIgnoreCase("csv")) {
                // Check for "uhrazene faktury" and "za rok {YYYY}" and not "privatizace"
                String name = resource.getName();
                String resourceId = resource.getUrl(); // No ID in JSON response anymore

                Pattern pattern = Pattern.compile(patternRegex);
                Matcher matcher = pattern.matcher(name);
                if (!matcher.find()) {
                    continue;
                }

                DataInstance dataInstance = new DataInstance();

                // Check if we already have a data instance with the same given id - if yes, simply update the URL
                // If not, create a new one
                Optional<DataInstance> sameIds = currentInstances.stream()
                        .filter(i -> i.getAuthorityId().equals(resourceId)).findFirst();
                if (sameIds.isPresent()) {
                    dataInstance = sameIds.get();
                    dataInstance.setUrl(resource.getUrl());
                } else {
                    String mappingFile = getMappingFile(name);
                    String instanceName = getInstanceName(name);

                    dataInstance.setMappingFile(mappingFile);
                    dataInstance.setDataSource(ds);
                    dataInstance.setUrl(resource.getUrl());
                    dataInstance.setAuthorityId(resourceId);
                    dataInstance.setFormat(resource.getFormat().toLowerCase());
                    dataInstance.setDescription(instanceName);
                    dataInstance.setPeriodicity(periodicity);
                    dataInstance.setIncremental(false);
                    ds.getDataInstances().add(dataInstance);
                    em.persist(dataInstance);
                }

                em.merge(dataInstance);

            }
        }
    }
}
