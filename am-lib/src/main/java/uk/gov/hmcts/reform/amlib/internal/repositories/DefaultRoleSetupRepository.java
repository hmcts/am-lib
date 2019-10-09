package uk.gov.hmcts.reform.amlib.internal.repositories;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttributeAudit;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.PermissionSetMapper;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.RolePermissionsForCaseTypeEnvelopeReducer;
import uk.gov.hmcts.reform.amlib.models.DefaultRolePermissions;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;

import java.util.List;

@SuppressWarnings({
    "LineLength",
    "PMD.TooManyMethods", // Repository class is specific and it makes sense to have all these methods here
    "PMD.UseObjectForClearerAPI",
    "PMD.AvoidDuplicateLiterals"
})
@RegisterColumnMapper(PermissionSetMapper.class)
public interface DefaultRoleSetupRepository {
    @SqlUpdate("insert into services (service_name, service_description) values (:serviceName, :serviceDescription)"
        + " on conflict on constraint services_pkey do update set service_description = :serviceDescription , last_update = now() at time zone 'utc'")
    void addService(String serviceName, String serviceDescription);

    @SqlUpdate("insert into roles (role_name, role_type, security_classification, access_type) values (:roleName, cast(:roleType as role_type), cast(:securityClassification as security_classification), cast(:accessType as access_type))"
        + " on conflict on constraint roles_pkey do update set role_type = cast(:roleType as role_type), security_classification = cast(:securityClassification as security_classification), access_type = cast(:accessType as access_type), last_update = now() at time zone 'utc'")
    void addRole(String roleName, RoleType roleType, SecurityClassification securityClassification, AccessType accessType);

    @SqlUpdate("insert into resources (service_name, resource_type, resource_name) values (:serviceName, :resourceType, :resourceName)"
        + "on conflict on constraint resources_pkey do update set last_update = now() at time zone 'utc'")
    void addResourceDefinition(@BindBean ResourceDefinition resourceDefinition);

    @SqlUpdate("insert into resource_attributes (service_name, resource_type, resource_name, attribute, default_security_classification, last_update, calling_service_name)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, cast(:defaultSecurityClassification as security_classification), now() at time zone 'utc', :callingServiceName)"
        + " on conflict on constraint resource_attributes_pkey do update set default_security_classification = cast(:defaultSecurityClassification as security_classification), "
        + "last_update = now() at time zone 'utc', calling_service_name = :callingServiceName")
    void createResourceAttribute(@BindBean ResourceAttribute resourceAttribute);

    @SqlUpdate("insert into default_permissions_for_roles (service_name, resource_type, resource_name, attribute, role_name, permissions, last_update, calling_service_name)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, :roleName, :permissionsAsInt, now() at time zone 'utc', :callingServiceName)"
        + " on conflict on constraint default_permissions_for_roles_service_name_resource_type_re_key do update "
        + "set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName,"
        + " attribute = :attributeAsString, role_name = :roleName, permissions = :permissionsAsInt, "
        + "last_update = now() at time zone 'utc', calling_service_name = :callingServiceName")
    void grantDefaultPermission(@BindBean RoleBasedAccessRecord roleBasedAccessRecord);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType")
    void deleteDefaultPermissionsForRoles(String serviceName, String resourceType);

    @SqlUpdate("delete from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName")
    void deleteDefaultPermissionsForRoles(@BindBean ResourceDefinition resourceDefinition);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType")
    void deleteResourceAttributes(String serviceName, String resourceType);

