package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import javafx.util.Pair;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermission;
import uk.gov.hmcts.reform.amlib.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Map;
import java.util.Set;

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

    public void grantDefaultPermission(String roleName,
                                       String serviceName,
                                       String resourceType,
                                       String resourceName,
                                       Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> attributePermissions) {

        jdbi.useTransaction((handle) -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            attributePermissions.forEach((attribute, permissionAndClassification) -> {
                dao.createResourceAttribute(
                    serviceName, resourceType, resourceName, attribute.toString(), permissionAndClassification.getValue());

                dao.grantDefaultPermission(
                    DefaultPermission.builder()
                        .serviceName(serviceName)
                        .resourceType(resourceType)
                        .resourceName(resourceName)
                        .attribute(attribute.toString())
                        .roleName(roleName)
                        .permissions(Permissions.sumOf(permissionAndClassification.getKey()))
                        .build());
            });
        });
    }
}
