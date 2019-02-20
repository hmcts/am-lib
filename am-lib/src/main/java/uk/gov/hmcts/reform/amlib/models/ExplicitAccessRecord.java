package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ExplicitAccessRecord extends AbstractAccessMetadata {

    private final Set<Permission> explicitPermissions;

    @Builder // All args constructor is needs for builder. @SuperBuilder cannot be used because IDE does not support it
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    private ExplicitAccessRecord(String resourceId,
                                 String accessorId,
                                 String accessType,
                                 String serviceName,
                                 String resourceType,
                                 String resourceName,
                                 String attribute,
                                 String securityClassification,
                                 Set<Permission> explicitPermissions) {
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute,
            securityClassification);
        this.explicitPermissions = explicitPermissions;
    }

    @JdbiConstructor
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public ExplicitAccessRecord(String resourceId,
                                String accessorId,
                                int permissions,
                                String accessType,
                                String serviceName,
                                String resourceType,
                                String resourceName,
                                String attribute,
                                String securityClassification) {
        this(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute,
            securityClassification, Permissions.fromSumOf(permissions));
    }

    public int getPermissions() {
        return Permissions.sumOf(explicitPermissions);
    }

}
