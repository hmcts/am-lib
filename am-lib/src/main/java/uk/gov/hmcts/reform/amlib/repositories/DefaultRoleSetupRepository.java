package uk.gov.hmcts.reform.amlib.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@SuppressWarnings("LineLength")
public interface DefaultRoleSetupRepository {
    @SqlUpdate("insert into services (service_name, service_description) values (:serviceName, :serviceDescription)" +
        " on conflict on constraint services_pkey do update set service_name = :serviceName, service_description = :serviceDescription")
    void addService(@Bind("serviceName") String serviceName, @Bind("serviceDescription") String serviceDescription);
}
