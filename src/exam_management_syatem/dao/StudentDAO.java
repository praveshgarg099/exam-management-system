package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public int insert(Student student, Connection conn) throws SQLException {
        String sql = "INSERT INTO students (name, mobile, email, aadhar_no, date_of_birth, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getMobile());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, student.getAadharNo());
            pstmt.setString(5, student.getDateOfBirth());
            pstmt.setBoolean(6, student.isActive());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for inserted student.");
    }

    public void update(Student student) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            update(student, conn);
        }
    }

    public void update(Student student, Connection conn) throws SQLException {
        String sql = "UPDATE students SET name = ?, mobile = ?, email = ?, aadhar_no = ?, date_of_birth = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getMobile());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, student.getAadharNo());
            pstmt.setString(5, student.getDateOfBirth());
            pstmt.setInt(6, student.getId());
            pstmt.executeUpdate();
        }
    }

    public void setActive(int studentId, boolean active) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            setActive(studentId, active, conn);
        }
    }

    public void setActive(int studentId, boolean active, Connection conn) throws SQLException {
        String sql = "UPDATE students SET active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT id, name, mobile, email, aadhar_no, date_of_birth, active, created_at, updated_at FROM students WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Student findByAadhar(String aadharNo) throws SQLException {
        String sql = "SELECT id, name, mobile, email, aadhar_no, date_of_birth, active, created_at, updated_at FROM students WHERE aadhar_no = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, aadharNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Student> listAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, name, mobile, email, aadhar_no, date_of_birth, active, created_at, updated_at FROM students ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Student> search(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, name, mobile, email, aadhar_no, date_of_birth, active, created_at, updated_at FROM students "
                + "WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR mobile LIKE ? OR aadhar_no LIKE ? ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setMobile(rs.getString("mobile"));
        s.setEmail(rs.getString("email"));
        s.setAadharNo(rs.getString("aadhar_no"));
        s.setDateOfBirth(rs.getString("date_of_birth"));
        s.setActive(rs.getBoolean("active"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
