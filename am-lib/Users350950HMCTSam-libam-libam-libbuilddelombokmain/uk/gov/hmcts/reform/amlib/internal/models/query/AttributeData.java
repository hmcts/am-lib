package uk.gov.hmcts.reform.amlib.internal.models.query;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import java.util.Set;

public final class AttributeData {
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private final Set<Permission> permissions;


    public static class AttributeDataBuilder {
        private JsonPointer attribute;
        private SecurityClassification defaultSecurityClassification;
        private Set<Permission> permissions;

        AttributeDataBuilder() {
        }

        public AttributeDataBuilder attribute(final JsonPointer attribute) {
            this.attribute = attribute;
            return this;
        }

        public AttributeDataBuilder defaultSecurityClassification(final SecurityClassification defaultSecurityClassification) {
            this.defaultSecurityClassification = defaultSecurityClassification;
            return this;
        }

        public AttributeDataBuilder permissions(final Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public AttributeData build() {
            return new AttributeData(attribute, defaultSecurityClassification, permissions);
        }

        @Override
        public String toString() {
            return "AttributeData.AttributeDataBuilder(attribute=" + this.attribute + ", defaultSecurityClassification=" + this.defaultSecurityClassification + ", permissions=" + this.permissions + ")";
        }
    }

    public static AttributeDataBuilder builder() {
        return new AttributeDataBuilder();
    }

    public JsonPointer getAttribute() {
        return this.attribute;
    }

    public SecurityClassification getDefaultSecurityClassification() {
        return this.defaultSecurityClassification;
    }

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AttributeData)) return false;
        final AttributeData other = (AttributeData) o;
        final Object this$attribute = this.getAttribute();
        final Object other$attribute = other.getAttribute();
        if (this$attribute == null ? other$attribute != null : !this$attribute.equals(other$attribute)) return false;
        final Object this$defaultSecurityClassification = this.getDefaultSecurityClassification();
        final Object other$defaultSecurityClassification = other.getDefaultSecurityClassification();
        if (this$defaultSecurityClassification == null ? other$defaultSecurityClassification != null : !this$defaultSecurityClassification.equals(other$defaultSecurityClassification)) return false;
        final Object this$permissions = this.getPermissions();
        final Object other$permissions = other.getPermissions();
        if (this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $attribute = this.getAttribute();
        result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
        final Object $defaultSecurityClassification = this.getDefaultSecurityClassification();
        result = result * PRIME + ($defaultSecurityClassification == null ? 43 : $defaultSecurityClassification.hashCode());
        final Object $permissions = this.getPermissions();
        result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AttributeData(attribute=" + this.getAttribute() + ", defaultSecurityClassification=" + this.getDefaultSecurityClassification() + ", permissions=" + this.getPermissions() + ")";
    }

    public AttributeData(final JsonPointer attribute, final SecurityClassification defaultSecurityClassification, final Set<Permission> permissions) {
        this.attribute = attribute;
        this.defaultSecurityClassification = defaultSecurityClassification;
        this.permissions = permissions;
    }
}
