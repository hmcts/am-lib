CREATE TYPE ACCESS_TYPE AS enum ('USER', 'ROLE');

ALTER TABLE access_management
DROP CONSTRAINT access_management_unique;

ALTER TABLE access_management
  ADD COLUMN relationship VARCHAR(100) NOT NULL;

ALTER TABLE access_management
ADD CONSTRAINT relationship_fkey FOREIGN KEY (relationship)
    REFERENCES roles (role_name)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE access_management
  ADD CONSTRAINT relationship_unique UNIQUE (resource_id, accessor_id, access_type, attribute, resource_type, service_name, resource_name, relationship);
