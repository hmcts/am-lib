package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    /**
     * This constructor has issues with performance due to requiring a new connection for every query.
     *
     * @param url      the url for the database
     * @param username the username for the database
     * @param password the password for the database
     */
    public DefaultRoleSetupImportService(String url, String username, String password) {
        this.jdbi = Jdbi.create(url, username, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended to be used over the above.
     *
     * @param dataSource the datasource for the database
     */
    public DefaultRoleSetupImportService(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * Creates a new unique service or updates description if already exists.
     *
     * @param serviceName the name of the service
     * @throws PersistenceException if any persistence errors were encountered
     */
    public void addService(@NotBlank String serviceName) {
        addService(serviceName, null);
    }

    /**
     * Creates a new unique service, with a description, or updates description if already exists.
     *
     * @param serviceName        the name of the service
     * @param serviceDescription a description of the service
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("service '{{in#serviceName}}' described as '{{in#serviceDescription}}' added")
    public void addService(@NotBlank String serviceName, String serviceDescription) {
        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, serviceDescription));
    }

    /**
     * Creates a new unique role or updates type, security classification and access type if already exists.
     *
     * @param roleName               the name of the role
     * @param roleType               the type of role
     * @param securityClassification the security classification for the role
     * @param accessType             the access type for the role
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("role '{{in#roleName}}' of type '{{in#roleType}}' added")
    public void addRole(@NotBlank String roleName,
                        @NotNull RoleType roleType,
                        @NotNull SecurityClassification securityClassification,
                        @NotNull AccessType accessType) {
        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addRole(roleName, roleType, securityClassification, accessType));
    }

    /**
     * Creates a new resource definition or does nothing if already exists.
     *
     * @param serviceName  the name of the service the resource belongs to
     * @param resourceType the type of resource
     * @param resourceName the name of the resource
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("resource definition defined as '{{in#serviceName}}|{{in#resourceType}}|{{in#resourceName}}' added")
    public void addResourceDefinition(@NotBlank String serviceName,
                                      @NotBlank String resourceType,
                                      @NotBlank String resourceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.addResourceDefinition(serviceName, resourceType, resourceName));

    }

    /**
     * Creates a new resource attribute with default permissions for a role or updates attributes if already exists.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param accessGrant a container for granting default permissions
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("role access granted to resource "
        + "defined as '{{in#accessGrant.serviceName}}|{{in#accessGrant.resourceType}}|{{in#accessGrant.resourceName}}'"
        + " for role '{{in#accessGrant.roleName}}': {{in#accessGrant.attributePermissions}}")
    public void grantDefaultPermission(@NotNull @Valid DefaultPermissionGrant accessGrant) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            accessGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {
                dao.createResourceAttribute(ResourceAttribute.builder()
                    .serviceName(accessGrant.getServiceName())
                    .resourceName(accessGrant.getResourceName())
                    .resourceType(accessGrant.getResourceType())
                    .attribute(attribute)
                    .defaultSecurityClassification(permissionAndClassification.getValue())
                    .build()
                );

                dao.grantDefaultPermission(
                    RoleBasedAccessRecord.builder()
                        .serviceName(accessGrant.getServiceName())
                        .resourceType(accessGrant.getResourceType())
                        .resourceName(accessGrant.getResourceName())
                        .attribute(attribute)
                        .roleName(accessGrant.getRoleName())
                        .permissions(permissionAndClassification.getKey())
                        .build());
            });
        });
    }

    /**
     * Deletes all default permissions within a service for a given resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param serviceName  the name of the service to delete default permissions for
     * @param resourceType the type of resource to delete default permissions for
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("role access revoked for service defined as '{{in#serviceName}}|{{in#resourceType}}'")
    public void truncateDefaultPermissionsForService(@NotBlank String serviceName, @NotBlank String resourceType) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            dao.deleteDefaultPermissionsForRoles(serviceName, resourceType);
            dao.deleteResourceAttributes(serviceName, resourceType);
        });
    }

    /**
     * Deletes all default permissions within a service for a specific resource name and resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param serviceName  the name of the service to delete default permissions for
     * @param resourceType the type of resource to delete default permissions for
     * @param resourceName the name of the resource to delete default permissions for
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("role access revoked for resource defined as '{{in#serviceName}}|{{in#resourceType}}|{{in#resourceName}}'")
    public void truncateDefaultPermissionsByResourceDefinition(@NotBlank String serviceName,
                                                               @NotBlank String resourceType,
                                                               @NotBlank String resourceName) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            dao.deleteDefaultPermissionsForRoles(serviceName, resourceType, resourceName);
            dao.deleteResourceAttributes(serviceName, resourceType, resourceName);
        });
    }

    /**
     * Deletes a resource definition.
     *
     * @param serviceName  the name of the service the resource attribute belongs to
     * @param resourceType the type of resource
     * @param resourceName the name of the resource
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("resource definition defined as '{{in#serviceName}}|{{in#resourceType}}|{{in#resourceName}}' deleted")
    public void deleteResourceDefinition(@NotBlank String serviceName,
                                         @NotBlank String resourceType,
                                         @NotBlank String resourceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.deleteResourceDefinition(serviceName, resourceType, resourceName));
    }

    /**
     * Deletes a role.
     *
     * @param roleName the role name to delete
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("role '{{in#roleName}}' deleted")
    public void deleteRole(@NotBlank String roleName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteRole(roleName));
    }

    /**
     * Deletes a service.
     *
     * @param serviceName the service name to delete
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("service '{{in#serviceName}}' deleted")
    public void deleteService(@NotBlank String serviceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteService(serviceName));
    }
}
