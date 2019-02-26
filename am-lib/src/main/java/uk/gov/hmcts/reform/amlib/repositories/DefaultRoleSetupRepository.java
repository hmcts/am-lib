package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindMap;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermission;

import java.util.Set;

@SuppressWarnings("LineLength")
public interface DefaultRoleSetupRepository {
    @SqlUpdate("insert into services (service_name, service_description) values (:serviceName, :serviceDescription)" +
        " on conflict on constraint services_pkey do update set service_name = :serviceName, service_description = :serviceDescription")
    void addService(@Bind("serviceName") String serviceName, @Bind("serviceDescription") String serviceDescription);

    @SqlUpdate("insert into roles (role_name, role_type, security_classification, access_management_type) values (:roleName, :roleType, cast(:securityClassification as securityclassification), :accessManagementType)" +
        " on conflict on constraint roles_pkey do update set role_name = :roleName, role_type = :roleType, security_classification = cast(:securityClassification as securityclassification), access_management_type = :accessManagementType")
    void addRole(@Bind("roleName") String roleName, @Bind("roleType") RoleType roleType, @Bind("securityClassification") SecurityClassification securityClassification, @Bind("accessManagementType") AccessManagementType accessManagementType);

    @SqlUpdate("insert into resources (service_name, resource_type, resource_name) values (:serviceName, :resourceType, :resourceName)" +
        "on conflict on constraint resources_pkey do update set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName")
    void addResourceDefinition(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName);

    @SqlUpdate("insert into resource_attributes (service_name, resource_type, resource_name, attribute, default_security_classification)"
    + " values (:serviceName, :resourceType, :resourceName, :attribute, cast(:securityClassification as securityclassification))")
    void createResourceAttribute(@Bind("serviceName") String serviceName, @Bind("resourceType") String resourceType, @Bind("resourceName") String resourceName, @Bind("attribute") String attribute, @Bind("securityClassification") SecurityClassification securityClassification);

    @SqlUpdate("insert into default_permissions_for_roles (service_name, resource_type, resource_name, attribute, role_name, permissions)"
    + " values (:serviceName, :resourceType, :resourceName, :attribute, :roleName, :permissions)")
    void grantDefaultPermission(@BindBean DefaultPermission defaultPermission);

}
