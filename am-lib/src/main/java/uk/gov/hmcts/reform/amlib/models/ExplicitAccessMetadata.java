package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD")
public final class ExplicitAccessMetadata extends AbstractAccessMetadata {

    @Builder // All args constructor is needs for builder. @SuperBuilder cannot be used because IDE does not support it
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    private ExplicitAccessMetadata(String resourceId,
                                   String accessorId,
                                   String accessType,
                                   String serviceName,
                                   String resourceType,
                                   String resourceName,
                                   String attribute,
                                   String securityClassification) {
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute + "%",
            securityClassification);
    }
}
