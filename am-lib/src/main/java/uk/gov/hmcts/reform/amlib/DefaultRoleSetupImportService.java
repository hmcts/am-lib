package uk.gov.hmcts.reform.amlib;

import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.ErrorAddingEntriesToDatabaseException;
import uk.gov.hmcts.reform.amlib.models.DefaultPermission;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    public DefaultRoleSetupImportService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public void addService(@NonNull String serviceName, String serviceDescription) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, serviceDescription));
    }

    public void addRole(@NonNull String roleName, RoleType roleType, SecurityClassification securityClassification,
                        AccessManagementType accessManagementType) {
        if (roleName.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addRole(roleName, roleType, securityClassification, accessManagementType));
    }

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

    public void grantDefaultPermission(DefaultPermissionGrant defaultPermissionGrant) {

        jdbi.useTransaction((handle) -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            try {
                defaultPermissionGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {
                    dao.createResourceAttribute(
                        defaultPermissionGrant.getServiceName(),
                        defaultPermissionGrant.getResourceType(),
                        defaultPermissionGrant.getResourceName(),
                        attribute.toString(),
                        permissionAndClassification.getValue());

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
                throw new ErrorAddingEntriesToDatabaseException(e);
            }
        });
    }

    public void truncateAllDefaultPermissionsForService(String serviceName, String resourceType) {
        jdbi.useTransaction((handle) -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType);
                dao.deleteResourceAttributes(serviceName, resourceType);
            } catch (Exception e) {
                //TODO: new exception for removing entries.
                throw new ErrorAddingEntriesToDatabaseException(e);
            }
        });
    }

    public void truncateAllDefaultPermissionsByResourceDefinition(String serviceName,
                                                                  String resourceType,
                                                                  String resourceName) {
        jdbi.useTransaction((handle) -> {
            try {
                DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
                dao.deleteDefaultPermissionsForRoles(serviceName, resourceType, resourceName);
                dao.deleteResourceAttributes(serviceName, resourceType, resourceName);
            } catch (Exception e) {
                //TODO: new exception for removing entries.
                throw new ErrorAddingEntriesToDatabaseException(e);
            }
        });
    }

    public void deleteResourceDefinition(String serviceName, String resourceType, String resourceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.deleteResourceDefinition(serviceName, resourceType, resourceName));
    }

    public void deleteRole(String roleName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteRole(roleName));
    }

    public void deleteService(String serviceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteService(serviceName));
    }
}
