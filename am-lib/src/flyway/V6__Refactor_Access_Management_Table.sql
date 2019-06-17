ALTER TABLE access_management
  ADD COLUMN access_type varchar(100) NOT NULL;

ALTER TABLE access_management
  ADD COLUMN service_name varchar(100) NOT NULL;

ALTER TABLE access_management
  ADD COLUMN resource_type varchar(100) NOT NULL;

ALTER TABLE access_management
  ADD COLUMN resource_name varchar(100) NOT NULL;

ALTER TABLE access_management
  ADD COLUMN attribute varchar(20) NOT NULL;

ALTER TABLE access_management
  ADD COLUMN security_classification varchar(100) NOT NULL;

ALTER TABLE access_management
  ADD CONSTRAINT access_management_resources_fkey FOREIGN KEY (service_name, resource_type, resource_name)
REFERENCES resources (service_name, resource_type, resource_name)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE access_management
  ADD CONSTRAINT access_management_unique UNIQUE (resource_id, accessor_id, access_type, attribute, resource_type, service_name, resource_name, security_classification);
