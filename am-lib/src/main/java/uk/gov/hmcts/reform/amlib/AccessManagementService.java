package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.ErrorAddingEntriesToDatabaseException;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;

public class AccessManagementService {
    private final Jdbi jdbi;
    private static final int MAX_ROLE_NAMES_RETURNED = 1;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * Grants explicit access to resource accordingly to record configuration.
     *
     * <p>Operation is performed in transaction so that if not all records can be created then whole grant will fail.
     *
     * @param explicitAccessGrant an object that describes explicit access to resource
     */
    public void grantExplicitResourceAccess(ExplicitAccessGrant explicitAccessGrant) {
        if (explicitAccessGrant.getAttributePermissions().size() == 0) {
            throw new IllegalArgumentException("At least one attribute is required");
        }
        if (explicitAccessGrant.getAttributePermissions().entrySet().stream()
            .anyMatch(attributePermission -> attributePermission.getValue().isEmpty())) {
            throw new IllegalArgumentException("At least one permission per attribute is required");
        }

        jdbi.useTransaction(handle -> {
            AccessManagementRepository dao = handle.attach(AccessManagementRepository.class);
            try {
                explicitAccessGrant.getAttributePermissions().entrySet().stream().map(attributePermission ->
                    ExplicitAccessRecord.builder()
                        .resourceId(explicitAccessGrant.getResourceId())
                        .accessorId(explicitAccessGrant.getAccessorId())
                        .explicitPermissions(attributePermission.getValue())
                        .accessType(explicitAccessGrant.getAccessType())
                        .serviceName(explicitAccessGrant.getServiceName())
                        .resourceType(explicitAccessGrant.getResourceType())
                        .resourceName(explicitAccessGrant.getResourceName())
                        .attribute(attributePermission.getKey().toString())
                        .securityClassification(explicitAccessGrant.getSecurityClassification())
                        .build())
                    .forEach(dao::createAccessManagementRecord);
            } catch (Exception e) {
                throw new ErrorAddingEntriesToDatabaseException(e);
            }
        });
    }

    /**
     * Removes explicit access to resource accordingly to record configuration.
     *
     * @param explicitAccessMetadata an object to remove a specific explicit access record.
     */
    public void revokeResourceAccess(ExplicitAccessMetadata explicitAccessMetadata) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.removeAccessManagementRecord(explicitAccessMetadata));
    }

    /**
     * Returns list of user ids who have access to resource or null if user has no access to this resource.
     *
     * @param userId     (accessorId)
     * @param resourceId resource Id
     * @return List of user ids (accessor id) or null
     */
    public List<String> getAccessorsList(String userId, String resourceId) {
        return jdbi.withExtension(AccessManagementRepository.class, dao -> {
            List<String> userIds = dao.getAccessorsList(userId, resourceId);

            return userIds.isEmpty() ? null : userIds;
        });
    }

    /**
     * Returns {@link FilterResourceResponse} when record with userId and resourceId exist and has READ permissions,
     * otherwise null.
     *
     * @param userId       (accessorId)
     * @param resourceId   resource id
     * @param resourceJson json
     * @return resourceJson or null
     */
    public FilterResourceResponse filterResource(String userId, String resourceId, JsonNode resourceJson) {
        ExplicitAccessRecord explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess == null) {
            return null;
        }

        if (READ.isGranted(explicitAccess.getPermissions())) {
            Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
            attributePermissions.put(JsonPointer.valueOf(""), Permissions.fromSumOf(explicitAccess.getPermissions()));

            return FilterResourceResponse.builder()
                .resourceId(resourceId)
                .data(resourceJson)
                .permissions(attributePermissions)
                .build();
        }

        return null;
    }


    /**
     * <p>
     * Method requires name of service, the type of resource, name of resource and a role name to
     * populate a  map of JsonPointers and set of permissions when record with user role and type exists in database.
     * </p>
     *
     * @param serviceName  name of service
     * @param resourceType type of resource
     * @param resourceName name of a resource
     * @param roleNames    A set of role names. Currently only one role name is supported but
     *                     in future implementations we shall support having multiple role names.
     * @return permissionsByTypeAndRole a map of attributes and their corresponding permissions or null.
     */
    public Map<JsonPointer, Set<Permission>> getRolePermissions(
        @NonNull String serviceName, @NonNull String resourceType,
        @NonNull String resourceName, @NonNull Set<String> roleNames) {
        if (roleNames.size() > MAX_ROLE_NAMES_RETURNED) {
            throw new IllegalArgumentException();
        }

        List<RoleBasedAccessRecord> roleBasedAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRolePermissions(serviceName, resourceType, resourceName, roleNames.iterator().next()));

        if (roleBasedAccess.isEmpty()) {
            return null;
        }

        return roleBasedAccess.stream()
            .collect(Collectors.toMap(RoleBasedAccessRecord::getAttribute, RoleBasedAccessRecord::getPermissions));
    }
}
