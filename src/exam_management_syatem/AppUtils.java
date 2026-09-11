package exam_management_syatem;

import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

public class AppUtils {

    /**
     * Safely loads an ImageIcon from classpath resources or relative file paths.
     * Prevents NullPointerExceptions or crashes if an image file is missing.
     */
    public static ImageIcon loadImage(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            return null;
        }

        try {
            // Normalize path for classpath lookup
            String cleanPath = resourcePath.replace('\\', '/');
            if (cleanPath.startsWith("C:") || cleanPath.startsWith("/Volumes")) {
                // Extract filename from legacy hardcoded absolute paths
                int lastSlash = Math.max(cleanPath.lastIndexOf('/'), cleanPath.lastIndexOf('\\'));
                if (lastSlash != -1) {
                    cleanPath = "/images/" + cleanPath.substring(lastSlash + 1);
                } else {
                    cleanPath = "/images/" + cleanPath;
                }
            } else if (!cleanPath.startsWith("/")) {
                cleanPath = "/" + cleanPath;
            }

            // 1. Try loading from classpath
            URL imgURL = AppUtils.class.getResource(cleanPath);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            }

            // 2. Try loading from relative images folder
            String filename = new File(cleanPath).getName();
            File localFile = new File("images", filename);
            if (localFile.exists()) {
                return new ImageIcon(localFile.getAbsolutePath());
            }

            // 3. Fallback to raw path as File if exists
            File rawFile = new File(resourcePath);
            if (rawFile.exists()) {
                return new ImageIcon(rawFile.getAbsolutePath());
            }
        } catch (Exception e) {
            // Silently swallow icon loading errors so GUI loads cleanly
        }
        return null;
    }

    /**
     * Validates subject/table names to prevent SQL DDL Injection.
     * Only allows alphanumeric characters and underscores.
     */
    public static boolean isValidIdentifier(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z0-9_]+$");
    }
}
