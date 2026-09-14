package exam_management_syatem.app;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.ui.auth.LoginFrame;
import exam_management_syatem.ui.theme.AppTheme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Main application bootstrap entrypoint for the Exam Management System.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Configure Look and Feel first so any dialogs inherit the modern theme
        try {
            AppTheme.setup();
        } catch (Throwable t) {
            System.err.println("Theme setup warning: " + t.getMessage());
        }

        // 2. Global uncaught exception handler to prevent silent failures
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Paths.get("error.log"),
                        "[" + java.time.LocalDateTime.now() + "] Error: " + throwable.toString() + "\n" +
                                java.util.Arrays.stream(throwable.getStackTrace())
                                        .map(StackTraceElement::toString)
                                        .collect(java.util.stream.Collectors.joining("\n\tat ")) + "\n\n",
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND
                );
            } catch (Exception ignored) {}
        });

        // 3. Initialize PostgreSQL database schema & ensure admin user exists
        try {
            DatabaseManager.initializeDatabase();
        } catch (Throwable t) {
            System.err.println("PostgreSQL database initialization warning: " + t.getMessage());
            t.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to PostgreSQL database.\n\n" +
                        "Details: " + (t.getMessage() != null ? t.getMessage() : t.toString()) + "\n\n" +
                        "Please ensure PostgreSQL service is running on port 5432\n" +
                        "or configure database settings in 'conf/database.properties'.",
                        "Database Notice",
                        JOptionPane.WARNING_MESSAGE);
            });
        }

        // 4. Always launch the User Interface so the window appears reliably
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame.launch();
            } catch (Throwable t) {
                System.err.println("Failed to display LoginFrame: " + t.getMessage());
                t.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to display application window: " + t.getMessage(),
                        "Launch Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
