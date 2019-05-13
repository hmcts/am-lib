package uk.gov.hmcts.reform.amlib.internal.models;

import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

public final class Role {
    private final String roleName;
    private final RoleType roleType;
    private final SecurityClassification securityClassification;
    private final AccessType accessType;


    public static class RoleBuilder {
        private String roleName;
        private RoleType roleType;
        private SecurityClassification securityClassification;
        private AccessType accessType;

        RoleBuilder() {
        }

        public RoleBuilder roleName(final String roleName) {
            this.roleName = roleName;
            return this;
        }

        public RoleBuilder roleType(final RoleType roleType) {
            this.roleType = roleType;
            return this;
        }

        public RoleBuilder securityClassification(final SecurityClassification securityClassification) {
            this.securityClassification = securityClassification;
            return this;
        }

        public RoleBuilder accessType(final AccessType accessType) {
            this.accessType = accessType;
            return this;
        }

        public Role build() {
            return new Role(roleName, roleType, securityClassification, accessType);
        }

        @Override
        public String toString() {
            return "Role.RoleBuilder(roleName=" + this.roleName + ", roleType=" + this.roleType + ", securityClassification=" + this.securityClassification + ", accessType=" + this.accessType + ")";
        }
    }

    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public String getRoleName() {
        return this.roleName;
    }

    public RoleType getRoleType() {
        return this.roleType;
    }

    public SecurityClassification getSecurityClassification() {
        return this.securityClassification;
    }

    public AccessType getAccessType() {
        return this.accessType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Role)) return false;
        final Role other = (Role) o;
        final Object this$roleName = this.getRoleName();
        final Object other$roleName = other.getRoleName();
        if (this$roleName == null ? other$roleName != null : !this$roleName.equals(other$roleName)) return false;
        final Object this$roleType = this.getRoleType();
        final Object other$roleType = other.getRoleType();
        if (this$roleType == null ? other$roleType != null : !this$roleType.equals(other$roleType)) return false;
        final Object this$securityClassification = this.getSecurityClassification();
        final Object other$securityClassification = other.getSecurityClassification();
        if (this$securityClassification == null ? other$securityClassification != null : !this$securityClassification.equals(other$securityClassification)) return false;
        final Object this$accessType = this.getAccessType();
        final Object other$accessType = other.getAccessType();
        if (this$accessType == null ? other$accessType != null : !this$accessType.equals(other$accessType)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $roleName = this.getRoleName();
        result = result * PRIME + ($roleName == null ? 43 : $roleName.hashCode());
        final Object $roleType = this.getRoleType();
        result = result * PRIME + ($roleType == null ? 43 : $roleType.hashCode());
        final Object $securityClassification = this.getSecurityClassification();
        result = result * PRIME + ($securityClassification == null ? 43 : $securityClassification.hashCode());
        final Object $accessType = this.getAccessType();
        result = result * PRIME + ($accessType == null ? 43 : $accessType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Role(roleName=" + this.getRoleName() + ", roleType=" + this.getRoleType() + ", securityClassification=" + this.getSecurityClassification() + ", accessType=" + this.getAccessType() + ")";
    }

    public Role(final String roleName, final RoleType roleType, final SecurityClassification securityClassification, final AccessType accessType) {
        this.roleName = roleName;
        this.roleType = roleType;
        this.securityClassification = securityClassification;
        this.accessType = accessType;
    }
}
