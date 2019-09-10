ALTER TABLE access_management
  ADD COLUMN last_update TIMESTAMP NOT NULL;

ALTER TABLE access_management
  ADD COLUMN calling_service_name VARCHAR(100) NOT NULL;
