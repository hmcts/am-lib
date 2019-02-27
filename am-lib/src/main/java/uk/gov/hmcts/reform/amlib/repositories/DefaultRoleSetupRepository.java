package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermission;
import uk.gov.hmcts.reform.amlib.models.ResourceAttribute;

@SuppressWarnings({"LineLength", "PMD"})
public interface DefaultRoleSetupRepository {
    @SqlUpdate("insert into services (service_name, service_description) values (:serviceName, :serviceDescription)"
        + " on conflict on constraint services_pkey do update set service_name = :serviceName, service_description = :serviceDescription")
    void addService(@Bind("serviceName") String serviceName, @Bind("serviceDescription") String serviceDescription);

    @SqlUpdate("insert into roles (role_name, role_type, security_classification, access_management_type) values (:roleName, :roleType, cast(:securityClassification as securityclassification), :accessManagementType)"
        + " on conflict on constraint roles_pkey do update set role_name = :roleName, role_type = :roleType, security_classification = cast(:securityClassification as securityclassification), access_management_type = :accessManagementType")
    void addRole(@Bind("roleName") String roleName, @Bind("roleType") RoleType roleType, @Bind("securityClassification") SecurityClassification securityClassification, @Bind("accessManagementType") AccessManagementType accessManagementType);

    @SqlUpdate("insert into resources (service_name, resource_type, resource_name) values (:serviceName, :resourceType, :resourceName)"
        + "on conflict on constraint resources_pkey do update set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName")
    void addResourceDefinition(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName);

    @SqlUpdate("insert into resource_attributes (service_name, resource_type, resource_name, attribute, default_security_classification)"
        + " values (:serviceName, :resourceType, :resourceName, :attribute, cast(:securityClassification as securityclassification))"
        + " on conflict on constraint resource_attributes_pkey do update set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName, attribute = :attribute, default_security_classification = cast(:securityClassification as securityclassification)")
    void createResourceAttribute(@BindBean ResourceAttribute resourceAttribute);

    @SqlUpdate("insert into default_permissions_for_roles (service_name, resource_type, resource_name, attribute, role_name, permissions)"
        + " values (:serviceName, :resourceType, :resourceName, :attribute, :roleName, :permissions)"
        + " on conflict on constraint default_permissions_for_roles_service_name_resource_type_re_key do update set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName, attribute = :attribute, role_name = :roleName, permissions = :permissions")
    void grantDefaultPermission(@BindBean DefaultPermission defaultPermission);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType")
    void deleteDefaultPermissionsForRoles(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName")
    void deleteDefaultPermissionsForRoles(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType")
    void deleteResourceAttributes(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceAttributes(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName);

    @SqlUpdate("delete from resources where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceDefinition(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName);

    @SqlUpdate("delete from roles where role_name = :roleName")
    void deleteRole(@Bind("roleName") String roleName);

    @SqlUpdate("delete from services where service_name = :serviceName")
    void deleteService(@Bind("serviceName") String serviceName);
}
