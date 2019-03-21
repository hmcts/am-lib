package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(Scope.Benchmark)
public class SpikeSetup {

    public ExplicitAccessGrant grant;
    public AccessManagementService service = new AccessManagementService("jdbc:postgresql://localhost:5433/am", "amuser", "ampass");

    @Setup(Level.Invocation)
    public void setup() {

        String resourceId = UUID.randomUUID().toString();
        String accessorId = UUID.randomUUID().toString();

        Map<JsonPointer, Set<Permission>> attributePermissions =
            ImmutableMap.of(
                JsonPointer.valueOf("/" + UUID.randomUUID().toString()),
                Stream.of(Permission.READ).collect(Collectors.toSet()));


        grant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessType("EXPLICIT")
            .serviceName("Service 1")
            .resourceType("Resource Type 1")
            .resourceName("resource")
            .attributePermissions(attributePermissions)
            .securityClassification(SecurityClassification.PUBLIC)
            .build();
    }
}
