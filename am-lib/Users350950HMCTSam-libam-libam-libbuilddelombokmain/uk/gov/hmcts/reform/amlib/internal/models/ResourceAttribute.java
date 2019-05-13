package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

public final class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;

    public String getAttributeAsString() {
        return attribute.toString();
    }


    public static class ResourceAttributeBuilder {
        private String serviceName;
        private String resourceType;
        private String resourceName;
        private JsonPointer attribute;
        private SecurityClassification defaultSecurityClassification;

        ResourceAttributeBuilder() {
        }

        public ResourceAttributeBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ResourceAttributeBuilder resourceType(final String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public ResourceAttributeBuilder resourceName(final String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public ResourceAttributeBuilder attribute(final JsonPointer attribute) {
            this.attribute = attribute;
            return this;
        }

        public ResourceAttributeBuilder defaultSecurityClassification(final SecurityClassification defaultSecurityClassification) {
            this.defaultSecurityClassification = defaultSecurityClassification;
            return this;
        }

        public ResourceAttribute build() {
            return new ResourceAttribute(serviceName, resourceType, resourceName, attribute, defaultSecurityClassification);
        }

        @Override
        public String toString() {
            return "ResourceAttribute.ResourceAttributeBuilder(serviceName=" + this.serviceName + ", resourceType=" + this.resourceType + ", resourceName=" + this.resourceName + ", attribute=" + this.attribute + ", defaultSecurityClassification=" + this.defaultSecurityClassification + ")";
        }
    }

    public static ResourceAttributeBuilder builder() {
        return new ResourceAttributeBuilder();
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

    public SecurityClassification getDefaultSecurityClassification() {
        return this.defaultSecurityClassification;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ResourceAttribute)) return false;
        final ResourceAttribute other = (ResourceAttribute) o;
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
        final Object this$defaultSecurityClassification = this.getDefaultSecurityClassification();
        final Object other$defaultSecurityClassification = other.getDefaultSecurityClassification();
        if (this$defaultSecurityClassification == null ? other$defaultSecurityClassification != null : !this$defaultSecurityClassification.equals(other$defaultSecurityClassification)) return false;
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
        final Object $attribute = this.getAttribute();
        result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
        final Object $defaultSecurityClassification = this.getDefaultSecurityClassification();
        result = result * PRIME + ($defaultSecurityClassification == null ? 43 : $defaultSecurityClassification.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ResourceAttribute(serviceName=" + this.getServiceName() + ", resourceType=" + this.getResourceType() + ", resourceName=" + this.getResourceName() + ", attribute=" + this.getAttribute() + ", defaultSecurityClassification=" + this.getDefaultSecurityClassification() + ")";
    }

    public ResourceAttribute(final String serviceName, final String resourceType, final String resourceName, final JsonPointer attribute, final SecurityClassification defaultSecurityClassification) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
        this.defaultSecurityClassification = defaultSecurityClassification;
    }
}