    @SqlUpdate("delete from resource_attributes where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceAttributes(@BindBean ResourceDefinition resourceDefinition);

    @SqlUpdate("delete from resources where service_name = :serviceName and resource_type = :resourceType  and resource_name = :resourceName")
    void deleteResourceDefinition(@BindBean ResourceDefinition resourceDefinition);

    @SqlUpdate("delete from roles where role_name = :roleName")
    void deleteRole(String roleName);

    @SqlUpdate("delete from services where service_name = :serviceName")
    void deleteService(String serviceName);

    @SqlUpdate("insert into resource_attributes_audit (service_name, resource_type, resource_name, attribute, default_security_classification, calling_service_name, audit_timestamp, changed_by, action)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, cast(:defaultSecurityClassification as security_classification), :callingServiceName, now() at time zone 'utc', :changedBy, 'grant')"
      )
    void createResourceAttributeForAudit(@BindBean ResourceAttributeAudit resourceAttributeAudit);

    @SqlUpdate("insert into default_permissions_for_roles_audit (service_name, resource_type, resource_name, attribute, role_name, permissions, calling_service_name, audit_timestamp, changed_by, action)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, :roleName, :permissionsAsInt, :callingServiceName, now() at time zone 'utc', :changedBy, 'grant' )"
       )
    void grantDefaultPermissionAudit(@BindBean RoleBasedAccessAuditRecord roleBasedAccessAuditRecord);

    @SqlUpdate("insert into default_permissions_for_roles_audit (service_name, resource_type, resource_name, attribute, role_name, permissions, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, role_name, permissions, :callingServiceName, now() at time zone 'utc', :changedBy, 'revoke' from default_permissions_for_roles where  service_name = :serviceName "
        + "and resource_type = :resourceType and resource_name = :resourceName"
    )
    void revokeDefaultPermissionAudit(@BindBean ResourceDefinition resourceDefinition, String callingServiceName, String changedBy);

    @SqlUpdate("insert into default_permissions_for_roles_audit (service_name, resource_type, resource_name, attribute, role_name, permissions, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, role_name, permissions, :callingServiceName, now() at time zone 'utc', :changedBy, 'revoke' from default_permissions_for_roles where  service_name = :serviceName "
        + "and resource_type = :resourceType "
    )
    void revokeDefaultPermissionAudit(String serviceName, String resourceType, String callingServiceName, String changedBy);

    @SqlUpdate("insert into resource_attributes_audit (service_name, resource_type, resource_name, attribute, default_security_classification, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, default_security_classification, :callingServiceName, now() at time zone 'utc', :changedBy, 'revoke' from resource_attributes where service_name = :serviceName "
        + "and resource_type = :resourceType "
    )
    void revokeResourceAttributeAudit(String serviceName, String resourceType, String callingServiceName, String changedBy);

    @SqlUpdate("insert into resource_attributes_audit (service_name, resource_type, resource_name, attribute, default_security_classification, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, default_security_classification, :callingServiceName, now() at time zone 'utc', :changedBy, 'revoke' from resource_attributes where service_name = :serviceName "
        + "and resource_type = :resourceType and resource_name = :resourceName"
    )
    void revokeResourceAttributeAudit(@BindBean ResourceDefinition resourceDefinition, String callingServiceName, String changedBy);

    //Batch Updates for CCD load
    @SqlBatch("delete from default_permissions_for_roles where (service_name,resource_type,resource_name) "
        + "in ((:serviceName,:resourceType,:resourceName))")
    void deleteBatchDefaultPermissions(@BindBean List<ResourceDefinition> resourceDefinition);

    @SqlBatch("delete from resource_attributes where (service_name,resource_type,resource_name) "
        + "in ((:serviceName,:resourceType,:resourceName))")
    void deleteBatchResourceAttributes(@BindBean List<ResourceAttribute> resourceAttributes);

    @SqlBatch("insert into default_permissions_for_roles_audit (service_name, resource_type, resource_name, attribute, role_name, permissions, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, role_name, permissions, :callingServiceName, CURRENT_TIMESTAMP, :changedBy, 'revoke' from default_permissions_for_roles where (service_name,resource_type,resource_name) "
        + "in ((:serviceName,:resourceType,:resourceName))"
    )
    void defaultPermissionAuditBatchRevoke(@BindBean List<ResourceDefinition> resourceDefinition, String callingServiceName, String changedBy);

    @SqlBatch("insert into resource_attributes_audit (service_name, resource_type, resource_name, attribute, default_security_classification, calling_service_name, audit_timestamp, changed_by, action) "
        + "select service_name, resource_type, resource_name, attribute, default_security_classification, :callingServiceName, CURRENT_TIMESTAMP, :changedBy, 'revoke' from resource_attributes  where (service_name,resource_type,resource_name) "
        + "in ((:serviceName,:resourceType,:resourceName))"
    )
    void resourceAttributeAuditBatchRevoke(@BindBean List<ResourceDefinition> resourceDefinitions, String callingServiceName, String changedBy);

    @SqlBatch("insert into resource_attributes (service_name, resource_type, resource_name, attribute, default_security_classification, last_update, calling_service_name)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, cast(:defaultSecurityClassification as security_classification), CURRENT_TIMESTAMP, :callingServiceName)"
        + " on conflict on constraint resource_attributes_pkey do update set default_security_classification = cast(:defaultSecurityClassification as security_classification), "
        + "last_update = CURRENT_TIMESTAMP, calling_service_name = :callingServiceName")
    void createResourceAttributeBatch(@BindBean List<ResourceAttribute> resourceAttribute);

    @SqlBatch("insert into default_permissions_for_roles (service_name, resource_type, resource_name, attribute, role_name, permissions, last_update, calling_service_name)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, :roleName, :permissionsAsInt, CURRENT_TIMESTAMP, :callingServiceName)"
        + " on conflict on constraint default_permissions_for_roles_service_name_resource_type_re_key do update "
        + "set service_name = :serviceName, resource_type = :resourceType, resource_name = :resourceName,"
        + " attribute = :attributeAsString, role_name = :roleName, permissions = :permissionsAsInt, "
        + "last_update = CURRENT_TIMESTAMP, calling_service_name = :callingServiceName")
    void grantDefaultPermissionBatch(@BindBean List<RoleBasedAccessRecord> roleBasedAccessRecord);

    @SqlBatch("insert into resource_attributes_audit (service_name, resource_type, resource_name, attribute, default_security_classification, calling_service_name, audit_timestamp, changed_by, action)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, cast(:defaultSecurityClassification as security_classification), :callingServiceName, CURRENT_TIMESTAMP, :changedBy, 'grant')"
    )
    void createResourceAttributeForAuditBatch(@BindBean List<ResourceAttribute> resourceAttributeAudit, String callingServiceName, String changedBy);

    @SqlBatch("insert into default_permissions_for_roles_audit (service_name, resource_type, resource_name, attribute, role_name, permissions, calling_service_name, audit_timestamp, changed_by, action)"
        + " values (:serviceName, :resourceType, :resourceName, :attributeAsString, :roleName, :permissionsAsInt, :callingServiceName, CURRENT_TIMESTAMP, :changedBy, 'grant' )"
    )
    void grantDefaultPermissionAuditBatch(@BindBean List<RoleBasedAccessRecord> roleBasedAccessAuditRecord, String callingServiceName, String changedBy);

    @SqlQuery("select role_name as role, permissions from default_permissions_for_roles where resource_type = 'case' and resource_name = :caseTypeId "
        + "and attribute = '' order by role_name")
    @RegisterConstructorMapper(DefaultRolePermissions.class)
    List<DefaultRolePermissions> getRolePermissionsForCaseType(String caseTypeId);

    @SqlQuery("select resource_name as caseTypeId, role_name as role, permissions from default_permissions_for_roles "
        + "where resource_type = 'case' and resource_name in (<caseTypeIds>) "
        + "and attribute = '' order by resource_name, role_name")
    @RegisterBeanMapper(RolePermissionsForCaseTypeEnvelope.class)
    @RegisterBeanMapper(DefaultRolePermissions.class)
    @UseRowReducer(RolePermissionsForCaseTypeEnvelopeReducer.class)
    List<RolePermissionsForCaseTypeEnvelope> getRolePermissionsForMultipleCaseTypes(@BindList List<String> caseTypeIds);

}
