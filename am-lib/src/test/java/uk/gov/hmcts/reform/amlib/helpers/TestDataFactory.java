package uk.gov.hmcts.reform.amlib.helpers;

import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.Set;

public class TestDataFactory {

    public static ExplicitAccessRecord createRecord(String resourceId,
                                                    String accessorId,
                                                    Set<Permissions> explicitPermissions) {
        return ExplicitAccessRecord.explicitAccessRecordBuilder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .explicitPermissions(explicitPermissions)
            .accessType(TestConstants.ACCESS_TYPE)
            .serviceName(TestConstants.SERVICE_NAME)
            .resourceType(TestConstants.RESOURCE_TYPE)
            .resourceName(TestConstants.RESOURCE_NAME)
            .attribute("")
            .securityClassification(TestConstants.SECURITY_CLASSIFICATION)
            .build();
    }

    public static ExplicitAccessMetadata removeRecord(String resourceId) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(TestConstants.ACCESSOR_ID)
            .accessType(TestConstants.ACCESS_TYPE)
            .serviceName(TestConstants.SERVICE_NAME)
            .resourceType(TestConstants.RESOURCE_TYPE)
            .resourceName(TestConstants.RESOURCE_NAME)
            .attribute("")
            .build();
    }
}
