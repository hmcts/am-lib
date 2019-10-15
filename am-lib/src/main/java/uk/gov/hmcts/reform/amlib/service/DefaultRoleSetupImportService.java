package uk.gov.hmcts.reform.amlib.service;

import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@SuppressWarnings({"PMD.TooManyMethods","PMD.UseObjectForClearerAPI"})
public interface DefaultRoleSetupImportService {

    void addService(@NotBlank String serviceName);

    void addService(@NotBlank String serviceName, String serviceDescription);

    void addRole(@NotBlank String roleName, @NotNull RoleType roleType,
                 @NotNull SecurityClassification securityClassification,
                 @NotNull AccessType accessType);

    void addResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition);

    void grantDefaultPermission(@NotNull @Valid DefaultPermissionGrant accessGrant);


    void truncateDefaultPermissionsForService(@NotBlank String serviceName, @NotBlank String resourceType,
                                              String callingServiceName, String changedBy);

    void truncateDefaultPermissionsByResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition,
                                                        String callingServiceName, String changedBy);

    void deleteResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition);

    void deleteRole(@NotBlank String roleName);

    void deleteService(@NotBlank String serviceName);

    //takes input a map of  CaseTypeId as key and the list of object of type DefaultPermissionGrant as the value
    //object of type DefaultPermissionGrant identifies one record in the case definition file for a CaseTypeId
    void grantResourceDefaultPermissions(@NotEmpty Map<@NotNull String,
        @NotEmpty List<@NotNull @Valid DefaultPermissionGrant>> mapAccessGrant);


    RolePermissionsForCaseTypeEnvelope getRolePermissionsForCaseType(@NotBlank String caseTypeId);

    List<RolePermissionsForCaseTypeEnvelope> getRolePermissionsForCaseType(@NotEmpty List<String> caseTypeId);
}
