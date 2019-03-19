package uk.gov.hmcts.reform.amlib.internal.repositories;

import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
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

@SuppressWarnings("LineLength")
@RegisterColumnMapper(JsonPointerMapper.class)
@RegisterColumnMapper(PermissionSetMapper.class)
public interface AccessManagementRepository {

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, access_type, service_name, resource_type, resource_name, attribute, security_classification) "
        + "values (:resourceId, :accessorId, :permissionsAsInt, :accessType, :serviceName, :resourceType, :resourceName, :attributeAsString, :securityClassification)"
        + "on conflict on constraint access_management_unique do update set permissions = :permissionsAsInt"
    )
    void createAccessManagementRecord(@BindBean ExplicitAccessRecord explicitAccessRecord);

    @SqlUpdate("delete from access_management where "
        + "access_management.resource_id = :resourceId "
        + "and access_management.accessor_id = :accessorId "
        + "and access_management.access_type = :accessType "
        + "and access_management.service_name = :serviceName "
        + "and access_management.resource_type = :resourceType "
        + "and access_management.resource_name = :resourceName "
        + "and access_management.attribute = :attributeAsString "
        + "or access_management.attribute like concat(:attributeAsString, '/', '%')")
    void removeAccessManagementRecord(@BindBean ExplicitAccessMetadata explicitAccessMetadata);

    @SqlQuery("select accessor_id from access_management where exists "
        + "(select 1 from access_management where access_management.accessor_id = :accessorId "
        + "and access_management.resource_id = :resourceId) "
        + "and access_management.resource_id = :resourceId")
    List<String> getAccessorsList(String accessorId, String resourceId);

    @SqlQuery("select * from access_management where accessor_id=? and resource_id=?")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> getExplicitAccess(String accessorId, String resourceId);

    @SuppressWarnings("PMD") // UseObjectForClearerAPI: more than 3 parameters makes sense for now, subject to change
    @SqlQuery("select * from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName and role_name = :roleName")
    @RegisterConstructorMapper(RoleBasedAccessRecord.class)
    List<RoleBasedAccessRecord> getRolePermissions(@BindBean ResourceDefinition resource, String roleName);

    @SqlQuery("select access_management_type from roles where role_name = :roleName")
    AccessType getRoleAccessType(String roleName);
}
