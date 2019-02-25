ALTER TABLE roles
  ADD COLUMN access_management_type varchar(100) NOT NULL;

ALTER TABLE roles
  ALTER COLUMN security_classification TYPE varchar(100);

