package exam_management_syatem.util;

/**
 * Common input and identifier validation routines.
 */
public class ValidationUtils {

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
