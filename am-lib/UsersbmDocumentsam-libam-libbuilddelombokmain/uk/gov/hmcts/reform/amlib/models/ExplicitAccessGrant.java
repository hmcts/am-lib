package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public final class ExplicitAccessGrant {
    @NotBlank
    private final String resourceId;
    @NotNull
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotEmpty
    private final Set<@NotBlank String> accessorIds;
    @NotNull
    private final AccessorType accessorType;
    @NotEmpty
    private final Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions;
    @NotBlank
    private final String relationship;

    ExplicitAccessGrant(final String resourceId, final ResourceDefinition resourceDefinition, final Set<@NotBlank String> accessorIds, final AccessorType accessorType, final Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions, final String relationship) {
        this.resourceId = resourceId;
        this.resourceDefinition = resourceDefinition;
        this.accessorIds = accessorIds;
        this.accessorType = accessorType;
        this.attributePermissions = attributePermissions;
        this.relationship = relationship;
    }


    public static class ExplicitAccessGrantBuilder {
        private String resourceId;
        private ResourceDefinition resourceDefinition;
        private Set<@NotBlank String> accessorIds;
        private AccessorType accessorType;
        private Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions;
        private String relationship;

        ExplicitAccessGrantBuilder() {
        }

        public ExplicitAccessGrantBuilder resourceId(final String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public ExplicitAccessGrantBuilder resourceDefinition(final ResourceDefinition resourceDefinition) {
            this.resourceDefinition = resourceDefinition;
            return this;
        }

        public ExplicitAccessGrantBuilder accessorIds(final Set<@NotBlank String> accessorIds) {
            this.accessorIds = accessorIds;
            return this;
        }

        public ExplicitAccessGrantBuilder accessorType(final AccessorType accessorType) {
            this.accessorType = accessorType;
            return this;
        }

        public ExplicitAccessGrantBuilder attributePermissions(final Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions) {
            this.attributePermissions = attributePermissions;
            return this;
        }

        public ExplicitAccessGrantBuilder relationship(final String relationship) {
            this.relationship = relationship;
            return this;
        }

        public ExplicitAccessGrant build() {
            return new ExplicitAccessGrant(resourceId, resourceDefinition, accessorIds, accessorType, attributePermissions, relationship);
        }

        @Override
        public String toString() {
            return "ExplicitAccessGrant.ExplicitAccessGrantBuilder(resourceId=" + this.resourceId + ", resourceDefinition=" + this.resourceDefinition + ", accessorIds=" + this.accessorIds + ", accessorType=" + this.accessorType + ", attributePermissions=" + this.attributePermissions + ", relationship=" + this.relationship + ")";
        }
    }

    public static ExplicitAccessGrantBuilder builder() {
        return new ExplicitAccessGrantBuilder();
    }

    public String getResourceId() {
        return this.resourceId;
    }

    public ResourceDefinition getResourceDefinition() {
        return this.resourceDefinition;
    }

    public Set<@NotBlank String> getAccessorIds() {
        return this.accessorIds;
    }

    public AccessorType getAccessorType() {
        return this.accessorType;
    }

    public Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> getAttributePermissions() {
        return this.attributePermissions;
    }

    public String getRelationship() {
        return this.relationship;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ExplicitAccessGrant)) return false;
        final ExplicitAccessGrant other = (ExplicitAccessGrant) o;
        final Object this$resourceId = this.getResourceId();
        final Object other$resourceId = other.getResourceId();
        if (this$resourceId == null ? other$resourceId != null : !this$resourceId.equals(other$resourceId)) return false;
        final Object this$resourceDefinition = this.getResourceDefinition();
        final Object other$resourceDefinition = other.getResourceDefinition();
        if (this$resourceDefinition == null ? other$resourceDefinition != null : !this$resourceDefinition.equals(other$resourceDefinition)) return false;
        final Object this$accessorIds = this.getAccessorIds();
        final Object other$accessorIds = other.getAccessorIds();
        if (this$accessorIds == null ? other$accessorIds != null : !this$accessorIds.equals(other$accessorIds)) return false;
        final Object this$accessorType = this.getAccessorType();
        final Object other$accessorType = other.getAccessorType();
        if (this$accessorType == null ? other$accessorType != null : !this$accessorType.equals(other$accessorType)) return false;
        final Object this$attributePermissions = this.getAttributePermissions();
        final Object other$attributePermissions = other.getAttributePermissions();
        if (this$attributePermissions == null ? other$attributePermissions != null : !this$attributePermissions.equals(other$attributePermissions)) return false;
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
        final Object $resourceDefinition = this.getResourceDefinition();
        result = result * PRIME + ($resourceDefinition == null ? 43 : $resourceDefinition.hashCode());
        final Object $accessorIds = this.getAccessorIds();
        result = result * PRIME + ($accessorIds == null ? 43 : $accessorIds.hashCode());
        final Object $accessorType = this.getAccessorType();
        result = result * PRIME + ($accessorType == null ? 43 : $accessorType.hashCode());
        final Object $attributePermissions = this.getAttributePermissions();
        result = result * PRIME + ($attributePermissions == null ? 43 : $attributePermissions.hashCode());
        final Object $relationship = this.getRelationship();
        result = result * PRIME + ($relationship == null ? 43 : $relationship.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ExplicitAccessGrant(resourceId=" + this.getResourceId() + ", resourceDefinition=" + this.getResourceDefinition() + ", accessorIds=" + this.getAccessorIds() + ", accessorType=" + this.getAccessorType() + ", attributePermissions=" + this.getAttributePermissions() + ", relationship=" + this.getRelationship() + ")";
    }
}
