package exam_management_syatem.config;

public class AppConfig {
    private static String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
    private static String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "exam_management";
    private static String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getProperty("user.name", "postgres");
    private static String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";

    public static String getDbHost() {
        return dbHost;
    }

    public static void setDbHost(String host) {
        if (host != null && !host.trim().isEmpty()) {
            dbHost = host.trim();
        }
    }

    public static String getDbPort() {
        return dbPort;
    }

    public static void setDbPort(String port) {
        if (port != null && !port.trim().isEmpty()) {
            dbPort = port.trim();
        }
    }

    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            dbName = name.trim();
        }
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static void setDbUser(String user) {
        if (user != null && !user.trim().isEmpty()) {
            dbUser = user.trim();
        }
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void setDbPassword(String password) {
        dbPassword = password != null ? password : "";
    }

    public static String getDbUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }
}
