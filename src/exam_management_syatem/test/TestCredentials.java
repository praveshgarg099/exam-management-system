package exam_management_syatem.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Helper to securely load administrator credentials for automated test suites
 * without hardcoding or exposing them in the public repository codebase.
 */
public class TestCredentials {

    private static String adminUsername;
    private static String adminPassword;

    static {
        load();
    }

    private static synchronized void load() {
        // 1. Check environment variables
        adminUsername = System.getenv("ADMIN_USERNAME");
        adminPassword = System.getenv("ADMIN_PASSWORD");

        // 2. Check system properties (-Dadmin.username / -Dadmin.password)
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            adminUsername = System.getProperty("admin.username");
        }
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            adminPassword = System.getProperty("admin.password");
        }

        // 3. Check git-ignored test-credentials.properties file
        if (adminUsername == null || adminUsername.trim().isEmpty()
                || adminPassword == null || adminPassword.trim().isEmpty()) {
            File propFile = new File("test-credentials.properties");
            if (propFile.exists() && propFile.canRead()) {
                try (FileInputStream in = new FileInputStream(propFile)) {
                    Properties props = new Properties();
                    props.load(in);
                    if ((adminUsername == null || adminUsername.trim().isEmpty())
                            && props.getProperty("admin.username") != null) {
                        adminUsername = props.getProperty("admin.username").trim();
                    }
                    if ((adminPassword == null || adminPassword.trim().isEmpty())
                            && props.getProperty("admin.password") != null) {
                        adminPassword = props.getProperty("admin.password").trim();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 4. Safe fallback if unconfigured
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            adminUsername = "admin";
        }
        if (adminPassword == null) {
            adminPassword = "";
        }
    }

    public static String getAdminUsername() {
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            load();
        }
        return adminUsername;
    }

    public static String getAdminPassword() {
        if (adminPassword == null) {
            load();
        }
        return adminPassword;
    }
}
