CREATE TYPE SECURITYCLASSIFICATION AS ENUM ('Public', 'Private', 'Restricted');

CREATE TABLE roles (
  role_name VARCHAR(100) PRIMARY KEY,
  role_type VARCHAR (50) NOT NULL,
  security_classification SECURITYCLASSIFICATION NOT NULL
);

CREATE TABLE services (
  service_name VARCHAR(100) PRIMARY KEY,
  service_description VARCHAR(250)
);

CREATE TABLE resources (
  service_name VARCHAR(100) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (service_name, resource_type, resource_name),
  CONSTRAINT resources_service_name_fkey FOREIGN KEY (service_name)
    REFERENCES services (service_name)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE resource_attributes (
  service_name VARCHAR(100) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_name VARCHAR(100) NOT NULL,
  attribute VARCHAR(250) NOT NULL,
  default_security_classification SECURITYCLASSIFICATION NOT NULL,
  PRIMARY KEY (service_name, resource_type, resource_name, attribute),
  CONSTRAINT resource_attributes_resources_fkey FOREIGN KEY (service_name, resource_type, resource_name)
    REFERENCES resources (service_name, resource_type, resource_name)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE default_permissions_for_roles (
  service_name VARCHAR(100) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_name VARCHAR(100) NOT NULL,
  attribute VARCHAR(250) NOT NULL,
  role_name VARCHAR(100) NOT NULL,
  permissions SMALLINT NOT NULL DEFAULT 0,
  UNIQUE (service_name, resource_type, resource_name, attribute, role_name),
  CONSTRAINT default_permissions_for_roles_roleName_fkey FOREIGN KEY (role_name)
    REFERENCES roles (role_name)
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT default_permissions_for_roles_resource_attributes_fkey FOREIGN KEY (service_name, resource_type, resource_name, attribute)
    REFERENCES resource_attributes (service_name, resource_type, resource_name, attribute)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

