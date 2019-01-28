package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AccessManagementRepository {

    @SqlUpdate("insert into \"AccessManagement\" (\"resourceId\", \"accessorId\") values (:resourceId, :accessorId)")
    Integer createAccessManagementRecord(
            @Bind("resourceId") String resourceId,
            @Bind("accessorId") String accessorId
    );
}
