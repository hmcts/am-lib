insert into services (service_name, service_description) values ('cmc-test', null);

insert into resources (service_name, resource_type, resource_name) values ('cmc-test', 'case-test', 'claim-test');

insert into roles (role_name, role_type, security_classification, access_type) values ('caseworker-test', cast('RESOURCE' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type));

insert into roles (role_name, role_type, security_classification, access_type) values ('caseworker-test1', cast('IDAM' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type));
