package uk.gov.hmcts.reform.amlib.internal.models;

public final class ResourceDefinition {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;


    public static class ResourceDefinitionBuilder {
        private String serviceName;
        private String resourceType;
        private String resourceName;

        ResourceDefinitionBuilder() {
        }

        public ResourceDefinitionBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ResourceDefinitionBuilder resourceType(final String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public ResourceDefinitionBuilder resourceName(final String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public ResourceDefinition build() {
            return new ResourceDefinition(serviceName, resourceType, resourceName);
        }

        @Override
        public String toString() {
            return "ResourceDefinition.ResourceDefinitionBuilder(serviceName=" + this.serviceName + ", resourceType=" + this.resourceType + ", resourceName=" + this.resourceName + ")";
        }
    }

    public static ResourceDefinitionBuilder builder() {
        return new ResourceDefinitionBuilder();
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ResourceDefinition)) return false;
        final ResourceDefinition other = (ResourceDefinition) o;
        final Object this$serviceName = this.getServiceName();
        final Object other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false;
        final Object this$resourceType = this.getResourceType();
        final Object other$resourceType = other.getResourceType();
        if (this$resourceType == null ? other$resourceType != null : !this$resourceType.equals(other$resourceType)) return false;
        final Object this$resourceName = this.getResourceName();
        final Object other$resourceName = other.getResourceName();
        if (this$resourceName == null ? other$resourceName != null : !this$resourceName.equals(other$resourceName)) return false;
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
        return result;
    }

    @Override
    public String toString() {
        return "ResourceDefinition(serviceName=" + this.getServiceName() + ", resourceType=" + this.getResourceType() + ", resourceName=" + this.getResourceName() + ")";
    }

    public ResourceDefinition(final String serviceName, final String resourceType, final String resourceName) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }
}
