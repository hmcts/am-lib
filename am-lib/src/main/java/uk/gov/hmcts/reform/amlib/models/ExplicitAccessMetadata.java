package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

@Data
@Builder
public class ExplicitAccessMetadata {

    private final String resourceId;
    private final String accessorId;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;

    @JdbiConstructor
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public ExplicitAccessMetadata(String resourceId,
                                  String accessorId,
                                  String accessType,
                                  String serviceName,
                                  String resourceType,
                                  String resourceName,
                                  String attribute) {
        this.resourceId = resourceId;
        this.accessorId = accessorId;
        this.accessType = accessType;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
    }
}
