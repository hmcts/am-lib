package uk.gov.hmcts.reform.amlib;

import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.ErrorAddingEntriesException;
import uk.gov.hmcts.reform.amlib.exceptions.ErrorWithDeletionException;
import uk.gov.hmcts.reform.amlib.models.DefaultPermission;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import javax.sql.DataSource;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    public DefaultRoleSetupImportService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public DefaultRoleSetupImportService(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource);
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * A method to add a new service.
     *
     * @param serviceName the name of the service.
     */
    public void addService(@NonNull String serviceName) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, ""));
    }

    /**
     * A method to add a new service.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a description of the service.
     */
    public void addService(@NonNull String serviceName, String serviceDescription) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, serviceDescription));
    }

    /**
     * A method to add a new role.
     *
     * @param roleName               the name of the role.
     * @param roleType               the type of role.
     * @param securityClassification the security classification for the role.
     * @param accessType             the access type for the role.
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
     * A method used to add a resource definition.
     *
     * @param serviceName  the name of the service the resource belongs to.
     * @param resourceType the type of resource.
     * @param resourceName the name of the resource.
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
     * A method to create a resource attribute and assign it default permissions for a role. This method uses a
     * transaction and will rollback if any errors are encountered. {@link ErrorAddingEntriesException}
     *
     * @param defaultPermissionGrant a container for granting default permissions.
     */
    public void grantDefaultPermission(DefaultPermissionGrant defaultPermissionGrant) {
        jdbi.useTransaction((handle) -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            try {
                defaultPermissionGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {
                    dao.createResourceAttribute(ResourceAttribute.builder()
                        .serviceName(defaultPermissionGrant.getServiceName())
                        .resourceName(defaultPermissionGrant.getResourceName())
                        .resourceType(defaultPermissionGrant.getResourceType())
                        .attribute(attribute.toString())
                        .securityClassification(permissionAndClassification.getValue())
                        .build()
                    );

                    dao.grantDefaultPermission(
                        DefaultPermission.builder()
                            .serviceName(defaultPermissionGrant.getServiceName())
                            .resourceType(defaultPermissionGrant.getResourceType())
                            .resourceName(defaultPermissionGrant.getResourceName())
                            .attribute(attribute.toString())
                            .roleName(defaultPermissionGrant.getRoleName())
                            .permissions(Permissions.sumOf(permissionAndClassification.getKey()))
                            .build());
                });

            } catch (Exception e) {
                throw new ErrorAddingEntriesException(e);
            }
        });
    }

    /**
     * A method to delete default permissions within a service for a given resource type.
     * This method uses a transaction and will rollback if any errors are encountered.
     *
     * @param serviceName  the name of the service to delete default permissions for.
     * @param resourceType the type of resource to delete default permissions for.
     */
    public void truncateDefaultPermissionsForService(String serviceName, String resourceType) {
        jdbi.useTransaction((handle) -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType);
                dao.deleteResourceAttributes(serviceName, resourceType);
            } catch (Exception e) {
                throw new ErrorWithDeletionException(e);
            }
        });
    }

    /**
     * A method to delete default permissions within a service for a specific resource name and resource type.
     * This method uses a transaction and will rollback if any errors are encountered.
     *
     * @param serviceName  the name of the service to delete default permissions for.
     * @param resourceType the type of resource to delete default permissions for.
     * @param resourceName the name of the resource to delete default permissions for.
     */
    public void truncateDefaultPermissionsByResourceDefinition(String serviceName,
                                                               String resourceType,
                                                               String resourceName) {
        jdbi.useTransaction((handle) -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType, resourceName);
                dao.deleteResourceAttributes(serviceName, resourceType, resourceName);
            } catch (Exception e) {
                throw new ErrorWithDeletionException(e);
            }
        });
    }

    /**
     * A method to delete a resource attribute.
     *
     * @param serviceName  the name of the service the resource attribute belongs to.
     * @param resourceType the type of resource.
     * @param resourceName the name of the resource.
     */
    public void deleteResourceDefinition(String serviceName, String resourceType, String resourceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.deleteResourceDefinition(serviceName, resourceType, resourceName));
    }

    /**
     * A method to delete a role.
     *
     * @param roleName the role name to delete.
     */
    public void deleteRole(String roleName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteRole(roleName));
    }

    /**
     * A method to delete a service.
     *
     * @param serviceName the service name to delete.
     */
    public void deleteService(String serviceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteService(serviceName));
    }
}
