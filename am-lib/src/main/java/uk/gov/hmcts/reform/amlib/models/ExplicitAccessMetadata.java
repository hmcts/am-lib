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

}
