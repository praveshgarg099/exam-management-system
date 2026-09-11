package exam_management_syatem.service;

import exam_management_syatem.dao.UserDAO;
import exam_management_syatem.model.User;
import exam_management_syatem.security.PasswordHasher;
import exam_management_syatem.security.UserSession;

import java.security.SecureRandom;
import java.sql.SQLException;

public class AuthenticationService {
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserSession loginAdmin(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim().toLowerCase());
        if (user == null || !user.isAdmin() || !user.isActive()) {
            throw new Exception("Invalid username or password.");
        }
        if (!PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            throw new Exception("Invalid username or password.");
        }
        return new UserSession(user.getId(), user.getUsername(), user.getRole(), user.getStudentId());
    }

    public UserSession loginStudent(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim().toLowerCase());
        if (user == null || !user.isStudent() || !user.isActive()) {
            throw new Exception("Invalid username or password or account is inactive.");
        }
        if (!PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            throw new Exception("Invalid username or password.");
        }
        return new UserSession(user.getId(), user.getUsername(), user.getRole(), user.getStudentId());
    }

    public void changeStudentPassword(int studentId, String currentPassword, String newPassword) throws Exception {
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Current password is required.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        User user = userDAO.findByStudentId(studentId);
        if (user == null) {
            throw new Exception("User account not found for student.");
        }
        if (!PasswordHasher.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new Exception("Current password verification failed.");
        }
        userDAO.updatePassword(user.getId(), PasswordHasher.hashPassword(newPassword));
    }

    public String adminResetStudentPassword(int studentId) throws Exception {
        User user = userDAO.findByStudentId(studentId);
        if (user == null) {
            throw new Exception("User account not found for student.");
        }
        // Generate random 8-character temporary password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("Temp@");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String tempPassword = sb.toString();

        userDAO.updatePassword(user.getId(), PasswordHasher.hashPassword(tempPassword));
        return tempPassword;
    }

    public void changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters long.");
        }
        userDAO.updatePassword(userId, PasswordHasher.hashPassword(newPassword));
    }
}
