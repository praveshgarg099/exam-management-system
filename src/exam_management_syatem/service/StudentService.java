package exam_management_syatem.service;

import exam_management_syatem.dao.StudentDAO;
import exam_management_syatem.dao.UserDAO;
import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.User;
import exam_management_syatem.security.PasswordHasher;
import exam_management_syatem.security.UserSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
    }

    public static class RegistrationResult {
        public final int studentId;
        public final String username;
        public final String password;

        public RegistrationResult(int studentId, String username, String password) {
            this.studentId = studentId;
            this.username = username;
            this.password = password;
        }
    }

    public RegistrationResult registerStudent(UserSession session, String name, String mobile, String email, String aadharNo, String dateOfBirth) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new IllegalArgumentException("Valid 10-digit mobile number is required.");
        }
        if (aadharNo == null || !aadharNo.matches("\\d{12}")) {
            throw new IllegalArgumentException("Valid 12-digit Aadhar number is required.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (dateOfBirth == null || !dateOfBirth.matches("\\d{8}")) {
            throw new IllegalArgumentException("Valid 8-digit DOB (DDMMYYYY) is required.");
        }

        if (studentDAO.findByAadhar(aadharNo.trim()) != null) {
            throw new IllegalArgumentException("A student with Aadhar number " + aadharNo + " is already registered.");
        }

        // Auto-generate username: first 4 chars of name + last 4 of Aadhar
        String cleanName = name.trim().replaceAll("\\s+", "");
        if (cleanName.length() < 4) {
            cleanName = (cleanName + "aaaa").substring(0, 4);
        } else {
            cleanName = cleanName.substring(0, 4);
        }
        String last4Aadhar = aadharNo.trim().substring(aadharNo.trim().length() - 4);
        String username = (cleanName + last4Aadhar).toLowerCase();

        // Initial password equals username
        String password = username;
        String passwordHash = PasswordHasher.hashPassword(password);

        Student s = new Student();
        s.setName(name.trim());
        s.setMobile(mobile.trim());
        s.setEmail(email.trim());
        s.setAadharNo(aadharNo.trim());
        s.setDateOfBirth(dateOfBirth.trim());

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int studentId = studentDAO.insert(s, conn);

            User u = new User();
            u.setRole("STUDENT");
            u.setStudentId(studentId);
            u.setActive(true);

            String baseUsername = username;
            String finalUsername = baseUsername;
            String finalPassword = baseUsername;
            int suffix = 0;

            while (true) {
                String candidateUsername = suffix == 0 ? baseUsername : (baseUsername + "_" + suffix);
                u.setUsername(candidateUsername);
                u.setPasswordHash(PasswordHasher.hashPassword(candidateUsername));

                java.sql.Savepoint sp = conn.setSavepoint("user_insert_sp");
                try {
                    userDAO.insert(u, conn);
                    conn.releaseSavepoint(sp);
                    finalUsername = candidateUsername;
                    finalPassword = candidateUsername;
                    break;
                } catch (SQLException sqle) {
                    if ("23505".equals(sqle.getSQLState())) {
                        conn.rollback(sp);
                        suffix++;
                        if (suffix > 1000) {
                            throw new SQLException("Unable to generate unique username after 1000 attempts", sqle);
                        }
                    } else {
                        throw sqle;
                    }
                }
            }

            conn.commit(); // Commit Transaction
            return new RegistrationResult(studentId, finalUsername, finalPassword);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new Exception("Student registration failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<Student> getAllStudents(UserSession session) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return studentDAO.listAll();
    }

    public Student getStudentById(UserSession session, int id) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireStudentAccess(id);
        return studentDAO.findById(id);
    }

    public List<Student> searchStudents(UserSession session, String keyword) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();
        return studentDAO.search(keyword);
    }

    public void updateStudent(UserSession session, Student s) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        if (s == null || s.getId() <= 0) {
            throw new IllegalArgumentException("Invalid student.");
        }
        session.requireStudentAccess(s.getId());

        if (s.getName() == null || s.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (s.getMobile() == null || !s.getMobile().matches("\\d{10}")) {
            throw new IllegalArgumentException("Valid 10-digit mobile number is required.");
        }
        if (s.getAadharNo() == null || !s.getAadharNo().matches("\\d{12}")) {
            throw new IllegalArgumentException("Valid 12-digit Aadhar number is required.");
        }
        if (s.getEmail() == null || !s.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (s.getDateOfBirth() == null || !s.getDateOfBirth().matches("\\d{8}")) {
            throw new IllegalArgumentException("Valid 8-digit DOB (DDMMYYYY) is required.");
        }

        Student existing = studentDAO.findByAadhar(s.getAadharNo().trim());
        if (existing != null && existing.getId() != s.getId()) {
            throw new IllegalArgumentException("Another student with Aadhar " + s.getAadharNo() + " already exists.");
        }

        studentDAO.update(s);
    }

    /**
     * Atomically toggles student and login user active status in a single database transaction.
     */
    public void setStudentActiveStatus(UserSession session, int studentId, boolean active) throws Exception {
        if (session == null) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
        session.requireAdmin();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                studentDAO.setActive(studentId, active, conn);
                userDAO.setActiveByStudentId(studentId, active, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
