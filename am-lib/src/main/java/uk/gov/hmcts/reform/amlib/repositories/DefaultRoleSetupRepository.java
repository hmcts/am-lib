package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.models.RoleBasedAccessRecord;

@SuppressWarnings({"LineLength", "PMD"})
public interface DefaultRoleSetupRepository {
    @SqlUpdate("insert into services (service_name, service_description) values (:serviceName, :serviceDescription)"
        + " on conflict on constraint services_pkey do update set service_description = :serviceDescription")
    void addService(String serviceName, String serviceDescription);

    @SqlUpdate("insert into roles (role_name, role_type, security_classification, access_management_type) values (:roleName, cast(:roleType as role_type), cast(:securityClassification as security_classification), cast(:accessType as access_type))"
        + " on conflict on constraint roles_pkey do update set role_type = cast(:roleType as role_type), security_classification = cast(:securityClassification as security_classification), access_management_type = cast(:accessType as access_type)")
    void addRole(String roleName, RoleType roleType, SecurityClassification securityClassification, AccessType accessType);

    @SqlUpdate("insert into resources (service_name, resource_type, resource_name) values (:serviceName, :resourceType, :resourceName)"
        + "on conflict on constraint resources_pkey do nothing")
    void addResourceDefinition(String serviceName, String resourceType, String resourceName);

    @SqlUpdate("insert into resource_attributes (service_name, resource_type, resource_name, attribute, default_security_classification)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, cast(:securityClassification as security_classification))"
        + " on conflict on constraint resource_attributes_pkey do update set default_security_classification = cast(:securityClassification as security_classification)")
    void createResourceAttribute(@BindBean ResourceAttribute resourceAttribute);

    @SqlUpdate("insert into default_permissions_for_roles (service_name, resource_type, resource_name, attribute, role_name, permissions)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, :roleName, :permissionsAsInt)"
        + " on conflict on constraint default_permissions_for_roles_service_name_resource_type_re_key do update set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName, attribute = :attributeAsString, role_name = :roleName, permissions = :permissionsAsInt")
    void grantDefaultPermission(@BindBean RoleBasedAccessRecord roleBasedAccessRecord);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType")
    void deleteDefaultPermissionsForRoles(String serviceName, String resourceType);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName")
    void deleteDefaultPermissionsForRoles(String serviceName, String resourceType, String resourceName);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType")
    void deleteResourceAttributes(String serviceName, String resourceType);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceAttributes(String serviceName, String resourceType, String resourceName);

    @SqlUpdate("delete from resources where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceDefinition(String serviceName, String resourceType, String resourceName);

    @SqlUpdate("delete from roles where role_name = :roleName")
    void deleteRole(String roleName);

    @SqlUpdate("delete from services where service_name = :serviceName")
    void deleteService(String serviceName);
}
