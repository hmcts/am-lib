package uk.gov.hmcts.reform.amlib.internal.repositories;

import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.Role;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.query.AttributeData;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.JsonPointerMapper;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.PermissionSetMapper;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Set;

@SuppressWarnings("LineLength")
@RegisterColumnMapper(JsonPointerMapper.class)
@RegisterColumnMapper(PermissionSetMapper.class)
public interface AccessManagementRepository {

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, relationship, last_update, calling_service_name) "
        + "values (:resourceId, :accessorId, :permissionsAsInt, cast(:accessorType as accessor_type), :serviceName, :resourceType, :resourceName, :attributeAsString, :relationship,"
        + " now() at time zone 'utc', :callingServiceName) "
        + "on conflict on constraint access_management_unique do update set permissions = :permissionsAsInt, "
        + "last_update = now() at time zone 'utc', calling_service_name = :callingServiceName")
    @GetGeneratedKeys("access_management_id")
    long grantAccessManagementWithNotNullRelationship(@BindBean ExplicitAccessRecord explicitAccessRecord);

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, relationship, last_update, calling_service_name) "
        + "values (:resourceId, :accessorId, :permissionsAsInt, cast(:accessorType as accessor_type), :serviceName, :resourceType, :resourceName, :attributeAsString, :relationship,"
        + " now() at time zone 'utc', :callingServiceName) "
        + "on conflict (resource_id, accessor_id, accessor_type, attribute, resource_type, service_name, resource_name) where relationship is null do update set permissions = :permissionsAsInt, "
        + " last_update = now() at time zone 'utc', calling_service_name = :callingServiceName")
    @GetGeneratedKeys("access_management_id")
    long grantAccessManagementWithNullRelationship(@BindBean ExplicitAccessRecord explicitAccessRecord);

    @SqlUpdate("delete from access_management where "
        + "access_management.resource_id = :resourceId "
        + "and access_management.accessor_id = :accessorId "
        + "and access_management.accessor_type = cast(:accessorType as accessor_type) "
        + "and (:resourceName is null or access_management.resource_name = :resourceName) "
        + "and (:serviceName is null or access_management.service_name = :serviceName) "
        + "and access_management.resource_type = :resourceType "
        + "and (:relationship is null or access_management.relationship = :relationship) "
        + "and (access_management.attribute = :attributeAsString or access_management.attribute like concat(:attributeAsString, '/', '%'))")
    void removeAccessManagementRecord(@BindBean ExplicitAccessMetadata explicitAccessMetadata);

    @SqlQuery("select * from access_management as am where "
        + "resource_id = :resourceId "
        + "and resource_type = :resourceType "
        + "and ((accessor_type = 'USER' and accessor_id = :accessorId) "
        + "or (accessor_type = 'ROLE' and accessor_id in (<userRoles>) and exists (select role_name from roles as r where r.role_name = am.accessor_id and cast(role_type as text) = 'IDAM')) "
        + "or (accessor_type = 'DEFAULT' and accessor_id = '*'))")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> getExplicitAccess(String accessorId, @BindList Set<String> userRoles, String resourceId, String resourceType);

    @SqlQuery("select * from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName and role_name = :roleName")
    @RegisterConstructorMapper(RoleBasedAccessRecord.class)
    List<RoleBasedAccessRecord> getRolePermissions(@BindBean ResourceDefinition resourceDefinition, String roleName);

    @SqlQuery("select distinct d.attribute, d.permissions, ra.default_security_classification from default_permissions_for_roles d"
        + " join resource_attributes ra on d.service_name = ra.service_name and d.resource_type = ra.resource_type and d.resource_name = ra.resource_name and d.attribute = ra.attribute"
        + " where d.service_name = :serviceName and d.resource_Type = :resourceType and d.resource_name = :resourceName and d.role_name = :roleName and cast(default_security_classification as text) in (<securityClassifications>)")
    @RegisterConstructorMapper(AttributeData.class)
    List<AttributeData> getAttributeDataForResource(@BindBean ResourceDefinition resourceDefinition, String roleName, @BindList Set<SecurityClassification> securityClassifications);

    @SqlQuery("select * from roles where role_name in (<userRoles>) and cast(access_type as text) in (<accessTypes>)")
    @RegisterConstructorMapper(Role.class)
    Set<Role> getRoles(@BindList Set<String> userRoles, @BindList Set<AccessType> accessTypes);

    @SqlQuery("select distinct default_perms.service_name, default_perms.resource_type, default_perms.resource_name from default_permissions_for_roles default_perms"
        + " join resource_attributes as resource on default_perms.service_name = resource.service_name and default_perms.resource_type = resource.resource_type and default_perms.resource_name = resource.resource_name"
        + " where default_perms.role_name in (<userRoles>) and default_perms.permissions & 1 = 1 and default_perms.attribute = '' and cast(resource.default_security_classification as text) in (<securityClassifications>)")
    @RegisterConstructorMapper(ResourceDefinition.class)
    Set<ResourceDefinition> getResourceDefinitionsWithRootCreatePermission(@BindList Set<String> userRoles, @BindList Set<SecurityClassification> securityClassifications);

    @SqlQuery("select * from access_management where resource_id=? and resource_name=? and resource_type =? and cast(accessor_type as text)  = ? and attribute ='' ")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> getExplicitAccessForResource(String resourceId, String resourceName, String resourceType, AccessorType accessorType);

    @SqlQuery("select relationship from access_management where resource_id = :caseId and accessor_id = :userId and cast(accessor_type as text) = 'USER' "
        + "and resource_type = 'case' and attribute = '' and permissions & 2 = 2 and relationship is not null order by relationship")
    List<String> getUserCaseRoles(String caseId, String userId);

    @SqlQuery("select resource_id from access_management where accessor_id = ? and cast(accessor_type as text) = 'USER' and resource_type = 'case' "
        + "and permissions & 2 = 2 and attribute = '' order by resource_id")
    List<String> getUserCases(String userId);


    @SqlUpdate("insert into access_management_audit (access_management_id, resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, relationship, calling_service_name, audit_timestamp, changed_by, action) "
        + "values (:access_management_id, :resourceId, :accessorId, :permissionsAsInt, cast(:accessorType as accessor_type), :serviceName, :resourceType, :resourceName, :attributeAsString, :relationship,"
        + " :callingServiceName, now() at time zone 'utc', :changedBy, 'grant' ) ")
    void grantAccessManagementForAudit(@Bind("access_management_id") long id, @BindBean ExplicitAccessRecord explicitAccessRecord, String callingServiceName, String changedBy);

    @SqlUpdate("insert into access_management_audit (access_management_id, resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, relationship, calling_service_name, audit_timestamp, changed_by, action) "
        + "select access_management_id, resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, relationship, :callingServiceName, now() at time zone 'utc', "
        + " :changedBy, 'revoke' from access_management where "
        + "access_management.resource_id = :resourceId "
        + "and access_management.accessor_id = :accessorId "
        + "and access_management.accessor_type = cast(:accessorType as accessor_type) "
        + "and (:resourceName is null or access_management.resource_name = :resourceName) "
        + "and (:serviceName is null or access_management.service_name = :serviceName) "
        + "and access_management.resource_type = :resourceType "
        + "and (:relationship is null or access_management.relationship = :relationship) "
        + "and (access_management.attribute = :attributeAsString or access_management.attribute like concat(:attributeAsString, '/', '%'))")
    void revokeAccessManagementForAudit(@BindBean ExplicitAccessMetadata explicitAccessMetadata);

}
