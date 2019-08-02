package uk.gov.hmcts.reform.amlib.config;

import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrDefault;
import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrThrow;

public class DatabaseProperties {

    private final Server server;
    private final String database;
    private final Credentials credentials;

    private DatabaseProperties(Server server, String database, Credentials credentials) {
        this.server = server;
        this.database = database;
        this.credentials = credentials;
    }

    public static DatabaseProperties createFromEnvironmentProperties() {
        return new DatabaseProperties(
            new Server(
                getValueOrDefault("DATABASE_HOST", "localhost"),
                getValueOrDefault("DATABASE_PORT", "5432")
            ),
            getValueOrThrow("DATABASE_NAME"),
            new Credentials(
                getValueOrThrow("DATABASE_USER"),
                getValueOrThrow("DATABASE_PASS")
            )
        );
    }

    public Server getServer() {
        return server;
    }

    public String getDatabase() {
        return database;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public static class Server {
        private final String host;
        private final int port;

        private Server(String host, String port) {
            this.host = host != null ? host : "localhost";
            this.port = port != null ? Integer.parseInt(port) : 5432;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    public static class Credentials {
        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
