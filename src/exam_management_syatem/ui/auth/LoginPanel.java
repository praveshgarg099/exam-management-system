package exam_management_syatem.ui.auth;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppPasswordField;
import exam_management_syatem.ui.components.AppTextField;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;

public class LoginPanel extends JPanel {

    public interface LoginListener {
        void onLoginSuccess(UserSession session);
    }

    public enum PortalMode {
        STUDENT,
        ADMIN
    }

    private final AuthenticationService authService;
    private final LoginListener loginListener;

    private PortalMode currentMode = PortalMode.STUDENT;

    private JButton studentTabBtn;
    private JButton adminTabBtn;
    private JLabel formSubtitle;
    private JLabel usernameLabel;
    private AppTextField usernameField;
    private AppPasswordField passwordField;
    private AppButton signInButton;
    private JPanel errorBanner;
    private JLabel errorLabel;

    public LoginPanel(AuthenticationService authService, LoginListener loginListener) {
        this.authService = authService;
        this.loginListener = loginListener;

        setLayout(new GridBagLayout());
        setBackground(Colors.BACKGROUND);

        initComponents();
    }

    private void initComponents() {
        // Main container card: 2 columns (Brand on Left, Form on Right)
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(2, 4, w - 4, h - 4, Dimensions.BORDER_RADIUS * 2, Dimensions.BORDER_RADIUS * 2);

                // Card surface
                g2.setColor(Colors.SURFACE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, Dimensions.BORDER_RADIUS * 2, Dimensions.BORDER_RADIUS * 2);

                // Border
                g2.setColor(Colors.BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, Dimensions.BORDER_RADIUS * 2, Dimensions.BORDER_RADIUS * 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(840, 560));
        card.setMinimumSize(new Dimension(720, 500));

        // 1. LEFT BRAND PANEL
        JPanel brandPanel = createBrandPanel();
        card.add(brandPanel, BorderLayout.WEST);

        // 2. RIGHT FORM PANEL
        JPanel formPanel = createFormPanel();
        card.add(formPanel, BorderLayout.CENTER);

        add(card);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Sleek slate gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, Color.decode("#0F172A"),
                    0, h, Color.decode("#1E293B")
                );
                g2.setPaint(gp);
                // Round left corners to match outer card
                g2.fillRoundRect(0, 0, w + Dimensions.BORDER_RADIUS * 2, h, Dimensions.BORDER_RADIUS * 2, Dimensions.BORDER_RADIUS * 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(340, 560));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(Spacing.XXXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // Header section
        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("ACCREDITED PLATFORM");
        badge.setFont(Typography.CAPTION);
        badge.setForeground(Color.decode("#38BDF8")); // Sky blue
        topSection.add(badge);
        topSection.add(Box.createRigidArea(new Dimension(0, Spacing.SM)));

        JLabel title = new JLabel("Exam Management");
        title.setFont(Typography.H1);
        title.setForeground(Color.WHITE);
        topSection.add(title);

        JLabel subtitle = new JLabel("System");
        subtitle.setFont(Typography.H1);
        subtitle.setForeground(Color.decode("#94A3B8"));
        topSection.add(subtitle);

        topSection.add(Box.createRigidArea(new Dimension(0, Spacing.XL)));

        // Feature checklist
        addFeatureItem(topSection, "Authoritative Exam Timer", "Server-synchronized countdowns ensure integrity.");
        addFeatureItem(topSection, "Tamper-Proof Attempts", "Strict PostgreSQL locking & no-retake invariants.");
        addFeatureItem(topSection, "Role-Isolated Portals", "Segregated environments for students and admins.");

        panel.add(topSection, BorderLayout.NORTH);

        // Footer in brand panel
        JLabel footerLabel = new JLabel("Institutional Release • Enterprise Edition");
        footerLabel.setFont(Typography.CAPTION);
        footerLabel.setForeground(Color.decode("#64748B"));
        panel.add(footerLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void addFeatureItem(JPanel parent, String heading, String desc) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBorder(new EmptyBorder(0, 0, Spacing.LG, 0));

        JLabel hLabel = new JLabel("• " + heading);
        hLabel.setFont(Typography.LABEL);
        hLabel.setForeground(Color.decode("#F1F5F9"));

        JLabel dLabel = new JLabel("   " + desc);
        dLabel.setFont(Typography.CAPTION);
        dLabel.setForeground(Color.decode("#94A3B8"));

        item.add(hLabel);
        item.add(Box.createRigidArea(new Dimension(0, Spacing.XS)));
        item.add(dLabel);

        parent.add(item);
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXXL, Spacing.XXL, Spacing.XXXL));

