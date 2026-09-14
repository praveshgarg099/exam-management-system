package exam_management_syatem.util;

import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Robust image loading utility supporting classpath resources, relative folders,
 * and legacy path normalization with silent failure handling.
 */
public class ImageLoader {

    public static ImageIcon loadImage(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            return null;
        }

        try {
            // Normalize path for classpath lookup
            String cleanPath = resourcePath.replace('\\', '/');
            if (cleanPath.startsWith("C:") || cleanPath.startsWith("/Volumes")) {
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
            URL imgURL = ImageLoader.class.getResource(cleanPath);
            if (imgURL == null && !cleanPath.startsWith("/images/")) {
                String filenameOnly = new File(cleanPath).getName();
                imgURL = ImageLoader.class.getResource("/images/" + filenameOnly);
            }
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
}
