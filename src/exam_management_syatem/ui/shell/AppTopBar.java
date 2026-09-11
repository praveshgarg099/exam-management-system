package exam_management_syatem.ui.shell;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.DesignSystem;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class AppTopBar extends JPanel {

    private JLabel titleLabel;
    private final NavigationController controller;
    private final UserSession session;

    public AppTopBar(NavigationController controller, UserSession session) {
        this.controller = controller;
        this.session = session;
        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setBackground(Colors.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER),
                DesignSystem.createPadding(Spacing.MD, Spacing.XL, Spacing.MD, Spacing.XL)
        ));

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(Typography.H2);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.LG, 0));
        rightPanel.setOpaque(false);

        String username = session.getUsername();
        String role = session.getRole();
        
        JLabel userLabel = new JLabel(username + " (" + role + ")");
        userLabel.setFont(Typography.BODY);
        userLabel.setForeground(Colors.TEXT_SECONDARY);
        rightPanel.add(userLabel);

        AppButton logoutBtn = new AppButton("Logout", AppButton.ButtonStyle.SECONDARY);
        logoutBtn.addActionListener(e -> controller.handleLogout());
        rightPanel.add(logoutBtn);

        add(rightPanel, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
