insert into services (service_name, service_description) values ('cmc-test', null)
on conflict on constraint services_pkey do nothing;

insert into resources (service_name, resource_type, resource_name) values ('cmc-test', 'case-test', 'claim-test')
 on conflict on constraint resources_pkey do nothing;

insert into roles (role_name, role_type, security_classification, access_type) values ('caseworker-test', cast('RESOURCE' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type))
 on conflict on constraint roles_pkey do update set role_type =cast('RESOURCE' as role_type), security_classification = cast('PUBLIC' as security_classification), access_type = cast('ROLE_BASED' as access_type);

insert into roles (role_name, role_type, security_classification, access_type) values ('caseworker-test1', cast('IDAM' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type))
 on conflict on constraint roles_pkey do update set role_type =cast('RESOURCE' as role_type), security_classification = cast('PUBLIC' as security_classification), access_type = cast('ROLE_BASED' as access_type);

