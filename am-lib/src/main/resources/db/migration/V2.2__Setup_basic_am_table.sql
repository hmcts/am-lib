create table "AccessManagement" (
	"accessManagementId" serial primary key,
 	"resourceId" varchar(250) not null,
	"accessorId" varchar(100) not null
);
