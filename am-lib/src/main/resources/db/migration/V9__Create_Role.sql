INSERT INTO roles VALUES ('case', 'ccd-user', 'Public');
INSERT INTO resource_attributes VALUES ('Service 1', 'Resource Type 1', 'resource', '/test', 'Public');
INSERT INTO resource_attributes VALUES ('Service 1', 'Resource Type 1', 'resource', '/test2', 'Public');
INSERT INTO resource_attributes VALUES ('Service 1', 'Resource Type 1', 'resource', '/testCreate', 'Public');
INSERT INTO default_permissions_for_roles VALUES ('Service 1', 'Resource Type 1', 'resource', '/test', 'case', 2);
INSERT INTO default_permissions_for_roles VALUES ('Service 1', 'Resource Type 1', 'resource', '/test2', 'case', 1);
