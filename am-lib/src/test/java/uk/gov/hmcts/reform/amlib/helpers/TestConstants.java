package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class TestConstants {
    public static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");
    public static final JsonNode DATA = JsonNodeFactory.instance.objectNode()
        .put("name", "John")
        .set("address", JsonNodeFactory.instance.objectNode()
            .put("city", "London"));

    public static final String CALLING_SERVICE_NAME_FOR_INSERTION = "integration-test-insert";

    public static final String CALLING_SERVICE_NAME_FOR_UPDATES = "integration-test-update";

    public static final String CALLING_SERVICE_NAME_FOR_REVOKE = "integration-test-revoke";

    public static final String CHANGED_BY_NAME_FOR_INSERTION = "insert-user";

    public static final String CHANGED_BY_NAME_FOR_UPDATE = "update-user";

    public static final String CHANGED_BY_NAME_FOR_REVOKE = "revoke-user";

    private TestConstants() {
        //NO-OP
    }
}
