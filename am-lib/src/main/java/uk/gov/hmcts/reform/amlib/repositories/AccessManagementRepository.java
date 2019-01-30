package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface AccessManagementRepository {

    @SqlUpdate("insert into \"AccessManagement\" (\"resourceId\", \"accessorId\") values (?, ?)")
    void createAccessManagementRecord(String resourceId, String accessorId);

    @SqlQuery("select \"accessorId\" from \"AccessManagement\" where exists "
            + "(select 1 from \"AccessManagement\" where \"AccessManagement\".\"accessorId\" =  :accessorId) "
            + "and \"AccessManagement\".\"resourceId\" = :resourceId")
    List<String> checkAccess(@Bind("accessorId") String accessorId, @Bind("resourceId") String resourceId);
}
