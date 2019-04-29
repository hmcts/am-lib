package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@SuppressWarnings("LineLength")
public final class DefaultPermissionGrant {
    @NotNull
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotBlank
    private final String roleName;
    @NotEmpty
    private final Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> attributePermissions;

    DefaultPermissionGrant(final ResourceDefinition resourceDefinition, final String roleName, final Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> attributePermissions) {
        this.resourceDefinition = resourceDefinition;
        this.roleName = roleName;
        this.attributePermissions = attributePermissions;
    }


    public static class DefaultPermissionGrantBuilder {
        private ResourceDefinition resourceDefinition;
        private String roleName;
        private Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> attributePermissions;

        DefaultPermissionGrantBuilder() {
        }

        public DefaultPermissionGrantBuilder resourceDefinition(final ResourceDefinition resourceDefinition) {
            this.resourceDefinition = resourceDefinition;
            return this;
        }

        public DefaultPermissionGrantBuilder roleName(final String roleName) {
            this.roleName = roleName;
            return this;
        }

        public DefaultPermissionGrantBuilder attributePermissions(final Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> attributePermissions) {
            this.attributePermissions = attributePermissions;
            return this;
        }

        public DefaultPermissionGrant build() {
            return new DefaultPermissionGrant(resourceDefinition, roleName, attributePermissions);
        }

        @Override
        public String toString() {
            return "DefaultPermissionGrant.DefaultPermissionGrantBuilder(resourceDefinition=" + this.resourceDefinition + ", roleName=" + this.roleName + ", attributePermissions=" + this.attributePermissions + ")";
        }
    }

    public static DefaultPermissionGrantBuilder builder() {
        return new DefaultPermissionGrantBuilder();
    }

    public ResourceDefinition getResourceDefinition() {
        return this.resourceDefinition;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> getAttributePermissions() {
        return this.attributePermissions;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DefaultPermissionGrant)) return false;
        final DefaultPermissionGrant other = (DefaultPermissionGrant) o;
        final Object this$resourceDefinition = this.getResourceDefinition();
        final Object other$resourceDefinition = other.getResourceDefinition();
        if (this$resourceDefinition == null ? other$resourceDefinition != null : !this$resourceDefinition.equals(other$resourceDefinition)) return false;
        final Object this$roleName = this.getRoleName();
        final Object other$roleName = other.getRoleName();
        if (this$roleName == null ? other$roleName != null : !this$roleName.equals(other$roleName)) return false;
        final Object this$attributePermissions = this.getAttributePermissions();
        final Object other$attributePermissions = other.getAttributePermissions();
        if (this$attributePermissions == null ? other$attributePermissions != null : !this$attributePermissions.equals(other$attributePermissions)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $resourceDefinition = this.getResourceDefinition();
        result = result * PRIME + ($resourceDefinition == null ? 43 : $resourceDefinition.hashCode());
        final Object $roleName = this.getRoleName();
        result = result * PRIME + ($roleName == null ? 43 : $roleName.hashCode());
        final Object $attributePermissions = this.getAttributePermissions();
        result = result * PRIME + ($attributePermissions == null ? 43 : $attributePermissions.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "DefaultPermissionGrant(resourceDefinition=" + this.getResourceDefinition() + ", roleName=" + this.getRoleName() + ", attributePermissions=" + this.getAttributePermissions() + ")";
    }
}
