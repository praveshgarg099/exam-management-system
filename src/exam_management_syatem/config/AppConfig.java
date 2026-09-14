package exam_management_syatem.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Production-ready application configuration manager.
 * Supports hierarchical resolution:
 * 1. Environment Variables (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
 * 2. External database.properties (from %APPDATA%, ~/.exammanagementsystem, conf/, or ./)
 * 3. Safe embedded defaults (Host: localhost, Port: 5432, DB: exam_management, User: postgres, Password: "")
 */
public class AppConfig {

    private static String dbHost = "localhost";
    private static String dbPort = "5432";
    private static String dbName = "exam_management";
    private static String dbUser = "postgres";
    private static String dbPassword = "";

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        // 1. Start with hardened platform defaults:
        // On Windows, standard PostgreSQL superuser is "postgres".
        // On macOS/Unix, Homebrew and local PostgreSQL default to the login user.
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
        dbHost = "localhost";
        dbPort = "5432";
        dbName = "exam_management";
        dbUser = isWindows ? "postgres" : System.getProperty("user.name", "postgres");
        dbPassword = "";

        // 2. Try loading from external properties file
        Properties props = new Properties();
        File propFile = findConfigurationFile();
        if (propFile != null && propFile.exists() && propFile.canRead()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
                if (props.getProperty("db.host") != null && !props.getProperty("db.host").trim().isEmpty()) {
                    dbHost = props.getProperty("db.host").trim();
                }
                if (props.getProperty("db.port") != null && !props.getProperty("db.port").trim().isEmpty()) {
                    dbPort = props.getProperty("db.port").trim();
                }
                if (props.getProperty("db.name") != null && !props.getProperty("db.name").trim().isEmpty()) {
                    dbName = props.getProperty("db.name").trim();
                }
                if (props.getProperty("db.user") != null && !props.getProperty("db.user").trim().isEmpty()) {
                    dbUser = props.getProperty("db.user").trim();
                }
                if (props.getProperty("db.password") != null) {
                    dbPassword = props.getProperty("db.password");
                }
            } catch (Exception e) {
                System.err.println("[AppConfig] Warning: Could not read configuration file: " + propFile.getAbsolutePath());
            }
        }

        // 3. Override with Environment Variables if set
        if (System.getenv("DB_HOST") != null && !System.getenv("DB_HOST").trim().isEmpty()) {
            dbHost = System.getenv("DB_HOST").trim();
        }
        if (System.getenv("DB_PORT") != null && !System.getenv("DB_PORT").trim().isEmpty()) {
            dbPort = System.getenv("DB_PORT").trim();
        }
        if (System.getenv("DB_NAME") != null && !System.getenv("DB_NAME").trim().isEmpty()) {
            dbName = System.getenv("DB_NAME").trim();
        }
        if (System.getenv("DB_USER") != null && !System.getenv("DB_USER").trim().isEmpty()) {
            dbUser = System.getenv("DB_USER").trim();
        }
        if (System.getenv("DB_PASSWORD") != null) {
            dbPassword = System.getenv("DB_PASSWORD");
        }
    }

    private static File findConfigurationFile() {
        // Option A: Explicit system property
        String customPath = System.getProperty("app.config.path");
        if (customPath != null && !customPath.trim().isEmpty()) {
            File f = new File(customPath.trim());
            if (f.exists()) return f;
        }

        // Option B: Windows %APPDATA%\ExamManagementSystem\database.properties
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.trim().isEmpty()) {
            File f = new File(new File(appData, "ExamManagementSystem"), "database.properties");
            if (f.exists()) return f;
        }

        // Option C: Unix/Mac ~/.exammanagementsystem/database.properties
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.trim().isEmpty()) {
            File f = new File(new File(userHome, ".exammanagementsystem"), "database.properties");
            if (f.exists()) return f;
        }

        // Option D: conf/database.properties in application root
        File confFile = new File("conf", "database.properties");
        if (confFile.exists()) return confFile;

        // Option E: database.properties in current directory
        File localFile = new File("database.properties");
        if (localFile.exists()) return localFile;

        return null;
    }

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

    public static String getAdminDbUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/postgres";
    }
}
