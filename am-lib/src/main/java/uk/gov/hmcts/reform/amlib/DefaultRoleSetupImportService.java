package uk.gov.hmcts.reform.amlib;

import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.repositories.DefaultRoleSetupRepository;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    public DefaultRoleSetupImportService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public void addService(@NonNull String serviceName, String serviceDescription) {
        if (serviceName.isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be empty");
        }
        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, serviceDescription));
    }
}
