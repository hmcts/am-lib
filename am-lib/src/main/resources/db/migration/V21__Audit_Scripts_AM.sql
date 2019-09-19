CREATE TYPE ACTION AS ENUM ('grant', 'revoke');

create table access_management_audit
(
    access_management_id    integer           not null,
    resource_id             VARCHAR(250)      not null,
    accessor_id             VARCHAR(100)      not null,
    permissions             integer  		      not null,
    accessor_type           accessor_type     not null,
    service_name            VARCHAR(100)      not null,
    resource_type           VARCHAR(100)      not null,
    resource_name           VARCHAR(100)      not null,
    attribute               VARCHAR(250)      not null,
    relationship            VARCHAR(100),
	  calling_service_name    VARCHAR(100),
    audit_timestamp		      TIMESTAMP 		    not null DEFAULT CURRENT_TIMESTAMP,
    changed_by              VARCHAR(100),
    action                  ACTION
);

create table default_permissions_for_roles_audit
(
    service_name  			    VARCHAR(100)       not null,
    resource_type 			    VARCHAR(100)       not null,
    resource_name 			    VARCHAR(100)       not null,
    attribute     			    VARCHAR(250)       not null,
    role_name     			    VARCHAR(100)       not null,
    permissions   			    SMALLINT  		     not null,
	  calling_service_name    VARCHAR(100),
    audit_timestamp		      TIMESTAMP 		    not null DEFAULT CURRENT_TIMESTAMP,
    changed_by              VARCHAR(100),
    action                  ACTION
);

create table resource_attributes_audit
(
    service_name                    VARCHAR(100)            not null,
    resource_type                   VARCHAR(100)            not null,
    resource_name                   VARCHAR(100)            not null,
    attribute                       VARCHAR(250)            not null,
    default_security_classification SECURITY_CLASSIFICATION not null,
	  calling_service_name            VARCHAR(100),
    audit_timestamp		              TIMESTAMP 		    not null DEFAULT CURRENT_TIMESTAMP,
    changed_by                      VARCHAR(100),
    action                          ACTION
);
