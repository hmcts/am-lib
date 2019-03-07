package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Data
@Builder
@AllArgsConstructor
public class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification securityClassification;

    @JdbiConstructor
    public ResourceAttribute(String serviceName, String resourceType, String resourceName, String attribute,
                             SecurityClassification defaultSecurityClassification) {
        this(serviceName, resourceType, resourceName, JsonPointer.valueOf(attribute), defaultSecurityClassification);
    }

    public String getAttributeAsString() {
        return attribute.toString();
    }
}
