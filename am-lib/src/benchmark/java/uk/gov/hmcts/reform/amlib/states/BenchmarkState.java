package uk.gov.hmcts.reform.amlib.states;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.postgresql.ds.PGPoolingDataSource;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.config.DatabaseProperties;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.enums.Permission.*;

@State(Scope.Benchmark)
public class BenchmarkState {
    public AccessManagementService service;

    @Setup
    public void initiateService() {
        service = new AccessManagementService(createDataSource());
    }

    @Setup
    public void populateDatabase(DataState state) {
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

        for (int i = 0; i < 50000; i++) {
            service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
                .resourceId(UUID.randomUUID().toString())
                .accessorIds(ImmutableSet.of(state.accessorId))
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

    @TearDown
    public void cleanupDatabase() {
        
    }

    @SuppressWarnings({"deprecation"})
    private DataSource createDataSource() {
        DatabaseProperties databaseProperties = DatabaseProperties.createFromEnvironmentProperties();

        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(databaseProperties.getServer().getHost());
        dataSource.setPortNumber(databaseProperties.getServer().getPort());
        dataSource.setDatabaseName(databaseProperties.getDatabase());
        dataSource.setUser(databaseProperties.getCredentials().getUsername());
        dataSource.setPassword(databaseProperties.getCredentials().getPassword());
        dataSource.setMaxConnections(64);
        return dataSource;
    }
}
