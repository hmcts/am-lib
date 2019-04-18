package uk.gov.hmcts.reform.amlib.internal.repositories;

import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
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

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, accessor_type, service_name, resource_type, resource_name, attribute, security_classification, relationship) "
        + "values (:resourceId, :accessorId, :permissionsAsInt, cast(:accessorType as accessor_type), :serviceName, :resourceType, :resourceName, :attributeAsString, :securityClassification, :relationship)"
        + "on conflict on constraint access_management_unique do update set permissions = :permissionsAsInt")
    void createAccessManagementRecord(@BindBean ExplicitAccessRecord explicitAccessRecord);

    @SqlUpdate("delete from access_management where "
        + "access_management.resource_id = :resourceId "
        + "and access_management.accessor_id = :accessorId "
        + "and access_management.accessor_type = cast(:accessorType as accessor_type) "
        + "and access_management.service_name = :resourceDefinition.serviceName "
        + "and access_management.resource_type = :resourceDefinition.resourceType "
        + "and access_management.resource_name = :resourceDefinition.resourceName "
        + "and (:relationship is null or access_management.relationship = :relationship) "
        + "and (access_management.attribute = :attributeAsString "
        + "or access_management.attribute like concat(:attributeAsString, '/', '%'))")
    void removeAccessManagementRecord(@BindBean ExplicitAccessMetadata explicitAccessMetadata);

    @SqlQuery("select * from access_management where accessor_id=? and resource_id=?")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> getExplicitAccess(String accessorId, String resourceId);

    @SuppressWarnings("PMD.UseObjectForClearerAPI") // More than 3 parameters makes sense for now, subject to change
    @SqlQuery("select * from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName and role_name = :roleName")
    @RegisterConstructorMapper(RoleBasedAccessRecord.class)
    List<RoleBasedAccessRecord> getRolePermissions(@BindBean ResourceDefinition resourceDefinition, String roleName);

    @SqlQuery("select role_name from roles where role_name in (<userRoles>) and access_type = cast(:accessType as access_type)")
    Set<String> getRoles(@BindList("userRoles") Set<String> userRoles, AccessType accessType);

    @SqlQuery("select distinct service_name, resource_type, resource_name from default_permissions_for_roles where role_name in (<userRoles>) and permissions & 1 = 1 and attribute = ''")
    @RegisterConstructorMapper(ResourceDefinition.class)
    Set<ResourceDefinition> getResourceDefinitionsWithRootCreatePermission(@BindList("userRoles") Set<String> userRoles);
}
