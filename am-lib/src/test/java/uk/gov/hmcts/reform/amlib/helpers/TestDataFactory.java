package uk.gov.hmcts.reform.amlib.helpers;

import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {
        //NO-OP
    }

    public static ExplicitAccessRecord createRecord(String resourceId,
                                                    String accessorId,
                                                    Set<Permission> explicitPermissions
                                                    ) {
        return ExplicitAccessRecord.builder()
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

    public static ExplicitAccessRecord createAttributeRecord(String resourceId,
                                                             String accessorId,
                                                             Set<Permission> explicitPermissions,
                                                             String attribute) {
        return ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .explicitPermissions(explicitPermissions)
            .accessType(TestConstants.ACCESS_TYPE)
            .serviceName(TestConstants.SERVICE_NAME)
            .resourceType(TestConstants.RESOURCE_TYPE)
            .resourceName(TestConstants.RESOURCE_NAME)
            .attribute(attribute)
            .securityClassification(TestConstants.SECURITY_CLASSIFICATION)
            .build();
    }

    public static ExplicitAccessMetadata createMetadata(String resourceId) {
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
