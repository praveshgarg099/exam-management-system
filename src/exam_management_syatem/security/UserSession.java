package exam_management_syatem.security;

public class UserSession {
    private final int userId;
    private final String username;
    private final String role;
    private final Integer studentId;

    public UserSession(int userId, String username, String role, Integer studentId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.studentId = studentId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public boolean isAuthenticated() {
        return userId > 0 && role != null && !role.trim().isEmpty();
    }

    public boolean isAdmin() {
        return isAuthenticated() && "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return isAuthenticated() && "STUDENT".equalsIgnoreCase(role) && studentId != null && studentId > 0;
    }

    public void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new SecurityException("Access Denied: Unauthenticated session.");
        }
    }

    public void requireAdmin() {
        requireAuthenticated();
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Administrator role required.");
        }
    }

    public void requireStudent() {
        requireAuthenticated();
        if (!isStudent()) {
            throw new SecurityException("Access Denied: Student role required.");
        }
    }

    public boolean canAccessStudent(int targetStudentId) {
        if (!isAuthenticated()) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        return isStudent() && studentId != null && studentId == targetStudentId;
    }

    public void requireStudentAccess(int targetStudentId) {
        requireAuthenticated();
        if (!canAccessStudent(targetStudentId)) {
            throw new SecurityException("Access Denied: Cannot access data for student ID " + targetStudentId + " with caller student ID " + studentId);
        }
    }
}
