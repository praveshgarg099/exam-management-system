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
        // 1. Initialize PostgreSQL database schema & ensure admin user exists
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            System.err.println("PostgreSQL database initialization error: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to PostgreSQL database: " + e.getMessage() +
                    "\nPlease ensure PostgreSQL service is running.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // 2. Configure Look and Feel
        AppTheme.setup();

        // 3. Launch Authentication Experience
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame.launch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
