package exam_management_syatem.ui.shell;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.DesignSystem;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Icons;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class AppSidebar extends JPanel {

    private final NavigationController controller;
    private final UserSession session;
    private final List<NavigationItem> navItems = new ArrayList<>();
    
    private final JPanel navPanel;
    private final List<AppButton> navButtons = new ArrayList<>();

    public AppSidebar(NavigationController controller, UserSession session) {
        this.controller = controller;
        this.session = session;
        
        setLayout(new BorderLayout());
        setBackground(Colors.SURFACE);
        setPreferredSize(new Dimension(Dimensions.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER));

        // Brand Header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.LG, Spacing.LG));
        brandPanel.setOpaque(false);
        JLabel brandLabel = new JLabel("Exam Platform");
        brandLabel.setFont(Typography.H2);
        brandLabel.setForeground(Colors.PRIMARY);
        brandPanel.add(brandLabel);
        add(brandPanel, BorderLayout.NORTH);

        // Navigation Links
        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(DesignSystem.createPadding(Spacing.MD, Spacing.SM, Spacing.MD, Spacing.SM));
        add(navPanel, BorderLayout.CENTER);
        
        setupNavigationForRole();
    }
    
    private void setupNavigationForRole() {
        if (session.isAdmin()) {
            navItems.add(new NavigationItem("ADMIN_DASHBOARD", "Dashboard", Icons.getDashboardIcon()));
            navItems.add(new NavigationItem("ADMIN_STUDENTS", "Students", Icons.getStudentIcon()));
            navItems.add(new NavigationItem("ADMIN_SUBJECTS", "Subjects", null));
            navItems.add(new NavigationItem("ADMIN_QUESTIONS", "Questions", null));
            navItems.add(new NavigationItem("ADMIN_EXAMS", "Exams", null));
            navItems.add(new NavigationItem("ADMIN_RESULTS", "Results", null));
        } else if (session.isStudent()) {
            navItems.add(new NavigationItem("STUDENT_DASHBOARD", "Dashboard", Icons.getDashboardIcon()));
            navItems.add(new NavigationItem("STUDENT_MY_EXAMS", "My Exams", null));
            navItems.add(new NavigationItem("STUDENT_MY_RESULTS", "My Results", null));
            navItems.add(new NavigationItem("STUDENT_PROFILE", "Profile & Security", null));
        }
        
        // Build UI buttons
        for (NavigationItem item : navItems) {
            AppButton btn = new AppButton(item.getLabel(), AppButton.ButtonStyle.GHOST);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Dimensions.BUTTON_HEIGHT));
            if (item.getIcon() != null) {
                btn.setIcon(item.getIcon());
                btn.setIconTextGap(Spacing.MD);
            }
            
            btn.addActionListener(e -> {
                controller.navigateTo(item.getId());
            });
            
            navButtons.add(btn);
            navPanel.add(btn);
            navPanel.add(Box.createRigidArea(new Dimension(0, Spacing.XS)));
        }
    }
    
    public void updateActiveState(String viewId) {
        // Ideally AppButton would have a SET_ACTIVE state, for now we will disable the active one
        // or just rely on the controller logic.
        for (int i = 0; i < navItems.size(); i++) {
            NavigationItem item = navItems.get(i);
            AppButton btn = navButtons.get(i);
            
            if (item.getId().equals(viewId)) {
                btn.setForeground(Colors.PRIMARY);
            } else {
                btn.setForeground(Colors.TEXT_PRIMARY);
            }
        }
    }
    
    public List<NavigationItem> getNavItems() {
        return navItems;
    }
}
