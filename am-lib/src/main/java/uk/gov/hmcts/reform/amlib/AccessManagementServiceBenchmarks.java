package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.springframework.boot.jdbc.DataSourceBuilder;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
public class AccessManagementServiceBenchmarks {

    @State(Scope.Thread)
    @SuppressWarnings("LineLength")
    public static class RecordState {
        AccessManagementService service =
            new AccessManagementService(DataSourceBuilder
                .create()
                .username("amuser@am-lib-test-aat")
                .password("upF#99gf7RAZ?77H")
                .url("jdbc:postgresql://am-lib-test-aat.postgres.database.azure.com:5432/am?user=amuser@am-lib-test-aat&password=upF#99gf7RAZ?77H&sslmode=require")
                .build()
            );

        String resourceId = UUID.randomUUID().toString();
        String accessorId = UUID.randomUUID().toString();

        Map<JsonPointer, Set<Permission>> attributePermissions =
            ImmutableMap.of(
                JsonPointer.valueOf("/" + UUID.randomUUID().toString()),
                Stream.of(Permission.READ).collect(Collectors.toSet()));

        ExplicitAccessGrant grant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessType("EXPLICIT")
            .serviceName("Service 1")
            .resourceType("Resource Type 1")
            .resourceName("resource")
            .attributePermissions(attributePermissions)
            .securityClassification(SecurityClassification.PUBLIC)
            .build();
    }

    @Benchmark
    public void grantExplicitResourceAccess(RecordState state) throws SQLException {
        state.service.grantExplicitResourceAccess(state.grant);
    }
}
