package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import java.util.Set;

public final class RoleBasedAccessRecord implements AttributeAccessDefinition {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;

    @Override
    public String getAttributeAsString() {
        return attribute.toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }


    public static class RoleBasedAccessRecordBuilder {
        private String serviceName;
        private String resourceType;
        private String resourceName;
        private String roleName;
        private JsonPointer attribute;
        private Set<Permission> permissions;

        RoleBasedAccessRecordBuilder() {
        }

        public RoleBasedAccessRecordBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public RoleBasedAccessRecordBuilder resourceType(final String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public RoleBasedAccessRecordBuilder resourceName(final String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public RoleBasedAccessRecordBuilder roleName(final String roleName) {
            this.roleName = roleName;
            return this;
        }

        public RoleBasedAccessRecordBuilder attribute(final JsonPointer attribute) {
            this.attribute = attribute;
            return this;
        }

        public RoleBasedAccessRecordBuilder permissions(final Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RoleBasedAccessRecord build() {
            return new RoleBasedAccessRecord(serviceName, resourceType, resourceName, roleName, attribute, permissions);
        }

        @Override
        public String toString() {
            return "RoleBasedAccessRecord.RoleBasedAccessRecordBuilder(serviceName=" + this.serviceName + ", resourceType=" + this.resourceType + ", resourceName=" + this.resourceName + ", roleName=" + this.roleName + ", attribute=" + this.attribute + ", permissions=" + this.permissions + ")";
        }
    }

    public static RoleBasedAccessRecordBuilder builder() {
        return new RoleBasedAccessRecordBuilder();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public JsonPointer getAttribute() {
        return this.attribute;
    }

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleBasedAccessRecord)) return false;
        final RoleBasedAccessRecord other = (RoleBasedAccessRecord) o;
        final Object this$serviceName = this.getServiceName();
        final Object other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false;
        final Object this$resourceType = this.getResourceType();
        final Object other$resourceType = other.getResourceType();
        if (this$resourceType == null ? other$resourceType != null : !this$resourceType.equals(other$resourceType)) return false;
        final Object this$resourceName = this.getResourceName();
        final Object other$resourceName = other.getResourceName();
        if (this$resourceName == null ? other$resourceName != null : !this$resourceName.equals(other$resourceName)) return false;
        final Object this$roleName = this.getRoleName();
        final Object other$roleName = other.getRoleName();
        if (this$roleName == null ? other$roleName != null : !this$roleName.equals(other$roleName)) return false;
        final Object this$attribute = this.getAttribute();
        final Object other$attribute = other.getAttribute();
        if (this$attribute == null ? other$attribute != null : !this$attribute.equals(other$attribute)) return false;
        final Object this$permissions = this.getPermissions();
        final Object other$permissions = other.getPermissions();
        if (this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $serviceName = this.getServiceName();
        result = result * PRIME + ($serviceName == null ? 43 : $serviceName.hashCode());
        final Object $resourceType = this.getResourceType();
        result = result * PRIME + ($resourceType == null ? 43 : $resourceType.hashCode());
        final Object $resourceName = this.getResourceName();
        result = result * PRIME + ($resourceName == null ? 43 : $resourceName.hashCode());
        final Object $roleName = this.getRoleName();
        result = result * PRIME + ($roleName == null ? 43 : $roleName.hashCode());
        final Object $attribute = this.getAttribute();
        result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
        final Object $permissions = this.getPermissions();
        result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RoleBasedAccessRecord(serviceName=" + this.getServiceName() + ", resourceType=" + this.getResourceType() + ", resourceName=" + this.getResourceName() + ", roleName=" + this.getRoleName() + ", attribute=" + this.getAttribute() + ", permissions=" + this.getPermissions() + ")";
    }

    public RoleBasedAccessRecord(final String serviceName, final String resourceType, final String resourceName, final String roleName, final JsonPointer attribute, final Set<Permission> permissions) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.roleName = roleName;
        this.attribute = attribute;
        this.permissions = permissions;
    }
}
