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

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }
}
