CREATE TYPE securityclassification AS ENUM ('Public', 'Private', 'Restricted');

CREATE TABLE roles (
  role_name varchar(100) PRIMARY KEY,
  role_type VARCHAR (50) NOT NULL,
  security_classification securityclassification NOT NULL
);

CREATE TABLE services (
  service_name varchar(100) PRIMARY KEY,
  service_description varchar(250)
);

CREATE TABLE resources (
  service_name varchar(100) NOT NULL,
  resource_type varchar(100) NOT NULL,
  resource_name varchar(100) NOT NULL,
  PRIMARY KEY (service_name, resource_type, resource_name),
  CONSTRAINT resources_service_name_fkey FOREIGN KEY (service_name)
    REFERENCES services (service_name) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE resource_attributes (
  service_name varchar(100) NOT NULL,
  resource_type varchar(100) NOT NULL,
  resource_name varchar(100) NOT NULL,
  attribute varchar(250) NOT NULL,
  default_security_classification securityclassification NOT NULL,
  PRIMARY KEY (service_name, resource_type, resource_name, attribute),
  CONSTRAINT resource_attributes_fkey FOREIGN KEY (service_name, resource_type, resource_name)
    REFERENCES resources (service_name, resource_type, resource_name) MATCH FULL
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE default_permissions_for_roles (
  service_name varchar(100) NOT NULL,
  resource_type varchar(100) NOT NULL,
  resource_name varchar(100) NOT NULL,
  attribute varchar(250) NOT NULL,
  role_name varchar(100) NOT NULL,
  permissions smallint NOT NULL DEFAULT 0,
  UNIQUE (service_name, resource_type, resource_name, attribute, role_name),
  CONSTRAINT default_permissions_for_roles_roleName_fkey FOREIGN KEY (role_name)
    REFERENCES roles (role_name) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);
