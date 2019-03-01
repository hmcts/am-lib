package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.RoleBasedAccessRecord;

import java.util.List;

@SuppressWarnings("LineLength")
public interface AccessManagementRepository {

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, access_type, service_name, resource_type, resource_name, attribute, security_classification) "
        + "values (:resourceId, :accessorId, :permissions, :accessType, :serviceName, :resourceType, :resourceName, :attribute, :securityClassification)"
        + "on conflict on constraint access_management_unique do update set permissions = :permissions"
    )
    void createAccessManagementRecord(@BindBean ExplicitAccessRecord explicitAccessRecord);

    @SqlUpdate("delete from access_management where "
        + "access_management.resource_id = :resourceId "
        + "and access_management.accessor_id = :accessorId "
        + "and access_management.access_type = :accessType "
        + "and access_management.service_name = :serviceName "
        + "and access_management.resource_type = :resourceType "
        + "and access_management.resource_name = :resourceName "
        + "and access_management.attribute = :attribute")
    void removeAccessManagementRecord(@BindBean ExplicitAccessMetadata explicitAccessMetadata);


    @SqlQuery("select accessor_id from access_management where exists "
        + "(select 1 from access_management where access_management.accessor_id = :accessorId "
        + "and access_management.resource_id = :resourceId) "
        + "and access_management.resource_id = :resourceId")
    List<String> getAccessorsList(@Bind("accessorId") String accessorId, @Bind("resourceId") String resourceId);

    // The 'LIMIT 1' suffix was introduced because at the current database state (V2.2) there is a technical
    // possibility of returning multiple records with this query, but it's forbidden from business point of view.
    @SqlQuery("select * from access_management where accessor_id=? and resource_id=? LIMIT 1")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    ExplicitAccessRecord getExplicitAccess(String accessorId, String resourceId);

    @SuppressWarnings("PMD")
    //Suppressing as PMD does not allow more than 3 string parameters to be passed in.
    @SqlQuery("select * from default_permissions_for_roles where service_name = :serviceName and resource_type = :resourceType and resource_name = :resourceName and role_name = :roleName")
    @RegisterConstructorMapper(RoleBasedAccessRecord.class)
    List<RoleBasedAccessRecord> getRolePermissions(String serviceName, String resourceType, String resourceName, String roleName);
}
