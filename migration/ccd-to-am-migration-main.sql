
BEGIN;

CREATE TEMP TABLE stage
(
    case_data_id VARCHAR, case_type_id VARCHAR,
    user_id VARCHAR, case_role VARCHAR
)
ON COMMIT DROP;

ALTER TABLE access_management DROP CONSTRAINT access_management_unique;
ALTER TABLE access_management DROP CONSTRAINT access_management_resources_fkey;
ALTER TABLE access_management DROP CONSTRAINT relationship_fkey;

\COPY stage FROM 'ccd.csv' DELIMITER ',' CSV HEADER;

SELECT COUNT(*) AS "columns to migrate" FROM stage;

WITH ins_access_management AS
(
    INSERT INTO access_management (resource_id, accessor_type, accessor_id,
            "attribute", permissions, service_name, resource_name,
            resource_type, relationship)
        SELECT s.case_data_id AS resource_id, 'USER' AS accessor_type,
            s.user_id AS accessor_id, ra."attribute" AS "attribute",
            dp.permissions AS permissions, re.service_name AS service_name,
            s.case_type_id AS resource_name, re.resource_type AS resource_type,
            s.case_role AS relationship
        FROM stage AS s, resource_attributes AS ra,
            default_permissions_for_roles AS dp, resources AS re
        WHERE ra.resource_name = s.case_type_id
        AND dp.resource_name = s.case_type_id
        AND re.resource_name = s.case_type_id
        AND s.case_role IN (SELECT role_name FROM roles)
    EXCEPT
        SELECT resource_id, accessor_type, accessor_id, "attribute",
            permissions, service_name, resource_name, resource_type,
            relationship
        FROM access_management
    RETURNING resource_id
)
SELECT COUNT(*) AS "access_management inserts" FROM ins_access_management;

ALTER TABLE access_management ADD CONSTRAINT access_management_unique
    UNIQUE (resource_id, accessor_id, accessor_type, "attribute", resource_type,
    service_name, resource_name, relationship);
ALTER TABLE access_management ADD CONSTRAINT access_management_resources_fkey
    FOREIGN KEY (service_name, resource_type, resource_name)
    REFERENCES resources(service_name, resource_type, resource_name);
ALTER TABLE access_management ADD CONSTRAINT relationship_fkey
    FOREIGN KEY (relationship)
    REFERENCES roles(role_name);

COMMIT;
