CREATE TYPE ACCESSOR_TYPE AS enum ('USER', 'ROLE');

ALTER TABLE access_management
  DROP CONSTRAINT access_management_unique;

UPDATE access_management SET access_type = UPPER(access_type);

ALTER TABLE access_management
  RENAME COLUMN access_type TO accessor_type;

ALTER TABLE access_management
  ADD COLUMN relationship VARCHAR(100) NOT NULL;

ALTER TABLE access_management
  ADD CONSTRAINT relationship_fkey FOREIGN KEY (relationship)
    REFERENCES roles (role_name)
    ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE access_management
  ALTER COLUMN accessor_type TYPE ACCESSOR_TYPE USING accessor_type::ACCESSOR_TYPE;

ALTER TABLE access_management
  ADD CONSTRAINT access_management_unique UNIQUE (resource_id, accessor_id, accessor_type, attribute, resource_type, service_name, resource_name, relationship);

ALTER TABLE roles
  RENAME COLUMN access_management_type TO access_type;
