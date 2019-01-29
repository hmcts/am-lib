CREATE TYPE "SecurityClassification" AS ENUM ('Public', 'Private', 'Restricted');

CREATE TABLE "Roles" (
  "roleName" varchar(100) PRIMARY KEY,
  "roleType" VARCHAR (50) NOT NULL,
  "securityClassification" "SecurityClassification" NOT NULL
);

CREATE TABLE "Services" (
  "serviceName" varchar(100) PRIMARY KEY,
  "serviceDescription" varchar(250)
);

CREATE TABLE "Resources" (
  "serviceName" varchar(100) NOT NULL,
  "resourceType" varchar(100) NOT NULL,
  "resourceName" varchar(100) NOT NULL,
  PRIMARY KEY ("serviceName", "resourceType", "resourceName"),
  CONSTRAINT "Resources_serviceName_fkey" FOREIGN KEY ("serviceName")
    REFERENCES "Services" ("serviceName") MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE "ResourceAttributes" (
  "serviceName" varchar(100) NOT NULL,
  "resourceType" varchar(100) NOT NULL,
  "resourceName" varchar(100) NOT NULL,
  "attribute" varchar(250) NOT NULL,
  "defaultSecurityClassification" "SecurityClassification" NOT NULL,
  PRIMARY KEY ("serviceName", "resourceType", "resourceName", "attribute"),
  CONSTRAINT "ResourceAttributes_fkey" FOREIGN KEY ("serviceName", "resourceType", "resourceName")
    REFERENCES "Resources" ("serviceName", "resourceType", "resourceName") MATCH FULL
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE "DefaultPermissionsForRoles" (
  "serviceName" varchar(100) NOT NULL,
  "resourceType" varchar(100) NOT NULL,
  "resourceName" varchar(100) NOT NULL,
  "attribute" varchar(250) NOT NULL,
  "roleName" varchar(100) NOT NULL,
  "permissions" smallint NOT NULL DEFAULT 0,
  UNIQUE ("serviceName", "resourceType", "resourceName", "attribute", "roleName"),
  CONSTRAINT "DefaultPermissionsForRoles_roleName_fkey" FOREIGN KEY ("roleName")
    REFERENCES "Roles" ("roleName") MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);
