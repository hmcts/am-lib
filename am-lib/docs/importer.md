# How to import default permissions for roles from CCD?

AM is able to store and retrive default permissions for roles (permissions that are already defined in definition spreadsheets).

If you want to import it you need to make sure you do it in proper order as there is quite a lot of db constraints that provided data need to match.

## When you start with empty database

Please see documentation about db on confluence first. 
If you want to delete existing data from database please see "How to delete permissions" section below.

1. Add service first

Before you can add anything else please make sure you have added any service to `services` table. To do it you can use this method:

```public void addService(@NotBlank String serviceName, String serviceDescription)```

for example:
```addService("CMC", "Civil Money Case")```

Description is not mandatory. Please keep in mind that name of service is unique primary key and you will overwrite description if you provide name of service that already exists, but it will not throw any exception.

2. Add roles

Roles are more complex as you have 4 values (and all are mandatory).
```public void addRole(@NotBlank String roleName, @NotNull RoleType roleType, @NotNull SecurityClassification securityClassification, @NotNull AccessType accessType)```

For example:
```addRole("citizen", RoleType.IDAM, SecurityClassification.PUBLIC, AccessManagementType.EXPLICIT_ACCESS)```

Possible options for `RoleType`:
* IDAM

It's a global (IdAM) role, such as 'citizen', 'caseworker', 'solicitor'.

* RESOURCE

It's a role related to a resource (for CCD it's "case role"), eg. 'defendant', 'claimant', 'applicant'.

Possible options for `AccessManagementType`:
* EXPLICIT_ACCESS

User who has role of this access management type must be granted explicit access if you want him to have any access to a resource. There must be a link (relationship) between him and a resource.

* ROLE_BASED

User who has role with `ROLE_BASED` access doesn't have to be granted explicit access to a case and he may still have access to it if default permissions for his role (with ROLE_BASED access) gives him it.

3. Add resource definitions
By "resource definition" we understand 3 values: service, resource type and resource name (all mandatory, combined unique):
```public void addResourceDefinition(@NotBlank String serviceName, @NotBlank String resourceType, @NotBlank String resourceName)```

For example:
```addResourceDefinition("CMC", "CASE", "CivilMoneyCase")```

Why 'CASE' needs to be provided? At the moment we will support only cases, but in the upcoming releases we are going to support also documents and maybe even other types of resources.

In CCD:
- serviceName = jurisdiction
- resourceName = case type
- resourceType is always "CASE"

4. Grant default permissions for a role
```public void grantDefaultPermission(@NotNull @Valid DefaultPermissionGrant accessGrant```

## How to delete permissions?

You can only truncate all default permissions, in two ways:

1. by `serviceName` and `resourceType`, eg:

```truncateDefaultPermissionsForService("CMC", "CASE");```

It will delete all default permissions for all resource names within provided service.

2. by `serviceName`, `resourceType` and `resourceName` eg:

```truncateDefaultPermissionsForService("CMC", "CASE", "CivilMonayCase");```

will delete all "CivilMonayCase", but no other cases definitions within CMC. One service may have more than 1 case type.

To clean up database you need to delete rows from database in the correct order.

1. Delete default permissions (as described above)
2. Delete resource definitions
```public void deleteResourceDefinition(@NotBlank String serviceName, @NotBlank String resourceType, @NotBlank String resourceName)```

for example:
```deleteResourceDefinition("CMC", "CASE", "CivilMoneyCase")```

3. Delete roles
```public void deleteRole(@NotBlank String roleName)```

eg:
```public void deleteRole("citizen")```

This can fail if this role is used as a relationship in `access_management` table.

4. Delete services
```public void deleteService(@NotBlank String serviceName)```

eg
```deleteService("CMC")```

## No cascade on delete

Please mind there is no cascade on delete. In other words, when you try to remove service, but you haven't deleted resource definitions that are using this service (fk constraint) it will cause an error.
