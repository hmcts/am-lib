package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public final class ExplicitAccessMetadata {
    @NotBlank
    private final String resourceId;
    @NotNull
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotBlank
    private final String accessorId;
    @NotNull
    private final AccessorType accessorType;
    @NotNull
    private final JsonPointer attribute;
    private final String relationship;

    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    ExplicitAccessMetadata(final String resourceId, final ResourceDefinition resourceDefinition, final String accessorId, final AccessorType accessorType, final JsonPointer attribute, final String relationship) {
        this.resourceId = resourceId;
        this.resourceDefinition = resourceDefinition;
        this.accessorId = accessorId;
        this.accessorType = accessorType;
        this.attribute = attribute;
        this.relationship = relationship;
    }


    public static class ExplicitAccessMetadataBuilder {
        private String resourceId;
        private ResourceDefinition resourceDefinition;
        private String accessorId;
        private AccessorType accessorType;
        private JsonPointer attribute;
        private String relationship;

        ExplicitAccessMetadataBuilder() {
        }

        public ExplicitAccessMetadataBuilder resourceId(final String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public ExplicitAccessMetadataBuilder resourceDefinition(final ResourceDefinition resourceDefinition) {
            this.resourceDefinition = resourceDefinition;
            return this;
        }

        public ExplicitAccessMetadataBuilder accessorId(final String accessorId) {
            this.accessorId = accessorId;
            return this;
        }

        public ExplicitAccessMetadataBuilder accessorType(final AccessorType accessorType) {
            this.accessorType = accessorType;
            return this;
        }

        public ExplicitAccessMetadataBuilder attribute(final JsonPointer attribute) {
            this.attribute = attribute;
            return this;
        }

        public ExplicitAccessMetadataBuilder relationship(final String relationship) {
            this.relationship = relationship;
            return this;
        }

        public ExplicitAccessMetadata build() {
            return new ExplicitAccessMetadata(resourceId, resourceDefinition, accessorId, accessorType, attribute, relationship);
        }

        @Override
        public String toString() {
            return "ExplicitAccessMetadata.ExplicitAccessMetadataBuilder(resourceId=" + this.resourceId + ", resourceDefinition=" + this.resourceDefinition + ", accessorId=" + this.accessorId + ", accessorType=" + this.accessorType + ", attribute=" + this.attribute + ", relationship=" + this.relationship + ")";
        }
    }

    public static ExplicitAccessMetadataBuilder builder() {
        return new ExplicitAccessMetadataBuilder();
    }

    public String getResourceId() {
        return this.resourceId;
    }

    public ResourceDefinition getResourceDefinition() {
        return this.resourceDefinition;
    }

    public String getAccessorId() {
        return this.accessorId;
    }

    public AccessorType getAccessorType() {
        return this.accessorType;
    }

    public JsonPointer getAttribute() {
        return this.attribute;
    }

    public String getRelationship() {
        return this.relationship;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ExplicitAccessMetadata)) return false;
        final ExplicitAccessMetadata other = (ExplicitAccessMetadata) o;
        final Object this$resourceId = this.getResourceId();
        final Object other$resourceId = other.getResourceId();
        if (this$resourceId == null ? other$resourceId != null : !this$resourceId.equals(other$resourceId)) return false;
        final Object this$resourceDefinition = this.getResourceDefinition();
        final Object other$resourceDefinition = other.getResourceDefinition();
        if (this$resourceDefinition == null ? other$resourceDefinition != null : !this$resourceDefinition.equals(other$resourceDefinition)) return false;
        final Object this$accessorId = this.getAccessorId();
        final Object other$accessorId = other.getAccessorId();
        if (this$accessorId == null ? other$accessorId != null : !this$accessorId.equals(other$accessorId)) return false;
        final Object this$accessorType = this.getAccessorType();
        final Object other$accessorType = other.getAccessorType();
        if (this$accessorType == null ? other$accessorType != null : !this$accessorType.equals(other$accessorType)) return false;
        final Object this$attribute = this.getAttribute();
        final Object other$attribute = other.getAttribute();
        if (this$attribute == null ? other$attribute != null : !this$attribute.equals(other$attribute)) return false;
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
        final Object $accessorId = this.getAccessorId();
        result = result * PRIME + ($accessorId == null ? 43 : $accessorId.hashCode());
        final Object $accessorType = this.getAccessorType();
        result = result * PRIME + ($accessorType == null ? 43 : $accessorType.hashCode());
        final Object $attribute = this.getAttribute();
        result = result * PRIME + ($attribute == null ? 43 : $attribute.hashCode());
        final Object $relationship = this.getRelationship();
        result = result * PRIME + ($relationship == null ? 43 : $relationship.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ExplicitAccessMetadata(resourceId=" + this.getResourceId() + ", resourceDefinition=" + this.getResourceDefinition() + ", accessorId=" + this.getAccessorId() + ", accessorType=" + this.getAccessorType() + ", attribute=" + this.getAttribute() + ", relationship=" + this.getRelationship() + ")";
    }
}
