package exam_management_syatem;

import exam_management_syatem.util.ImageLoader;
import exam_management_syatem.util.ValidationUtils;

import javax.swing.ImageIcon;

/**
 * Backward compatibility facade delegating to specialized utilities
 * in {@link exam_management_syatem.util}.
 */
public class AppUtils {

    /**
     * Safely loads an ImageIcon from classpath resources or relative file paths.
     */
    public static ImageIcon loadImage(String resourcePath) {
        return ImageLoader.loadImage(resourcePath);
    }

    /**
     * Validates subject/table names to prevent SQL DDL Injection.
     */
    public static boolean isValidIdentifier(String name) {
        return ValidationUtils.isValidIdentifier(name);
    }
}
