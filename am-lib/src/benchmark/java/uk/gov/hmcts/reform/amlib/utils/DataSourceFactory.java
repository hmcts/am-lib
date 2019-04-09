package uk.gov.hmcts.reform.amlib.utils;

import org.postgresql.ds.PGPoolingDataSource;
import uk.gov.hmcts.reform.amlib.config.DatabaseProperties;

import javax.sql.DataSource;

public class DataSourceFactory {
    @SuppressWarnings({"deprecation"})
    public static DataSource createDataSource() {
        DatabaseProperties databaseProperties = DatabaseProperties.createFromEnvironmentProperties();

        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(databaseProperties.getServer().getHost());
        dataSource.setPortNumber(databaseProperties.getServer().getPort());
        dataSource.setDatabaseName(databaseProperties.getDatabase());
        dataSource.setUser(databaseProperties.getCredentials().getUsername());
        dataSource.setPassword(databaseProperties.getCredentials().getPassword());
        dataSource.setMaxConnections(64);
        return dataSource;
    }
}
