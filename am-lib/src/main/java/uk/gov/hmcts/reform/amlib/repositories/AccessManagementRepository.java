package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.List;

@SuppressWarnings("LineLength")
public interface AccessManagementRepository {

    @SqlUpdate("insert into access_management (resource_id, accessor_id, permissions, access_type, service_name, resource_type, resource_name, attribute, security_classification) "
        + "values (:resourceId, :accessorId, :permissions, :accessType, :serviceName, :resourceType, :resourceName, :attribute, :securityClassification)")
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

    @SqlQuery("select * from access_management where accessor_id=? and resource_id=?")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> getExplicitAccess(String accessorId, String resourceId);
}
