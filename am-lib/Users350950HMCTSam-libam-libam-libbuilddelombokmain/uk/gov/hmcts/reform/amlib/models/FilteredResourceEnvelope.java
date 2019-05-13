package uk.gov.hmcts.reform.amlib.models;

import java.util.Set;

public final class FilteredResourceEnvelope {
    private final Resource resource;
    private final AccessEnvelope access;
    private final Set<String> relationships;

    FilteredResourceEnvelope(final Resource resource, final AccessEnvelope access, final Set<String> relationships) {
        this.resource = resource;
        this.access = access;
        this.relationships = relationships;
    }


    public static class FilteredResourceEnvelopeBuilder {
        private Resource resource;
        private AccessEnvelope access;
        private Set<String> relationships;

        FilteredResourceEnvelopeBuilder() {
        }

        public FilteredResourceEnvelopeBuilder resource(final Resource resource) {
            this.resource = resource;
            return this;
        }

        public FilteredResourceEnvelopeBuilder access(final AccessEnvelope access) {
            this.access = access;
            return this;
        }

        public FilteredResourceEnvelopeBuilder relationships(final Set<String> relationships) {
            this.relationships = relationships;
            return this;
        }

        public FilteredResourceEnvelope build() {
            return new FilteredResourceEnvelope(resource, access, relationships);
        }

        @Override
        public String toString() {
            return "FilteredResourceEnvelope.FilteredResourceEnvelopeBuilder(resource=" + this.resource + ", access=" + this.access + ", relationships=" + this.relationships + ")";
        }
    }

    public static FilteredResourceEnvelopeBuilder builder() {
        return new FilteredResourceEnvelopeBuilder();
    }

    public Resource getResource() {
        return this.resource;
    }

    public AccessEnvelope getAccess() {
        return this.access;
    }

    public Set<String> getRelationships() {
        return this.relationships;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FilteredResourceEnvelope)) return false;
        final FilteredResourceEnvelope other = (FilteredResourceEnvelope) o;
        final Object this$resource = this.getResource();
        final Object other$resource = other.getResource();
        if (this$resource == null ? other$resource != null : !this$resource.equals(other$resource)) return false;
        final Object this$access = this.getAccess();
        final Object other$access = other.getAccess();
        if (this$access == null ? other$access != null : !this$access.equals(other$access)) return false;
        final Object this$relationships = this.getRelationships();
        final Object other$relationships = other.getRelationships();
        if (this$relationships == null ? other$relationships != null : !this$relationships.equals(other$relationships)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $resource = this.getResource();
        result = result * PRIME + ($resource == null ? 43 : $resource.hashCode());
        final Object $access = this.getAccess();
        result = result * PRIME + ($access == null ? 43 : $access.hashCode());
        final Object $relationships = this.getRelationships();
        result = result * PRIME + ($relationships == null ? 43 : $relationships.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "FilteredResourceEnvelope(resource=" + this.getResource() + ", access=" + this.getAccess() + ", relationships=" + this.getRelationships() + ")";
    }
}
