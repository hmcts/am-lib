package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.boot.jdbc.DataSourceBuilder;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

@BenchmarkMode(Mode.Throughput)
public class AccessManagementServiceBenchmarks {

    private static final ObjectMapper mapper = new ObjectMapper();

    @State(Scope.Thread)
    @SuppressWarnings("LineLength")
    public static class RecordState {
        String resourceId = UUID.randomUUID().toString();
        String accessorId = UUID.randomUUID().toString();

        //load in mock FPL case data
        JsonNode inputJson;

        {
            try {
                inputJson = mapper.readTree(ClassLoader.getSystemResource("mock_case_data.json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //initialise service with datasource. Uses springboot DataSourceBuilder.
        AccessManagementService service =
            new AccessManagementService(DataSourceBuilder
                .create()
                .username("amuser@am-lib-test-aat")
                .password("upF#99gf7RAZ?77H")
                .url("jdbc:postgresql://am-lib-test-aat.postgres.database.azure.com:5432/am?user=amuser@am-lib-test-aat&password=upF#99gf7RAZ?77H&sslmode=require")
                .build()
            );

        //need to define role constant and add to AAT (for role based)

        //define resource constant
        Resource resource = Resource.builder()
            .resourceId(resourceId)
            .type(ResourceDefinition.builder()
                .serviceName("Service 1")
                .resourceType("Resource Type 1")
                .resourceName("resource")
                .build())
            .resourceJson(inputJson)
            .build();

        @Setup
        public void doSetup() {
            //create map of explicit access JsonPointer -> Permission
            Map<JsonPointer, Set<Permission>> attributePermissions =
                ImmutableMap.<JsonPointer, Set<Permission>>builder()
                    .put(JsonPointer.valueOf(""), ImmutableSet.of(READ, CREATE))
                    .put(JsonPointer.valueOf("/orders"), ImmutableSet.of(CREATE))
                    .put(JsonPointer.valueOf("/orders/orderType"), ImmutableSet.of(READ))
                    .put(JsonPointer.valueOf("/orders/emergencyProtectionOrderDirections"), ImmutableSet.of(DELETE))
                    .put(JsonPointer.valueOf("/caseName"), ImmutableSet.of(UPDATE, CREATE, READ, DELETE))
                    .put(JsonPointer.valueOf("/children"), ImmutableSet.of(READ))
                    .put(JsonPointer.valueOf("/children/firstChild"), ImmutableSet.of(UPDATE))
                    .put(JsonPointer.valueOf("/children/adoption"), ImmutableSet.of(READ, CREATE))
                    .put(JsonPointer.valueOf("/applicant"), ImmutableSet.of(READ, CREATE))
                    .put(JsonPointer.valueOf("/solicitor"), ImmutableSet.of(UPDATE, DELETE, CREATE))
                    .build();

            //create explicit access grant
            service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
                .resourceId(resourceId)
                .accessorIds(ImmutableSet.of(accessorId))
                .accessType("EXPLICIT")
                .serviceName("Service 1")
                .resourceType("Resource Type 1")
                .resourceName("resource")
                .attributePermissions(attributePermissions)
                .securityClassification(SecurityClassification.PUBLIC)
                .build()
            );
        }
    }

    @Benchmark
    public void baseline() {
        //NO-OP
    }

    @Benchmark
    public void filterResourceBenchmark(RecordState state) {
        state.service.filterResource(state.accessorId, ImmutableSet.of("DOES_NOT_EXIST"), state.resource);
    }
}
