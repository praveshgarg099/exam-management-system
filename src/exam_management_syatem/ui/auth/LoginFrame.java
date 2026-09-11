package exam_management_syatem.ui.auth;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.shell.MainApplicationFrame;
import exam_management_syatem.ui.theme.AppTheme;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class LoginFrame extends JFrame implements LoginPanel.LoginListener {

    private final LoginPanel loginPanel;

    public LoginFrame() {
        this(LoginPanel.PortalMode.STUDENT);
    }

    public LoginFrame(LoginPanel.PortalMode initialMode) {
        AppTheme.setup();

        setTitle("Exam Management System — Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(840, 620));
        setSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Colors.BACKGROUND);

        AuthenticationService authService = new AuthenticationService();
        loginPanel = new LoginPanel(authService, this);
        loginPanel.setPortalMode(initialMode);

        setLayout(new BorderLayout());
        add(loginPanel, BorderLayout.CENTER);
    }

    @Override
    public void onLoginSuccess(UserSession session) {
        // 1. Dispose authentication frame cleanly
        dispose();

        // 2. Open authenticated MainApplicationFrame
        SwingUtilities.invokeLater(() -> {
            MainApplicationFrame appFrame = new MainApplicationFrame(session);
            appFrame.setVisible(true);
        });
    }

    public void setPortalMode(LoginPanel.PortalMode mode) {
        loginPanel.setPortalMode(mode);
    }

    public static void launch() {
        launch(LoginPanel.PortalMode.STUDENT);
    }

    public static void launchStudentMode() {
        launch(LoginPanel.PortalMode.STUDENT);
    }

    public static void launchAdminMode() {
        launch(LoginPanel.PortalMode.ADMIN);
    }

    public static void launch(LoginPanel.PortalMode mode) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(mode);
            frame.setVisible(true);
            frame.loginPanel.focusUsername();
        });
    }

    public static void main(String[] args) {
        // Initialize database if needed, then launch
        try {
            exam_management_syatem.db.DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization warning: " + e.getMessage());
        }
        launch();
    }
}
