ALTER TABLE access_management_audit
    ALTER column action  SET not null;

ALTER TABLE default_permissions_for_roles_audit
    ALTER column action  SET not null;

ALTER TABLE resource_attributes_audit
    ALTER column action  SET not null;


