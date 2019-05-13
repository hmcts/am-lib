package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import java.util.Set;

public final class ExplicitAccessRecord implements AttributeAccessDefinition {
    private final String resourceId;
    private final String accessorId;
    private final AccessorType accessorType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;
    private final String relationship;

    @Override
    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }


    public static class ExplicitAccessRecordBuilder {
        private String resourceId;
        private String accessorId;
        private AccessorType accessorType;
        private String serviceName;
        private String resourceType;
        private String resourceName;
        private JsonPointer attribute;
        private Set<Permission> permissions;
        private String relationship;

        ExplicitAccessRecordBuilder() {
        }

        public ExplicitAccessRecordBuilder resourceId(final String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public ExplicitAccessRecordBuilder accessorId(final String accessorId) {
            this.accessorId = accessorId;
            return this;
        }

        public ExplicitAccessRecordBuilder accessorType(final AccessorType accessorType) {
            this.accessorType = accessorType;
            return this;
        }

        public ExplicitAccessRecordBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ExplicitAccessRecordBuilder resourceType(final String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public ExplicitAccessRecordBuilder resourceName(final String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public ExplicitAccessRecordBuilder attribute(final JsonPointer attribute) {
            this.attribute = attribute;
            return this;
        }

        public ExplicitAccessRecordBuilder permissions(final Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public ExplicitAccessRecordBuilder relationship(final String relationship) {
            this.relationship = relationship;
            return this;
        }

        public ExplicitAccessRecord build() {
            return new ExplicitAccessRecord(resourceId, accessorId, accessorType, serviceName, resourceType, resourceName, attribute, permissions, relationship);
        }

        @Override
        public String toString() {
            return "ExplicitAccessRecord.ExplicitAccessRecordBuilder(resourceId=" + this.resourceId + ", accessorId=" + this.accessorId + ", accessorType=" + this.accessorType + ", serviceName=" + this.serviceName + ", resourceType=" + this.resourceType + ", resourceName=" + this.resourceName + ", attribute=" + this.attribute + ", permissions=" + this.permissions + ", relationship=" + this.relationship + ")";
        }
    }

    public static ExplicitAccessRecordBuilder builder() {
        return new ExplicitAccessRecordBuilder();
    }

    public String getResourceId() {
        return this.resourceId;
    }

    public String getAccessorId() {
        return this.accessorId;
    }

    public AccessorType getAccessorType() {
        return this.accessorType;
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

    public JsonPointer getAttribute() {
        return this.attribute;
    }

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    public String getRelationship() {
        return this.relationship;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ExplicitAccessRecord)) return false;
        final ExplicitAccessRecord other = (ExplicitAccessRecord) o;
        final Object this$resourceId = this.getResourceId();
        final Object other$resourceId = other.getResourceId();
        if (this$resourceId == null ? other$resourceId != null : !this$resourceId.equals(other$resourceId)) return false;
        final Object this$accessorId = this.getAccessorId();
        final Object other$accessorId = other.getAccessorId();
        if (this$accessorId == null ? other$accessorId != null : !this$accessorId.equals(other$accessorId)) return false;
        final Object this$accessorType = this.getAccessorType();
        final Object other$accessorType = other.getAccessorType();
        if (this$accessorType == null ? other$accessorType != null : !this$accessorType.equals(other$accessorType)) return false;
        final Object this$serviceName = this.getServiceName();
        final Object other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false;
        final Object this$resourceType = this.getResourceType();
        final Object other$resourceType = other.getResourceType();
        if (this$resourceType == null ? other$resourceType != null : !this$resourceType.equals(other$resourceType)) return false;
        final Object this$resourceName = this.getResourceName();
        final Object other$resourceName = other.getResourceName();
        if (this$resourceName == null ? other$resourceName != null : !this$resourceName.equals(other$resourceName)) return false;
        final Object this$attribute = this.getAttribute();
        final Object other$attribute = other.getAttribute();
        if (this$attribute == null ? other$attribute != null : !this$attribute.equals(other$attribute)) return false;
        final Object this$permissions = this.getPermissions();
        final Object other$permissions = other.getPermissions();
        if (this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions)) return false;
        final Object this$relationship = this.getRelationship();
        final Object other$relationship = other.getRelationship();
        if (this$relationship == null ? other$relationship != null : !this$relationship.equals(other$relationship)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $resourceId = this.getResourceId();
        result = result * PRIME + ($resourceId == null ? 43 : $resourceId.hashCode());
        final Object $accessorId = this.getAccessorId();
        result = result * PRIME + ($accessorId == null ? 43 : $accessorId.hashCode());
        final Object $accessorType = this.getAccessorType();
        result = result * PRIME + ($accessorType == null ? 43 : $accessorType.hashCode());
        final Object $serviceName = this.getServiceName();
        result = result * PRIME + ($serviceName == null ? 43 : $serviceName.hashCode());
        final Object $resourceType = this.getResourceType();
        result = result * PRIME + ($resourceType == null ? 43 : $resourceType.hashCode());
        final Object $resourceName = this.getResourceName();
        result = result * PRIME + ($resourceName == null ? 43 : $resourceName.hashCode());
        final Object $attribute = this.getAttribute();
        result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
        final Object $permissions = this.getPermissions();
        result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
        final Object $relationship = this.getRelationship();
        result = result * PRIME + ($relationship == null ? 43 : $relationship.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ExplicitAccessRecord(resourceId=" + this.getResourceId() + ", accessorId=" + this.getAccessorId() + ", accessorType=" + this.getAccessorType() + ", serviceName=" + this.getServiceName() + ", resourceType=" + this.getResourceType() + ", resourceName=" + this.getResourceName() + ", attribute=" + this.getAttribute() + ", permissions=" + this.getPermissions() + ", relationship=" + this.getRelationship() + ")";
    }

    public ExplicitAccessRecord(final String resourceId, final String accessorId, final AccessorType accessorType, final String serviceName, final String resourceType, final String resourceName, final JsonPointer attribute, final Set<Permission> permissions, final String relationship) {
        this.resourceId = resourceId;
        this.accessorId = accessorId;
        this.accessorType = accessorType;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
        this.permissions = permissions;
        this.relationship = relationship;
    }
}
