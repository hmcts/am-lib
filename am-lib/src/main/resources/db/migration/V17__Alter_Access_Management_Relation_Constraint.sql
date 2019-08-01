CREATE UNIQUE INDEX access_management_unique_null_relation
 ON access_management(resource_id, accessor_id, accessor_type, attribute, resource_type, service_name, resource_name)
where relationship IS NULL;

