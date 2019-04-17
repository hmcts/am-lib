package uk.gov.hmcts.reform.amlib;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditingAspect;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;

import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.utils.DataSourceFactory.createDataSource;

public class SetupGenerator {
    private static final List<CaseDefinition> definitions = ImmutableList.of(
        new CaseDefinition(
            "fpl",
            "application",
            ImmutableMap.<JsonPointer, Set<Permission>>builder()
                .put(JsonPointer.valueOf(""), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/caseName"), ImmutableSet.of(UPDATE, CREATE, READ, DELETE))
                .put(JsonPointer.valueOf("/orders"), ImmutableSet.of(CREATE))
                .put(JsonPointer.valueOf("/orders/orderType"), ImmutableSet.of(READ))
                .put(JsonPointer.valueOf("/orders/emergencyProtectionOrderDirections"), ImmutableSet.of(DELETE))
                .put(JsonPointer.valueOf("/applicant"), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/children"), ImmutableSet.of(READ))
                .put(JsonPointer.valueOf("/children/firstChild"), ImmutableSet.of(UPDATE))
                .put(JsonPointer.valueOf("/children/adoption"), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/solicitor"), ImmutableSet.of(UPDATE, DELETE, CREATE))
                .build()
        ),
        new CaseDefinition(
            "cmc",
            "claim",
            ImmutableMap.<JsonPointer, Set<Permission>>builder()
                .put(JsonPointer.valueOf(""), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/referenceNumber"), ImmutableSet.of(UPDATE, CREATE, READ, DELETE))
                .put(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE))
                .put(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ))
                .put(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(DELETE))
                .put(JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/defendant/address"), ImmutableSet.of(READ))
                .put(JsonPointer.valueOf("/amount"), ImmutableSet.of(UPDATE))
                .put(JsonPointer.valueOf("/response"), ImmutableSet.of(READ, CREATE))
                .put(JsonPointer.valueOf("/countyCourtJudgement"), ImmutableSet.of(UPDATE, DELETE, CREATE))
                .build()
        )
    );

    private SetupGenerator() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static void main(String[] args) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(AuditingAspect.class).setLevel(Level.ERROR);

        DataSource dataSource = createDataSource();
        DefaultRoleSetupImportService importerService = new DefaultRoleSetupImportService(dataSource);
        AccessManagementService service = new AccessManagementService(dataSource);

        for (CaseDefinition definition : definitions) {
            String resourceType = "case";

            // Lookup tables
            importerService.addService(definition.serviceName);
            importerService.addResourceDefinition(definition.serviceName, resourceType, definition.resourceName);
            importerService.addRole("caseworker", IDAM, PUBLIC, ROLE_BASED);

            // Role based permissions
            importerService.grantDefaultPermission(
                DefaultPermissionGrant.builder()
                    .serviceName(definition.serviceName)
                    .resourceType(resourceType)
                    .resourceName(definition.resourceName)
                    .roleName("caseworker")
                    .attributePermissions(definition.attributePermissions.entrySet().stream()
                        .collect(toDefaultAttributePermissions()))
                    .build()
            );

            // Explicit permissions
            IntStream.range(1, 25001).forEach(number -> service.grantExplicitResourceAccess(
                ExplicitAccessGrant.builder()
                    .serviceName(definition.serviceName)
                    .resourceType(resourceType)
                    .resourceName(definition.resourceName)
                    .resourceId(definition.serviceName + "-resource-" + number)
                    .accessorIds(ImmutableSet.of("user-" + number))
                    .accessorType(USER)
                    .attributePermissions(definition.attributePermissions)
                    .relationship("caseworker")
                    .build()
            ));
        }
    }

    @SuppressWarnings("LineLength")
    private static Collector<Entry<JsonPointer, Set<Permission>>, ?, Map<JsonPointer, Entry<Set<Permission>, SecurityClassification>>> toDefaultAttributePermissions() {
        return Collectors.toMap(
            Entry::getKey,
            entry -> new Pair<>(entry.getValue(), PUBLIC)
        );
    }

    private static class CaseDefinition {
        private String serviceName;
        private String resourceName;
        private Map<JsonPointer, Set<Permission>> attributePermissions;

        private CaseDefinition(String serviceName,
                               String resourceName,
                               Map<JsonPointer, Set<Permission>> attributePermissions) {
            this.serviceName = serviceName;
            this.resourceName = resourceName;
            this.attributePermissions = attributePermissions;
        }
    }
}
