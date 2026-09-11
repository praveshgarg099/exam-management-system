package exam_management_syatem.config;

import java.io.File;

public class AppConfig {
    private static String dbPath = "exam_management.db";

    public static String getDbPath() {
        return dbPath;
    }

    public static void setDbPath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            dbPath = path;
        }
    }

    public static String getDbUrl() {
        File file = new File(dbPath);
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }
}
