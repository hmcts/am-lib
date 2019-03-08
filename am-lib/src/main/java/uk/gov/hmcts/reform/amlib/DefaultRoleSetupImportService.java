package uk.gov.hmcts.reform.amlib;

import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.repositories.DefaultRoleSetupRepository;

import javax.sql.DataSource;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    /**
     * This constructor has issues with performance due to requiring a new connection for every query.
     *
     * @param url      the url for the database
     * @param username     the username for the database
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
     */
    public void addService(@NonNull String serviceName) {
        addService(serviceName, null);
    }

    /**
     * Creates a new unique service, with a description, or updates description if already exists.
     *
     * @param serviceName        the name of the service
     * @param serviceDescription a description of the service
     */
    public void addService(@NonNull String serviceName, String serviceDescription) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

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
     */
    public void addRole(@NonNull String roleName,
                        @NonNull RoleType roleType,
                        @NonNull SecurityClassification securityClassification,
                        @NonNull AccessType accessType) {
        if (roleName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addRole(roleName, roleType, securityClassification, accessType));
    }

    /**
     * Creates a new resource definition or does nothing if already exists.
     *
     * @param serviceName  the name of the service the resource belongs to
     * @param resourceType the type of resource
     * @param resourceName the name of the resource
     */
    public void addResourceDefinition(@NonNull String serviceName,
                                      @NonNull String resourceType,
                                      @NonNull String resourceName) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        if (resourceType.isEmpty() || resourceName.isEmpty()) {
            throw new IllegalArgumentException("Resource cannot contain empty values");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.addResourceDefinition(serviceName, resourceType, resourceName));

    }

    /**
     * Creates a new resource attribute with default permissions for a role or updates attributes if already exists.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param defaultPermissionGrant a container for granting default permissions
     * @throws PersistenceException if any errors were encountered causing transaction rollback
     */
    public void grantDefaultPermission(DefaultPermissionGrant defaultPermissionGrant) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            try {
                defaultPermissionGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {
                    dao.createResourceAttribute(ResourceAttribute.builder()
                        .serviceName(defaultPermissionGrant.getServiceName())
                        .resourceName(defaultPermissionGrant.getResourceName())
                        .resourceType(defaultPermissionGrant.getResourceType())
                        .attribute(attribute)
                        .defaultSecurityClassification(permissionAndClassification.getValue())
                        .build()
                    );

                    dao.grantDefaultPermission(
                        RoleBasedAccessRecord.builder()
                            .serviceName(defaultPermissionGrant.getServiceName())
                            .resourceType(defaultPermissionGrant.getResourceType())
                            .resourceName(defaultPermissionGrant.getResourceName())
                            .attribute(attribute)
                            .roleName(defaultPermissionGrant.getRoleName())
                            .permissions(permissionAndClassification.getKey())
                            .build());
                });

            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        });
    }

    /**
     * Deletes all default permissions within a service for a given resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param serviceName  the name of the service to delete default permissions for
     * @param resourceType the type of resource to delete default permissions for
     * @throws PersistenceException if any errors were encountered causing transaction rollback
     */
    public void truncateDefaultPermissionsForService(String serviceName, String resourceType) {
        jdbi.useTransaction(handle -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType);
                dao.deleteResourceAttributes(serviceName, resourceType);
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
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
     * @throws PersistenceException if any errors were encountered causing transaction rollback
     */
    public void truncateDefaultPermissionsByResourceDefinition(String serviceName,
                                                               String resourceType,
                                                               String resourceName) {
        jdbi.useTransaction(handle -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType, resourceName);
                dao.deleteResourceAttributes(serviceName, resourceType, resourceName);
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        });
    }

    /**
     * Deletes a resource definition.
     *
     * @param serviceName  the name of the service the resource attribute belongs to
     * @param resourceType the type of resource
     * @param resourceName the name of the resource
     */
    public void deleteResourceDefinition(String serviceName, String resourceType, String resourceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.deleteResourceDefinition(serviceName, resourceType, resourceName));
    }

    /**
     * Deletes a role.
     *
     * @param roleName the role name to delete
     */
    public void deleteRole(String roleName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteRole(roleName));
    }

    /**
     * Deletes a service.
     *
     * @param serviceName the service name to delete
     */
    public void deleteService(String serviceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteService(serviceName));
    }
}
