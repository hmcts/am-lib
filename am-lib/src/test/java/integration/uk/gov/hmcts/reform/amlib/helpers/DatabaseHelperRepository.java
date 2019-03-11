package integration.uk.gov.hmcts.reform.amlib.helpers;

import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.Role;
import uk.gov.hmcts.reform.amlib.models.Service;
import uk.gov.hmcts.reform.amlib.repositories.mappers.JsonPointerMapper;
import uk.gov.hmcts.reform.amlib.repositories.mappers.PermissionSetMapper;

@SuppressWarnings({"PMD", "LineLength"})
@RegisterColumnMapper(JsonPointerMapper.class)
@RegisterColumnMapper(PermissionSetMapper.class)
public interface DatabaseHelperRepository {

    @SqlUpdate("delete from access_management;"
        + "delete from default_permissions_for_roles;"
        + "delete from resource_attributes;"
        + "delete from resources;"
        + "delete from services;"
        + "delete from roles;")
    void truncateTables();

    @SqlQuery("select * from roles "
        + "where role_name = :roleName")
    @RegisterConstructorMapper(Role.class)
    Role getRole(String roleName);

    @SqlQuery("select attribute from access_management "
        + "where access_management.resource_id = :resourceId")
    String getResource(String resourceId);

    @SqlQuery("select * from services "
        + "where service_name = :serviceName")
    @RegisterConstructorMapper(Service.class)
    Service getService(String serviceName);

    @SqlQuery("select * from resources "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName")
    @RegisterConstructorMapper(ResourceDefinition.class)
    ResourceDefinition getResourcesDefinition(String serviceName, String resourceType, String resourceName);

    @SqlQuery("select * from resource_attributes "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and default_security_classification = cast(:securityClassification as security_classification)")
    @RegisterConstructorMapper(ResourceAttribute.class)
    ResourceAttribute getResourceAttribute(String serviceName, String resourceType, String resourceName, String attribute, SecurityClassification securityClassification);

    @SqlQuery("select count(1) from access_management "
        + "where resource_id = :resourceId")
    int countExplicitPermissions(String resourceId);

    @SqlQuery("select count(1) from default_permissions_for_roles "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and role_name = :roleName "
        + "and permissions = :permissions")
    int countDefaultPermissions(String serviceName, String resourceType, String resourceName, String attribute, String roleName, int permissions);
}
