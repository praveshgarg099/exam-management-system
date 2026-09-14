package exam_management_syatem.test;

import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.ui.auth.LoginFrame;
import exam_management_syatem.ui.auth.LoginPanel;
import exam_management_syatem.ui.components.AppPasswordField;
import exam_management_syatem.ui.components.AppTextField;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UI3VerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("     UI.3 LOGIN EXPERIENCE VERIFICATION SUITE    ");
        System.out.println("=================================================");

        try {
            exam_management_syatem.db.DatabaseManager.initializeDatabase();

            testPasswordFieldShowHide();
            testValidationEmptyFields();
            testAdminLoginSuccess();
            testStudentLoginSuccess();
            testInvalidPassword();
            testCrossRoleLoginBlocked();
            testResponsiveSizes();

            System.out.println("=================================================");
            System.out.printf("UI.3 TEST SUMMARY: %d PASSED, %d FAILED%n", testsPassed, (testsRun - testsPassed));
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

    private static void testPasswordFieldShowHide() throws Exception {
        System.out.println("\n--- 1. Password Field & Show/Hide Toggle ---");
        final AppPasswordField[] pfHolder = new AppPasswordField[1];
        SwingUtilities.invokeAndWait(() -> {
            pfHolder[0] = new AppPasswordField("Enter password");
            pfHolder[0].setText("SecretPass123");
        });

        AppPasswordField pf = pfHolder[0];
        Field pfField = AppPasswordField.class.getDeclaredField("passwordField");
        pfField.setAccessible(true);
        javax.swing.JPasswordField innerPf = (javax.swing.JPasswordField) pfField.get(pf);

        if (innerPf.getEchoChar() != (char) 0) {
            pass("Password hidden by default with echoChar = '" + innerPf.getEchoChar() + "'");
        } else {
            fail("Password should be hidden by default");
        }

        // Toggle to visible
        Method toggleMethod = AppPasswordField.class.getDeclaredMethod("togglePasswordVisibility");
        toggleMethod.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                toggleMethod.invoke(pf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if (innerPf.getEchoChar() == (char) 0) {
            pass("Password visible after toggle (echoChar == 0)");
        } else {
            fail("Password should be visible after toggle");
        }

        // Toggle back to hidden
        SwingUtilities.invokeAndWait(() -> {
            try {
                toggleMethod.invoke(pf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if (innerPf.getEchoChar() != (char) 0) {
            pass("Password hidden again after second toggle");
        } else {
            fail("Password should be hidden after second toggle");
        }
    }

    private static void testValidationEmptyFields() throws Exception {
        System.out.println("\n--- 2. Form Validation (Empty Fields) ---");
        AuthenticationService auth = new AuthenticationService();
        final LoginPanel[] panelHolder = new LoginPanel[1];

        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new LoginPanel(auth, session -> {});
        });

        LoginPanel panel = panelHolder[0];
        Field errLblField = LoginPanel.class.getDeclaredField("errorLabel");
        errLblField.setAccessible(true);
        javax.swing.JLabel errorLabel = (javax.swing.JLabel) errLblField.get(panel);

        Method handleLoginMethod = LoginPanel.class.getDeclaredMethod("handleLogin");
        handleLoginMethod.setAccessible(true);

        // 1. Both empty -> Please enter your username
        SwingUtilities.invokeAndWait(() -> {
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if ("Please enter your username.".equals(errorLabel.getText())) {
            pass("Empty username correctly caught with user-friendly validation error");
        } else {
            fail("Expected 'Please enter your username.' but got: " + errorLabel.getText());
        }

        // 2. Username set, password empty -> Please enter your password
        Field uField = LoginPanel.class.getDeclaredField("usernameField");
        uField.setAccessible(true);
        AppTextField usernameField = (AppTextField) uField.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText("someuser");
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if ("Please enter your password.".equals(errorLabel.getText())) {
            pass("Empty password correctly caught with user-friendly validation error");
        } else {
            fail("Expected 'Please enter your password.' but got: " + errorLabel.getText());
        }
    }

    private static void testAdminLoginSuccess() throws Exception {
        System.out.println("\n--- 3. Admin Authentication Flow ---");
        AuthenticationService auth = new AuthenticationService();
        AtomicReference<UserSession> sessionRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        final LoginPanel[] panelHolder = new LoginPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new LoginPanel(auth, session -> {
                sessionRef.set(session);
                latch.countDown();
            });
            panelHolder[0].setPortalMode(LoginPanel.PortalMode.ADMIN);
        });

        LoginPanel panel = panelHolder[0];
        Field uField = LoginPanel.class.getDeclaredField("usernameField");
        Field pField = LoginPanel.class.getDeclaredField("passwordField");
        uField.setAccessible(true);
        pField.setAccessible(true);

        AppTextField usernameField = (AppTextField) uField.get(panel);
        AppPasswordField passwordField = (AppPasswordField) pField.get(panel);

        Method handleLoginMethod = LoginPanel.class.getDeclaredMethod("handleLogin");
        handleLoginMethod.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText(TestCredentials.getAdminUsername());
            passwordField.setText(TestCredentials.getAdminPassword());
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        if (completed && sessionRef.get() != null) {
            UserSession s = sessionRef.get();
            if (s.isAdmin() && TestCredentials.getAdminUsername().equalsIgnoreCase(s.getUsername())) {
                pass("Admin login authenticated successfully with UserSession role = " + s.getRole());
            } else {
                fail("UserSession does not have admin role");
            }
        } else {
            fail("Admin authentication timed out or failed");
        }
    }

    private static void testStudentLoginSuccess() throws Exception {
        System.out.println("\n--- 4. Student Authentication Flow ---");
        exam_management_syatem.service.StudentService studentService = new exam_management_syatem.service.StudentService();
        long suffix = System.currentTimeMillis() % 100000000L;
        String aadhar = String.format("%012d", suffix);
        UserSession adminSession = new AuthenticationService().loginAdmin(TestCredentials.getAdminUsername(), TestCredentials.getAdminPassword());
        exam_management_syatem.service.StudentService.RegistrationResult reg = studentService.registerStudent(
            adminSession, "UIStudent", "9876543210", "student_" + suffix + "@example.com", aadhar, "01012000"
        );

        AuthenticationService auth = new AuthenticationService();
        AtomicReference<UserSession> sessionRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        final LoginPanel[] panelHolder = new LoginPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new LoginPanel(auth, session -> {
                sessionRef.set(session);
                latch.countDown();
            });
            panelHolder[0].setPortalMode(LoginPanel.PortalMode.STUDENT);
        });

        LoginPanel panel = panelHolder[0];
        Field uField = LoginPanel.class.getDeclaredField("usernameField");
        Field pField = LoginPanel.class.getDeclaredField("passwordField");
        uField.setAccessible(true);
        pField.setAccessible(true);

        AppTextField usernameField = (AppTextField) uField.get(panel);
        AppPasswordField passwordField = (AppPasswordField) pField.get(panel);

        Method handleLoginMethod = LoginPanel.class.getDeclaredMethod("handleLogin");
        handleLoginMethod.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText(reg.username);
            passwordField.setText(reg.password);
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        if (completed && sessionRef.get() != null) {
            UserSession s = sessionRef.get();
            if (s.isStudent() && reg.username.equalsIgnoreCase(s.getUsername())) {
                pass("Student login authenticated successfully with UserSession role = " + s.getRole() + ", studentId = " + s.getStudentId());
            } else {
                fail("UserSession does not have expected student properties");
            }
        } else {
            fail("Student authentication timed out or failed");
        }
    }

    private static void testInvalidPassword() throws Exception {
        System.out.println("\n--- 5. Invalid Credentials Handling ---");
        AuthenticationService auth = new AuthenticationService();
        final LoginPanel[] panelHolder = new LoginPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new LoginPanel(auth, session -> {});
            panelHolder[0].setPortalMode(LoginPanel.PortalMode.ADMIN);
        });

        LoginPanel panel = panelHolder[0];
        Field uField = LoginPanel.class.getDeclaredField("usernameField");
        Field pField = LoginPanel.class.getDeclaredField("passwordField");
        Field errLblField = LoginPanel.class.getDeclaredField("errorLabel");
        Field btnField = LoginPanel.class.getDeclaredField("signInButton");

        uField.setAccessible(true);
        pField.setAccessible(true);
        errLblField.setAccessible(true);
        btnField.setAccessible(true);

        AppTextField usernameField = (AppTextField) uField.get(panel);
        AppPasswordField passwordField = (AppPasswordField) pField.get(panel);
        javax.swing.JLabel errorLabel = (javax.swing.JLabel) errLblField.get(panel);
        exam_management_syatem.ui.components.AppButton signInBtn = (exam_management_syatem.ui.components.AppButton) btnField.get(panel);

        Method handleLoginMethod = LoginPanel.class.getDeclaredMethod("handleLogin");
        handleLoginMethod.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText("admin");
            passwordField.setText("CompletelyWrongPass999");
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for SwingWorker
        Thread.sleep(800);

        SwingUtilities.invokeAndWait(() -> {
            if ("Invalid username or password.".equals(errorLabel.getText())) {
                pass("Invalid password handled with safe non-sensitive error banner");
            } else {
                fail("Expected 'Invalid username or password.' but got: " + errorLabel.getText());
            }

            if (signInBtn.isEnabled() && "Sign In".equals(signInBtn.getText())) {
                pass("Sign In button re-enabled and restored after failed login");
            } else {
                fail("Sign In button was not restored properly");
            }
        });
    }

    private static void testCrossRoleLoginBlocked() throws Exception {
        System.out.println("\n--- 6. Cross-Role Segregation (Admin in Student portal & vice versa) ---");
        AuthenticationService auth = new AuthenticationService();
        final LoginPanel[] panelHolder = new LoginPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            panelHolder[0] = new LoginPanel(auth, session -> {});
            // In student mode, try admin credentials
            panelHolder[0].setPortalMode(LoginPanel.PortalMode.STUDENT);
        });

        LoginPanel panel = panelHolder[0];
        Field uField = LoginPanel.class.getDeclaredField("usernameField");
        Field pField = LoginPanel.class.getDeclaredField("passwordField");
        Field errLblField = LoginPanel.class.getDeclaredField("errorLabel");

        uField.setAccessible(true);
        pField.setAccessible(true);
        errLblField.setAccessible(true);

        AppTextField usernameField = (AppTextField) uField.get(panel);
        AppPasswordField passwordField = (AppPasswordField) pField.get(panel);
        javax.swing.JLabel errorLabel = (javax.swing.JLabel) errLblField.get(panel);

        Method handleLoginMethod = LoginPanel.class.getDeclaredMethod("handleLogin");
        handleLoginMethod.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            usernameField.setText(TestCredentials.getAdminUsername());
            passwordField.setText(TestCredentials.getAdminPassword());
            try {
                handleLoginMethod.invoke(panel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(800);

        SwingUtilities.invokeAndWait(() -> {
            if (!errorLabel.getText().isEmpty()) {
                pass("Admin credentials rejected in Student Portal as expected: " + errorLabel.getText());
            } else {
                fail("Admin should not authenticate in Student portal mode");
            }
        });
    }

    private static void testResponsiveSizes() throws Exception {
        System.out.println("\n--- 7. Responsive Resizing & Layout Verification ---");
        final LoginFrame[] frameHolder = new LoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frameHolder[0] = new LoginFrame();
        });

        LoginFrame frame = frameHolder[0];
        int[][] resolutions = {
            {1440, 900},
            {1280, 800},
            {1024, 768},
            {900, 700},
            {840, 620}
        };

        for (int[] res : resolutions) {
            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(res[0], res[1]);
                frame.doLayout();
                frame.validate();
            });
            pass("Window layout validated without errors at " + res[0] + "x" + res[1]);
        }

        SwingUtilities.invokeAndWait(frame::dispose);
    }
}
