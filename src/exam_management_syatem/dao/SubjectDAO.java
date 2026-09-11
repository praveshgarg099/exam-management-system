package exam_management_syatem.dao;

import exam_management_syatem.db.DatabaseManager;
import exam_management_syatem.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public int insert(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (name, active) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.isActive() ? 1 : 0);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for subject.");
    }

    public Subject findByName(String name) throws SQLException {
        String sql = "SELECT id, name, active, created_at, updated_at FROM subjects WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Subject findById(int id) throws SQLException {
        String sql = "SELECT id, name, active, created_at, updated_at FROM subjects WHERE id = ?";
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

    public List<Subject> listActive() throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, name, active, created_at, updated_at FROM subjects WHERE active = 1 ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Subject> listAll() throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, name, active, created_at, updated_at FROM subjects ORDER BY name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void update(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getId());
            pstmt.executeUpdate();
        }
    }

    public void setActive(int subjectId, boolean active) throws SQLException {
        String sql = "UPDATE subjects SET active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, active ? 1 : 0);
            pstmt.setInt(2, subjectId);
            pstmt.executeUpdate();
        }
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setActive(rs.getInt("active") == 1);
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
