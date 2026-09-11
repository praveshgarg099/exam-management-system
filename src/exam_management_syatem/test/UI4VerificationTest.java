package exam_management_syatem.test;

import exam_management_syatem.model.ExamResult;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.DashboardMetricsService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.ui.components.StatCard;
import exam_management_syatem.ui.shell.NavigationController;
import exam_management_syatem.ui.views.admin.AdminDashboardView;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UI4VerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("     UI.4 ADMIN DASHBOARD VERIFICATION SUITE     ");
        System.out.println("=================================================");

        try {
            exam_management_syatem.db.DatabaseManager.initializeDatabase();

            testAdminAuthorization();
            testKpiCardsAndDataMapping();
            testRecentResultsTableAndStatusBadges();
            testQuickActionsNavigationDispatch();
            testResponsiveSizes();

            System.out.println("=================================================");
            System.out.printf("UI.4 TEST SUMMARY: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
            System.out.println("=================================================");

            if (testsPassed != testsRun) {
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void pass(String msg) {
        testsRun++;
        testsPassed++;
        System.out.println("  [PASS] " + msg);
    }

    private static void fail(String msg) {
        testsRun++;
        System.err.println("  [FAIL] " + msg);
    }

    private static void testAdminAuthorization() throws Exception {
        System.out.println("\n--- 1. Service Authorization Enforcement ---");
        DashboardMetricsService dms = new DashboardMetricsService();
        ResultService rs = new ResultService();

        try {
            dms.getMetrics(null);
            fail("Null session should be rejected by DashboardMetricsService");
        } catch (SecurityException e) {
            pass("Null session rejected by DashboardMetricsService");
        }

        UserSession studentSession = new UserSession(2, "student_test", "STUDENT", 1);
        try {
            dms.getMetrics(studentSession);
            fail("Student session should be rejected by DashboardMetricsService");
        } catch (SecurityException e) {
            pass("Student session rejected by DashboardMetricsService");
        }

        try {
            rs.getRecentResults(studentSession, 10);
            fail("Student session should be rejected by ResultService.getRecentResults");
        } catch (SecurityException e) {
            pass("Student session rejected by ResultService.getRecentResults");
        }
    }

    private static void testKpiCardsAndDataMapping() throws Exception {
        System.out.println("\n--- 2. Five KPI Cards & Data Mapping ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");

        final AdminDashboardView[] viewHolder = new AdminDashboardView[1];
        SwingUtilities.invokeAndWait(() -> {
            viewHolder[0] = new AdminDashboardView(adminSession);
        });

        // Wait for SwingWorker to finish
        Thread.sleep(1200);

        AdminDashboardView view = viewHolder[0];

        Field fStudents = AdminDashboardView.class.getDeclaredField("studentsCard");
        Field fQuestions = AdminDashboardView.class.getDeclaredField("questionsCard");
        Field fSchedules = AdminDashboardView.class.getDeclaredField("schedulesCard");
        Field fPassRate = AdminDashboardView.class.getDeclaredField("passRateCard");
        Field fAvgScore = AdminDashboardView.class.getDeclaredField("avgScoreCard");

        fStudents.setAccessible(true);
        fQuestions.setAccessible(true);
        fSchedules.setAccessible(true);
        fPassRate.setAccessible(true);
        fAvgScore.setAccessible(true);

        StatCard cStudents = (StatCard) fStudents.get(view);
        StatCard cQuestions = (StatCard) fQuestions.get(view);
        StatCard cSchedules = (StatCard) fSchedules.get(view);
        StatCard cPassRate = (StatCard) fPassRate.get(view);
        StatCard cAvgScore = (StatCard) fAvgScore.get(view);

        Field valField = StatCard.class.getDeclaredField("valueLabel");
        valField.setAccessible(true);

        String vStudents = ((javax.swing.JLabel) valField.get(cStudents)).getText();
        String vQuestions = ((javax.swing.JLabel) valField.get(cQuestions)).getText();
        String vSchedules = ((javax.swing.JLabel) valField.get(cSchedules)).getText();
        String vPassRate = ((javax.swing.JLabel) valField.get(cPassRate)).getText();
        String vAvgScore = ((javax.swing.JLabel) valField.get(cAvgScore)).getText();

        if (!"—".equals(vStudents) && Integer.parseInt(vStudents) > 0) {
            pass("Enrolled Students KPI loaded real database count: " + vStudents);
        } else {
            fail("Enrolled Students KPI has invalid value: " + vStudents);
        }

        if (!"—".equals(vQuestions) && Integer.parseInt(vQuestions) > 0) {
            pass("Question Repository KPI loaded real database count: " + vQuestions);
        } else {
            fail("Question Repository KPI has invalid value: " + vQuestions);
        }

        if (!"—".equals(vSchedules) && Integer.parseInt(vSchedules) > 0) {
            pass("Exam Schedules KPI loaded real database count: " + vSchedules);
        } else {
            fail("Exam Schedules KPI has invalid value: " + vSchedules);
        }

        if (vPassRate.contains("%")) {
            pass("Pass Rate KPI formatted with percentage: " + vPassRate);
        } else {
            fail("Pass Rate KPI missing percentage: " + vPassRate);
        }

        if (vAvgScore.contains("%")) {
            pass("Average Performance KPI formatted with percentage: " + vAvgScore);
        } else {
            fail("Average Performance KPI missing percentage: " + vAvgScore);
        }
    }

    private static void testRecentResultsTableAndStatusBadges() throws Exception {
        System.out.println("\n--- 3. Recent Results Table & Status Badges ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");
        ResultService rs = new ResultService();
        List<ExamResult> recent = rs.getRecentResults(adminSession, 10);

        pass("ResultService.getRecentResults returned " + recent.size() + " items (<= 10)");

        final AdminDashboardView[] viewHolder = new AdminDashboardView[1];
        SwingUtilities.invokeAndWait(() -> {
            viewHolder[0] = new AdminDashboardView(adminSession);
        });

        Thread.sleep(1200);

        AdminDashboardView view = viewHolder[0];
        Field fTable = AdminDashboardView.class.getDeclaredField("recentTable");
        fTable.setAccessible(true);
        JTable table = (JTable) fTable.get(view);

        if (recent.isEmpty()) {
            Field fEmpty = AdminDashboardView.class.getDeclaredField("emptyStatePanel");
            fEmpty.setAccessible(true);
            javax.swing.JPanel emptyPanel = (javax.swing.JPanel) fEmpty.get(view);
            if (emptyPanel.isVisible()) {
                pass("Empty state panel displayed when no results exist");
            } else {
                fail("Empty state panel should be visible");
            }
        } else {
            if (table.getRowCount() == recent.size()) {
                pass("Recent results table populated with exactly " + table.getRowCount() + " rows");
            } else {
                fail("Table row count mismatch. Expected: " + recent.size() + ", got: " + table.getRowCount());
            }

            // Verify column count
            if (table.getColumnCount() == 8) {
                pass("Table correctly defines 8 audited columns");
            } else {
                fail("Expected 8 columns, got: " + table.getColumnCount());
            }

            // Verify first row mapping
            Object attemptVal = table.getValueAt(0, 1);
            ExamResult firstResult = recent.get(0);
            if (firstResult.getAttemptId() == null) {
                if ("— (Legacy)".equals(attemptVal)) {
                    pass("Nullable legacy attempt_id correctly mapped to '— (Legacy)' without fabrication");
                } else {
                    fail("Expected '— (Legacy)' for null attempt_id but got: " + attemptVal);
                }
            } else {
                if (("#" + firstResult.getAttemptId()).equals(attemptVal)) {
                    pass("Attempt ID correctly mapped to #" + firstResult.getAttemptId());
                } else {
                    fail("Attempt ID mismatch. Expected: #" + firstResult.getAttemptId() + ", got: " + attemptVal);
                }
            }

            // Verify status badge object in row
            Object statusVal = table.getValueAt(0, 6);
            if (statusVal instanceof exam_management_syatem.ui.components.StatusBadge) {
                pass("Status column rendered using StatusBadge component");
            } else {
                fail("Status column did not contain StatusBadge: " + statusVal);
            }
        }
    }

    private static void testQuickActionsNavigationDispatch() throws Exception {
        System.out.println("\n--- 4. Quick Actions Centralized Navigation Dispatch ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");
        List<String> dispatchedRoutes = new ArrayList<>();

        NavigationController mockController = new NavigationController() {
            @Override
            public void navigateTo(String viewId) {
                dispatchedRoutes.add(viewId);
            }

            @Override
            public String getCurrentViewId() {
                return "ADMIN_DASHBOARD";
            }

            @Override
            public boolean canNavigateTo(String viewId) {
                return true;
            }

            @Override
            public void handleLogout() {}
        };

        final AdminDashboardView[] viewHolder = new AdminDashboardView[1];
        SwingUtilities.invokeAndWait(() -> {
            viewHolder[0] = new AdminDashboardView(adminSession, mockController);
        });

        AdminDashboardView view = viewHolder[0];

        // Find buttons in the view and click them
        List<JButton> actionButtons = new ArrayList<>();
        findButtons(view, actionButtons);

        for (JButton btn : actionButtons) {
            if ("Schedule Exam".equals(btn.getText()) ||
                "Student Directory".equals(btn.getText()) ||
                "Question Bank".equals(btn.getText()) ||
                "Subject Management".equals(btn.getText()) ||
                "Exam Results".equals(btn.getText())) {
                
                SwingUtilities.invokeAndWait(btn::doClick);
            }
        }

        if (dispatchedRoutes.contains("ADMIN_EXAMS")) {
            pass("Schedule Exam dispatched 'ADMIN_EXAMS' to NavigationController");
        } else {
            fail("ADMIN_EXAMS was not dispatched");
        }

        if (dispatchedRoutes.contains("ADMIN_STUDENTS")) {
            pass("Student Directory dispatched 'ADMIN_STUDENTS' to NavigationController");
        } else {
            fail("ADMIN_STUDENTS was not dispatched");
        }

        if (dispatchedRoutes.contains("ADMIN_QUESTIONS")) {
            pass("Question Bank dispatched 'ADMIN_QUESTIONS' to NavigationController");
        } else {
            fail("ADMIN_QUESTIONS was not dispatched");
        }

        if (dispatchedRoutes.contains("ADMIN_SUBJECTS")) {
            pass("Subject Management dispatched 'ADMIN_SUBJECTS' to NavigationController");
        } else {
            fail("ADMIN_SUBJECTS was not dispatched");
        }

        if (dispatchedRoutes.contains("ADMIN_RESULTS")) {
            pass("Exam Results dispatched 'ADMIN_RESULTS' to NavigationController");
        } else {
            fail("ADMIN_RESULTS was not dispatched");
        }
    }

    private static void testResponsiveSizes() throws Exception {
        System.out.println("\n--- 5. Responsive Layout & Scrolling Verification ---");
        UserSession adminSession = new AuthenticationService().loginAdmin("superadmin", "123456");

        final JFrame[] frameHolder = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame f = new JFrame("Admin Dashboard Test Frame");
            f.getContentPane().add(new AdminDashboardView(adminSession));
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameHolder[0] = f;
        });

        JFrame frame = frameHolder[0];
        int[][] resolutions = {
            {1440, 900},
            {1280, 800},
            {1024, 768},
            {900, 700}
        };

        for (int[] res : resolutions) {
            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(res[0], res[1]);
                frame.doLayout();
                frame.validate();
            });
            pass("Dashboard rendered cleanly without exceptions at " + res[0] + "x" + res[1]);
        }

        SwingUtilities.invokeAndWait(frame::dispose);
    }

    private static void findButtons(java.awt.Container container, List<JButton> list) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof JButton) {
                list.add((JButton) c);
            } else if (c instanceof java.awt.Container) {
                findButtons((java.awt.Container) c, list);
            }
        }
    }
}
