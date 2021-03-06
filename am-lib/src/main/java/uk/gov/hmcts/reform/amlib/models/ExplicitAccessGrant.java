package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.AuditAction;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonDeserialize(builder = ExplicitAccessGrant.ExplicitAccessGrantBuilder.class)
public final class ExplicitAccessGrant {
    @NotBlank
    private final String resourceId;
    @NotNull
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotEmpty
    private final Set<@NotBlank String> accessorIds;
    @NotNull
    private final AccessorType accessorType;
    @NotEmpty
    @ApiModelProperty(
        name = "attributePermissions",
        example  = "{\"/attribute1\": [\"CREATE\",\"READ\",\"UPDATE\"],\"/attribute2\": [\"READ\",\"UPDATE\"],"
            + "\"/attribute3\": [\"READ\"]}",
        dataType = "Map[String,Set]")
    private final Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions;

    private final String relationship;

    //Auditing columns are marked @JsonIgnore
    @JsonIgnore
    private Instant lastUpdate;

    @Setter
    @JsonIgnore
    private String callingServiceName;

    @JsonIgnore
    @Setter
    private String changedBy;

    @JsonIgnore
    private AuditAction action;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExplicitAccessGrantBuilder {
        // Lombok will add constructor, setters, build method
    }
}
