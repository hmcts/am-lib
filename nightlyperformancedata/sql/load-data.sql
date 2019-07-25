insert into services (service_name, service_description) values ('cmc', null)
on conflict on constraint services_pkey do nothing;

insert into resources (service_name, resource_type, resource_name) values ('cmc', 'case', 'claim')
 on conflict on constraint resources_pkey do nothing;

insert into roles (role_name, role_type, security_classification, access_type) values ('caseworker', cast('RESOURCE' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type))
 on conflict on constraint roles_pkey do update set role_type =cast('RESOURCE' as role_type), security_classification = cast('PUBLIC' as security_classification), access_type = cast('ROLE_BASED' as access_type);

insert into roles (role_name, role_type, security_classification, access_type) values ('Role 1', cast('RESOURCE' as role_type),cast('PUBLIC' as security_classification), cast('ROLE_BASED' as access_type))
 on conflict on constraint roles_pkey do update set role_type =cast('RESOURCE' as role_type), security_classification = cast('PUBLIC' as security_classification), access_type = cast('ROLE_BASED' as access_type);

insert into access_management (resource_id, accessor_id, permissions, accessor_type, service_name, resource_type,
  resource_name, attribute, relationship) values ('0011', '5511', 7, cast('USER' as accessor_type), 'cmc', 'case',
  'claim', '', 'caseworker'), ('0011', '5512', 3, cast('USER' as accessor_type), 'cmc', 'case', 'claim', '', null),
  ('0011', '5513', 15, cast('USER' as accessor_type), 'cmc', 'case', 'claim', '', 'caseworker');
