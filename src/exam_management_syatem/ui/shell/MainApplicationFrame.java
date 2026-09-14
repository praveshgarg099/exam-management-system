package exam_management_syatem.ui.shell;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Typography;
import exam_management_syatem.ui.theme.AppTheme;
import exam_management_syatem.ui.views.admin.AdminDashboardView;
import exam_management_syatem.ui.views.admin.AdminStudentsView;
import exam_management_syatem.ui.views.admin.AdminSubjectsView;
import exam_management_syatem.ui.views.admin.AdminQuestionsView;
import exam_management_syatem.ui.views.admin.AdminExamsView;
import exam_management_syatem.ui.views.admin.AdminResultsView;
import exam_management_syatem.ui.views.student.StudentDashboardView;
import exam_management_syatem.ui.views.student.StudentMyExamsView;
import exam_management_syatem.ui.views.student.StudentMyResultsView;
import exam_management_syatem.ui.views.student.StudentProfileView;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;

public class MainApplicationFrame extends JFrame implements NavigationController {

    private final UserSession session;
    private final ViewRegistry registry;
    
    private AppSidebar sidebar;
    private AppTopBar topBar;
    
    private JPanel contentArea;
    private CardLayout cardLayout;
    private String currentViewId;

    public MainApplicationFrame(UserSession session) {
        this.session = session;
        this.session.requireAuthenticated(); // Fail fast if invalid
        
        this.registry = new ViewRegistry();
        
        AppTheme.setup();
        
        setTitle("Exam Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new java.awt.Dimension(900, 700));
        setLocationRelativeTo(null);
        
        initComponents();
        
        // Auto-navigate to first available route
        SwingUtilities.invokeLater(() -> {
            if (!sidebar.getNavItems().isEmpty()) {
                navigateTo(sidebar.getNavItems().get(0).getId());
            }
        });
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Colors.BACKGROUND);
        
        sidebar = new AppSidebar(this, session);
        add(sidebar, BorderLayout.WEST);
        
        topBar = new AppTopBar(this, session);
        add(topBar, BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Colors.BACKGROUND);
        add(contentArea, BorderLayout.CENTER);
        
        // Register views
        for (NavigationItem item : sidebar.getNavItems()) {
            JPanel viewPanel;
            if (item.getId().equals("ADMIN_DASHBOARD")) {
                viewPanel = new AdminDashboardView(session, this);
            } else if (item.getId().equals("ADMIN_STUDENTS")) {
                viewPanel = new AdminStudentsView(session, this);
            } else if (item.getId().equals("ADMIN_SUBJECTS")) {
                viewPanel = new AdminSubjectsView(session, this);
            } else if (item.getId().equals("ADMIN_QUESTIONS")) {
                viewPanel = new AdminQuestionsView(session, this);
            } else if (item.getId().equals("ADMIN_EXAMS")) {
                viewPanel = new AdminExamsView(session, this);
            } else if (item.getId().equals("ADMIN_RESULTS")) {
                viewPanel = new AdminResultsView(session, this);
            } else if (item.getId().equals("STUDENT_DASHBOARD")) {
                viewPanel = new StudentDashboardView(session, this);
            } else if (item.getId().equals("STUDENT_MY_EXAMS")) {
                viewPanel = new StudentMyExamsView(session);
            } else if (item.getId().equals("STUDENT_MY_RESULTS")) {
                viewPanel = new StudentMyResultsView(session);
            } else if (item.getId().equals("STUDENT_PROFILE")) {
                viewPanel = new StudentProfileView(session);
            } else {
                viewPanel = createNotYetMigratedView(item.getLabel());
            }
            
            registry.registerView(item, viewPanel);
            contentArea.add(viewPanel, item.getId());
        }
    }
    
    @Override
    public void navigateTo(String viewId) {
        if (!canNavigateTo(viewId)) {
            return;
        }
        
        currentViewId = viewId;
        cardLayout.show(contentArea, viewId);
        
        NavigationItem item = registry.getNavItem(viewId);
        if (item != null) {
            topBar.setTitle(item.getLabel());
        }
        
        sidebar.updateActiveState(viewId);

        // Lazy load data for the navigated view
        JPanel activeView = registry.getView(viewId);
        if (activeView instanceof StudentDashboardView) {
            ((StudentDashboardView) activeView).refreshData();
        } else if (activeView instanceof StudentMyExamsView) {
            ((StudentMyExamsView) activeView).refreshData();
        } else if (activeView instanceof StudentMyResultsView) {
            ((StudentMyResultsView) activeView).refreshData();
        } else if (activeView instanceof StudentProfileView) {
            ((StudentProfileView) activeView).loadProfileData();
        } else if (activeView instanceof AdminDashboardView) {
            ((AdminDashboardView) activeView).refreshData();
        } else if (activeView instanceof AdminStudentsView) {
            ((AdminStudentsView) activeView).refreshData();
        } else if (activeView instanceof AdminSubjectsView) {
            ((AdminSubjectsView) activeView).refreshData();
        } else if (activeView instanceof AdminQuestionsView) {
            ((AdminQuestionsView) activeView).refreshData();
        } else if (activeView instanceof AdminExamsView) {
            ((AdminExamsView) activeView).refreshData();
        } else if (activeView instanceof AdminResultsView) {
            ((AdminResultsView) activeView).refreshData();
        }
    }

    @Override
    public String getCurrentViewId() {
        return currentViewId;
    }

    @Override
    public boolean canNavigateTo(String viewId) {
        return registry.hasView(viewId);
    }

    @Override
    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to logout?", 
            "Logout", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            exam_management_syatem.ui.auth.LoginFrame.launch();
        }
    }
    
    private JPanel createNotYetMigratedView(String label) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Colors.BACKGROUND);
        JLabel l = new JLabel(label + " is NOT YET MIGRATED");
        l.setFont(Typography.H2);
        l.setForeground(Colors.TEXT_MUTED);
        p.add(l);
        return p;
    }
}
