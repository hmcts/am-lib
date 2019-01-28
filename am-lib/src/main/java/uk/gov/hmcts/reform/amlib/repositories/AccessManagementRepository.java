package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AccessManagementRepository {

    @SqlUpdate("insert into \"AccessManagement\" (\"resourceId\", \"accessorId\") values (?, ?)")
    void createAccessManagementRecord(String resourceId, String accessorId);
}
