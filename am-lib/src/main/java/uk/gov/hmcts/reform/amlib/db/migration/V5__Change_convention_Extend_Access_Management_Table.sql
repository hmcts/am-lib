ALTER TABLE "AccessManagement"
  RENAME TO access_management;

ALTER TABLE access_management
  RENAME COLUMN "accessManagementId" TO access_management_id;

ALTER TABLE access_management
  RENAME COLUMN "resourceId" TO resource_id;

ALTER TABLE access_management
  RENAME COLUMN "accessorId" TO accessor_id;