        // Form content wrapper
        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));

        // 1. Role Toggle Tabs (Student / Admin)
        JPanel tabContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabContainer.setOpaque(false);

        JPanel tabPill = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Colors.SURFACE_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
                g2.setColor(Colors.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
                g2.dispose();
            }
        };
        tabPill.setOpaque(false);
        tabPill.setBorder(new EmptyBorder(2, 2, 2, 2));

        studentTabBtn = createTabButton("Student Portal", true);
        adminTabBtn = createTabButton("Administrator", false);

        studentTabBtn.addActionListener(e -> setPortalMode(PortalMode.STUDENT));
        adminTabBtn.addActionListener(e -> setPortalMode(PortalMode.ADMIN));

        tabPill.add(studentTabBtn);
        tabPill.add(adminTabBtn);
        tabContainer.add(tabPill);

        formBox.add(tabContainer);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.XL)));

        // 2. Headings
        JLabel formTitle = new JLabel("Sign In");
        formTitle.setFont(Typography.H1);
        formTitle.setForeground(Colors.TEXT_PRIMARY);
        formBox.add(formTitle);

        formSubtitle = new JLabel("Sign in to access your scheduled examinations.");
        formSubtitle.setFont(Typography.BODY_SMALL);
        formSubtitle.setForeground(Colors.TEXT_SECONDARY);
        formBox.add(formSubtitle);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.LG)));

        // 3. Error Banner (Hidden by default)
        errorBanner = new JPanel(new BorderLayout());
        errorBanner.setOpaque(true);
        errorBanner.setBackground(Colors.STATUS_ERROR_BG);
        errorBanner.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Colors.STATUS_ERROR, 1),
            new EmptyBorder(Spacing.SM, Spacing.MD, Spacing.SM, Spacing.MD)
        ));
        errorLabel = new JLabel();
        errorLabel.setFont(Typography.BODY_SMALL);
        errorLabel.setForeground(Colors.STATUS_ERROR_TEXT);
        errorBanner.add(errorLabel, BorderLayout.CENTER);
        errorBanner.setVisible(false);
        formBox.add(errorBanner);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.MD)));

        // 4. Username Field
        usernameLabel = new JLabel("Student Username");
        usernameLabel.setFont(Typography.LABEL);
        usernameLabel.setForeground(Colors.TEXT_PRIMARY);
        formBox.add(usernameLabel);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.XS)));

        usernameField = new AppTextField("e.g. student1");
        usernameField.addActionListener(e -> handleLogin());
        formBox.add(usernameField);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.MD)));

        // 5. Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(Typography.LABEL);
        passwordLabel.setForeground(Colors.TEXT_PRIMARY);
        formBox.add(passwordLabel);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.XS)));

        passwordField = new AppPasswordField("Enter your password");
        passwordField.addActionListener(e -> handleLogin());
        formBox.add(passwordField);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.SM)));

        // 6. Forgot Password row
        JPanel helperRow = new JPanel(new BorderLayout());
        helperRow.setOpaque(false);

        JButton forgotBtn = new JButton("Forgot password?");
        forgotBtn.setFont(Typography.CAPTION);
        forgotBtn.setForeground(Colors.PRIMARY);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> showForgotPasswordDialog());
        helperRow.add(forgotBtn, BorderLayout.EAST);

        formBox.add(helperRow);
        formBox.add(Box.createRigidArea(new Dimension(0, Spacing.LG)));

        // 7. Sign In Button
        signInButton = new AppButton("Sign In", AppButton.ButtonStyle.PRIMARY);
        signInButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, Dimensions.BUTTON_HEIGHT));
        signInButton.addActionListener(e -> handleLogin());
        formBox.add(signInButton);

        formContainer.add(formBox, BorderLayout.CENTER);

        // Security reassurance footer
        JLabel secFoot = new JLabel("Protected with PBKDF2 cryptography & secure session verification.");
        secFoot.setFont(Typography.CAPTION);
        secFoot.setForeground(Colors.TEXT_MUTED);
        secFoot.setHorizontalAlignment(JLabel.CENTER);
        formContainer.add(secFoot, BorderLayout.SOUTH);

        return formContainer;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(Typography.LABEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 32));
        updateTabStyle(btn, active);
        return btn;
    }

    private void updateTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Colors.SURFACE);
            btn.setForeground(Colors.PRIMARY);
            btn.setOpaque(true);
        } else {
            btn.setBackground(Colors.SURFACE_ELEVATED);
            btn.setForeground(Colors.TEXT_SECONDARY);
            btn.setOpaque(false);
        }
        btn.repaint();
    }

    public void setPortalMode(PortalMode mode) {
        this.currentMode = mode;
        clearError();

        if (mode == PortalMode.STUDENT) {
            updateTabStyle(studentTabBtn, true);
            updateTabStyle(adminTabBtn, false);
            formSubtitle.setText("Sign in to access your scheduled examinations.");
            usernameLabel.setText("Student Username");
            usernameField.setPlaceholder("e.g. student1");
        } else {
            updateTabStyle(studentTabBtn, false);
            updateTabStyle(adminTabBtn, true);
            formSubtitle.setText("Sign in to manage questions, exams, and evaluations.");
            usernameLabel.setText("Administrator Username");
            usernameField.setPlaceholder("e.g. admin");
        }
        usernameField.requestFocusInWindow();
    }

    public PortalMode getPortalMode() {
        return currentMode;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorBanner.setVisible(true);
        revalidate();
        repaint();
    }

    private void clearError() {
        errorBanner.setVisible(false);
        revalidate();
        repaint();
    }

    private void showForgotPasswordDialog() {
        JOptionPane.showMessageDialog(
            this,
            "For academic security and account protection, password resets must be issued by a System Administrator or Examination Coordinator.\n\n" +
            "Please contact your institution's examination administration office to request a temporary reset password.",
            "Password Recovery Information",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleLogin() {
        clearError();

        String username = usernameField.getText().trim();
        char[] passChars = passwordField.getPassword();
        String password = new String(passChars).trim();

        // 1. Validation
        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocusInWindow();
            return;
        }

        // 2. Disable UI & show loading state
        setFormEnabled(false);
        signInButton.setText("Signing in...");

        // 3. Asynchronous authentication via SwingWorker (avoids blocking EDT)
        final PortalMode mode = currentMode;
        SwingWorker<UserSession, Void> worker = new SwingWorker<>() {
            @Override
            protected UserSession doInBackground() throws Exception {
                if (mode == PortalMode.STUDENT) {
                    return authService.loginStudent(username, password);
                } else {
                    return authService.loginAdmin(username, password);
                }
            }

            @Override
            protected void done() {
                try {
                    UserSession session = get();
                    // Clear password field memory
                    passwordField.setText("");

                    if (loginListener != null) {
                        loginListener.onLoginSuccess(session);
                    }
                } catch (Exception ex) {
                    // Safe non-sensitive error display
                    String causeMsg = "Invalid username or password.";
                    if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                        String msg = ex.getCause().getMessage().toLowerCase();
                        if (msg.contains("inactive")) {
                            causeMsg = "Account is deactivated. Please contact your administrator.";
                        }
                    }
                    showError(causeMsg);
                    setFormEnabled(true);
                    signInButton.setText("Sign In");
                    passwordField.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void setFormEnabled(boolean enabled) {
        studentTabBtn.setEnabled(enabled);
        adminTabBtn.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        signInButton.setEnabled(enabled);
    }

    public void focusUsername() {
        usernameField.requestFocusInWindow();
    }
}
