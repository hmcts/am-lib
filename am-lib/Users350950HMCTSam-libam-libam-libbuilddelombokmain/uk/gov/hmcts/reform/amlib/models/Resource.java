package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public final class Resource {
    @NotBlank
    private final String id;
    @NotNull
    @Valid
    private final ResourceDefinition definition;
    @NotNull
    private final JsonNode data;

    Resource(final String id, final ResourceDefinition definition, final JsonNode data) {
        this.id = id;
        this.definition = definition;
        this.data = data;
    }


    public static class ResourceBuilder {
        private String id;
        private ResourceDefinition definition;
        private JsonNode data;

        ResourceBuilder() {
        }

        public ResourceBuilder id(final String id) {
            this.id = id;
            return this;
        }

        public ResourceBuilder definition(final ResourceDefinition definition) {
            this.definition = definition;
            return this;
        }

        public ResourceBuilder data(final JsonNode data) {
            this.data = data;
            return this;
        }

        public Resource build() {
            return new Resource(id, definition, data);
        }

        @Override
        public String toString() {
            return "Resource.ResourceBuilder(id=" + this.id + ", definition=" + this.definition + ", data=" + this.data + ")";
        }
    }

    public static ResourceBuilder builder() {
        return new ResourceBuilder();
    }

    public String getId() {
        return this.id;
    }

    public ResourceDefinition getDefinition() {
        return this.definition;
    }

    public JsonNode getData() {
        return this.data;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Resource)) return false;
        final Resource other = (Resource) o;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$definition = this.getDefinition();
        final Object other$definition = other.getDefinition();
        if (this$definition == null ? other$definition != null : !this$definition.equals(other$definition)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $definition = this.getDefinition();
        result = result * PRIME + ($definition == null ? 43 : $definition.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Resource(id=" + this.getId() + ", definition=" + this.getDefinition() + ", data=" + this.getData() + ")";
    }
}
