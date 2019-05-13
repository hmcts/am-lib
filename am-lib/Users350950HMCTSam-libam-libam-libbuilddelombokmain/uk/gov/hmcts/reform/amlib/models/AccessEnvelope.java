package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import java.util.Map;
import java.util.Set;

public final class AccessEnvelope {
    private final Map<JsonPointer, Set<Permission>> permissions;
    private final AccessType accessType;

    AccessEnvelope(final Map<JsonPointer, Set<Permission>> permissions, final AccessType accessType) {
        this.permissions = permissions;
        this.accessType = accessType;
    }


    public static class AccessEnvelopeBuilder {
        private Map<JsonPointer, Set<Permission>> permissions;
        private AccessType accessType;

        AccessEnvelopeBuilder() {
        }

        public AccessEnvelopeBuilder permissions(final Map<JsonPointer, Set<Permission>> permissions) {
            this.permissions = permissions;
            return this;
        }

        public AccessEnvelopeBuilder accessType(final AccessType accessType) {
            this.accessType = accessType;
            return this;
        }

        public AccessEnvelope build() {
            return new AccessEnvelope(permissions, accessType);
        }

        @Override
        public String toString() {
            return "AccessEnvelope.AccessEnvelopeBuilder(permissions=" + this.permissions + ", accessType=" + this.accessType + ")";
        }
    }

    public static AccessEnvelopeBuilder builder() {
        return new AccessEnvelopeBuilder();
    }

    public Map<JsonPointer, Set<Permission>> getPermissions() {
        return this.permissions;
    }

    public AccessType getAccessType() {
        return this.accessType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AccessEnvelope)) return false;
        final AccessEnvelope other = (AccessEnvelope) o;
        final Object this$permissions = this.getPermissions();
        final Object other$permissions = other.getPermissions();
        if (this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions)) return false;
        final Object this$accessType = this.getAccessType();
        final Object other$accessType = other.getAccessType();
        if (this$accessType == null ? other$accessType != null : !this$accessType.equals(other$accessType)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $permissions = this.getPermissions();
        result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
        final Object $accessType = this.getAccessType();
        result = result * PRIME + ($accessType == null ? 43 : $accessType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AccessEnvelope(permissions=" + this.getPermissions() + ", accessType=" + this.getAccessType() + ")";
    }
}
