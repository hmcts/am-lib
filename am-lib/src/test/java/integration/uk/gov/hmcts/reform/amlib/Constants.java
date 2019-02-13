package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.UPDATE;

public interface Constants {
    String ACCESS_TYPE = "user";
    String SERVICE_NAME = "Service 1";
    String RESOURCE_TYPE = "Resource Type 1";
    String RESOURCE_NAME = "resource";
    String SECURITY_CLASSIFICATION = "Public";
    String ACCESSOR_ID = "a";
    String OTHER_ACCESSOR_ID = "b";
    Set<Permissions> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS = Stream.of(CREATE, READ, UPDATE).collect(toSet());
    JsonNode DATA = JsonNodeFactory.instance.objectNode();
}
