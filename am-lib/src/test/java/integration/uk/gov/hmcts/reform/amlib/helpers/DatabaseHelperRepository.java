package integration.uk.gov.hmcts.reform.amlib.helpers;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.internal.models.Role;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.Service;
import uk.gov.hmcts.reform.amlib.internal.models.query.AttributeData;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.JsonPointerMapper;
import uk.gov.hmcts.reform.amlib.internal.repositories.mappers.PermissionSetMapper;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Set;

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

    @SqlQuery("select * from services "
        + "where service_name = :serviceName")
    @RegisterConstructorMapper(Service.class)
    Service getService(String serviceName);

    @SqlQuery("select * from resources "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName")
    @RegisterBeanMapper(ResourceDefinition.class)
    ResourceDefinition getResourcesDefinition(String serviceName, String resourceType, String resourceName);

    @SqlQuery("select * from resource_attributes "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and default_security_classification = cast(:securityClassification as security_classification)")
    @RegisterConstructorMapper(ResourceAttribute.class)
    ResourceAttribute getResourceAttribute(@BindBean ResourceDefinition resourceDefinition, String attribute, SecurityClassification securityClassification);

    @SqlQuery("select count(1) from access_management "
        + "where resource_id = :resourceId")
    int countExplicitPermissions(String resourceId);

    @SqlQuery("select * from access_management "
        + "where resource_id = :resourceId")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    List<ExplicitAccessRecord> findExplicitPermissions(String resourceId);

    @SqlQuery("select * from access_management "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and relationship = :relationship "
        + "and permissions = :permissions")
    @RegisterConstructorMapper(ExplicitAccessRecord.class)
    ExplicitAccessRecord getExplicitAccessRecordsForAudit(@BindBean ResourceDefinition resourceDefinition, String attribute,
                                                          String relationship, int permissions);

    @SqlQuery("select count(1) from default_permissions_for_roles "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and role_name = :roleName "
        + "and permissions = :permissions")
    int countDefaultPermissions(@BindBean ResourceDefinition resourceDefinition, String attribute, String roleName, int permissions);

    @SqlQuery("select * from default_permissions_for_roles "
        + "where service_name = :serviceName "
        + "and resource_type = :resourceType "
        + "and resource_name = :resourceName "
        + "and attribute = :attribute "
        + "and role_name = :roleName "
        + "and permissions = :permissions")
    @RegisterConstructorMapper(RoleBasedAccessRecord.class)
    RoleBasedAccessRecord getDefaultPermissionsForAudit(@BindBean ResourceDefinition resourceDefinition, String attribute, String roleName, int permissions);

    @SqlQuery("select distinct d.attribute, d.permissions, ra.default_security_classification, ra.last_update, ra.calling_service_name from default_permissions_for_roles d"
        + " join resource_attributes ra on d.service_name = ra.service_name and d.resource_type = ra.resource_type and d.resource_name = ra.resource_name and d.attribute = ra.attribute"
        + " where d.service_name = :serviceName and d.resource_Type = :resourceType and d.resource_name = :resourceName and d.role_name = :roleName and cast(default_security_classification as text) in (<securityClassifications>)")
    @RegisterConstructorMapper(AttributeData.class)
    List<AttributeData> getAttributeDataForResource(@BindBean ResourceDefinition resourceDefinition, String roleName, @BindList Set<SecurityClassification> securityClassifications);

}
