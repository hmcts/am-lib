package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import java.util.Map;
import java.util.Set;

public final class RolePermissions {
    private final Map<JsonPointer, Set<Permission>> permissions;
    private final Map<JsonPointer, SecurityClassification> securityClassifications;
    private final SecurityClassification roleSecurityClassification;
    private final AccessType roleAccessType;

    RolePermissions(final Map<JsonPointer, Set<Permission>> permissions, final Map<JsonPointer, SecurityClassification> securityClassifications, final SecurityClassification roleSecurityClassification, final AccessType roleAccessType) {
        this.permissions = permissions;
        this.securityClassifications = securityClassifications;
        this.roleSecurityClassification = roleSecurityClassification;
        this.roleAccessType = roleAccessType;
    }


    public static class RolePermissionsBuilder {
        private Map<JsonPointer, Set<Permission>> permissions;
        private Map<JsonPointer, SecurityClassification> securityClassifications;
        private SecurityClassification roleSecurityClassification;
        private AccessType roleAccessType;

        RolePermissionsBuilder() {
        }

        public RolePermissionsBuilder permissions(final Map<JsonPointer, Set<Permission>> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RolePermissionsBuilder securityClassifications(final Map<JsonPointer, SecurityClassification> securityClassifications) {
            this.securityClassifications = securityClassifications;
            return this;
        }

        public RolePermissionsBuilder roleSecurityClassification(final SecurityClassification roleSecurityClassification) {
            this.roleSecurityClassification = roleSecurityClassification;
            return this;
        }

        public RolePermissionsBuilder roleAccessType(final AccessType roleAccessType) {
            this.roleAccessType = roleAccessType;
            return this;
        }

        public RolePermissions build() {
            return new RolePermissions(permissions, securityClassifications, roleSecurityClassification, roleAccessType);
        }

        @Override
        public String toString() {
            return "RolePermissions.RolePermissionsBuilder(permissions=" + this.permissions + ", securityClassifications=" + this.securityClassifications + ", roleSecurityClassification=" + this.roleSecurityClassification + ", roleAccessType=" + this.roleAccessType + ")";
        }
    }

    public static RolePermissionsBuilder builder() {
        return new RolePermissionsBuilder();
    }

    public Map<JsonPointer, Set<Permission>> getPermissions() {
        return this.permissions;
    }

    public Map<JsonPointer, SecurityClassification> getSecurityClassifications() {
        return this.securityClassifications;
    }

    public SecurityClassification getRoleSecurityClassification() {
        return this.roleSecurityClassification;
    }

    public AccessType getRoleAccessType() {
        return this.roleAccessType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RolePermissions)) return false;
        final RolePermissions other = (RolePermissions) o;
        final Object this$permissions = this.getPermissions();
        final Object other$permissions = other.getPermissions();
        if (this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions)) return false;
        final Object this$securityClassifications = this.getSecurityClassifications();
        final Object other$securityClassifications = other.getSecurityClassifications();
        if (this$securityClassifications == null ? other$securityClassifications != null : !this$securityClassifications.equals(other$securityClassifications)) return false;
        final Object this$roleSecurityClassification = this.getRoleSecurityClassification();
        final Object other$roleSecurityClassification = other.getRoleSecurityClassification();
        if (this$roleSecurityClassification == null ? other$roleSecurityClassification != null : !this$roleSecurityClassification.equals(other$roleSecurityClassification)) return false;
        final Object this$roleAccessType = this.getRoleAccessType();
        final Object other$roleAccessType = other.getRoleAccessType();
        if (this$roleAccessType == null ? other$roleAccessType != null : !this$roleAccessType.equals(other$roleAccessType)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $permissions = this.getPermissions();
        result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
        final Object $securityClassifications = this.getSecurityClassifications();
        result = result * PRIME + ($securityClassifications == null ? 43 : $securityClassifications.hashCode());
        final Object $roleSecurityClassification = this.getRoleSecurityClassification();
        result = result * PRIME + ($roleSecurityClassification == null ? 43 : $roleSecurityClassification.hashCode());
        final Object $roleAccessType = this.getRoleAccessType();
        result = result * PRIME + ($roleAccessType == null ? 43 : $roleAccessType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RolePermissions(permissions=" + this.getPermissions() + ", securityClassifications=" + this.getSecurityClassifications() + ", roleSecurityClassification=" + this.getRoleSecurityClassification() + ", roleAccessType=" + this.getRoleAccessType() + ")";
    }
}
