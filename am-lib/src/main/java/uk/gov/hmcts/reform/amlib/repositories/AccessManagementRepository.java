package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AccessManagementRepository {

    @SqlUpdate("insert into \"AccessManagement\" (\"resourceId\", \"accessorId\") values (?, ?)")
    void createAccessManagementRecord(String resourceId, String accessorId);

    @SqlQuery("select exists(select 1 from \"AccessManagement\" where  \"accessorId\"=? and \"resourceId\"=?)")
    boolean explicateAccessExist(String accessorId, String resourceId);
}
