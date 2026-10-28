package com.sset.erp.repository;

import com.sset.erp.model.AdminUser;
import com.sset.erp.model.FacultyUser;
import com.sset.erp.model.ParentUser;
import com.sset.erp.model.Role;
import com.sset.erp.model.StudentUser;
import com.sset.erp.model.User;
import com.sset.erp.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * MySQL-backed implementation of UserRepository that reads and writes the
 * `edumanage` database on the local SQL server (users + role detail tables).
 */
public class MysqlUserRepository implements UserRepository {

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Mapping helpers ─────────────────────────────────────────────────

    private Optional<User> rowToOptional(ResultSet rs) {
        try {
            String id = rs.getString("id");
            if (id == null) return Optional.empty();

            Role role = Role.valueOf(rs.getString("role").trim().toUpperCase());
            java.sql.Date created = rs.getDate("created_at");
            java.util.Date createdAt = created != null ? new java.util.Date(created.getTime()) : new java.util.Date();
            boolean active = rs.getBoolean("active");
            String username = rs.getString("username");
            String passwordHash = rs.getString("password_hash");
            String salt = rs.getString("salt");
            String fullName = rs.getString("full_name");
            String email = rs.getString("email");

            switch (role) {
                case ADMIN:
                    return Optional.of(new AdminUser(id, username, passwordHash, salt, fullName,
                            email, active, createdAt, stringIfPresent(rs, "admin_level")));
                case FACULTY:
                    return Optional.of(new FacultyUser(id, username, passwordHash, salt, fullName,
                            email, active, createdAt,
                            stringIfPresent(rs, "employee_id"),
                            orDefault(stringIfPresent(rs, "department"), "CSE"),
                            orDefault(stringIfPresent(rs, "designation"), "Faculty"),
                            stringIfPresent(rs, "advisor_department"),
                            intIfPresent(rs, "advisor_semester"),
                            stringIfPresent(rs, "advisor_batch")));
                case STUDENT:
                    return Optional.of(new StudentUser(id, username, passwordHash, salt, fullName,
                            email, active, createdAt,
                            stringIfPresent(rs, "roll_number"),
                            orDefault(stringIfPresent(rs, "department"), "CSE"),
                            intIfPresent(rs, "semester"),
                            stringIfPresent(rs, "batch")));
                case PARENT:
                    return Optional.of(new ParentUser(id, username, passwordHash, salt, fullName,
                            email, active, createdAt,
                            stringIfPresent(rs, "student_roll_number"),
                            stringIfPresent(rs, "emergency_contact")));
                default:
                    return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("[MysqlUserRepository] Row mapping failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    private String orDefault(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private String stringIfPresent(ResultSet rs, String column) {
        try {
            return hasColumn(rs, column) ? rs.getString(column) : null;
        } catch (SQLException e) {
            return null;
        }
    }

    private int intIfPresent(ResultSet rs, String column) {
        try {
            return hasColumn(rs, column) ? rs.getInt(column) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * The detail column set differs per role, so users are loaded via a LEFT JOIN per role.
     */
    private String detailColumnsFor(Role role) {
        switch (role) {
            case ADMIN:
                return "a.admin_level";
            case FACULTY:
                return "a.employee_id, a.department, a.designation, a.advisor_department, a.advisor_semester, a.advisor_batch";
            case STUDENT:
                return "a.roll_number, a.department, a.semester, a.batch";
            case PARENT:
                return "a.student_roll_number, a.emergency_contact";
            default:
                return "NULL AS admin_level";
        }
    }

    private String detailTableFor(Role role) {
        switch (role) {
            case ADMIN:  return "admins";
            case FACULTY: return "faculty";
            case STUDENT: return "students";
            case PARENT: return "parents";
            default: return null;
        }
    }

    private String selectSql(Role role) {
        String table = detailTableFor(role);
        String detail = detailColumnsFor(role);
        if (table == null) {
            return "SELECT u.id, u.username, u.password_hash, u.salt, u.full_name, u.email, "
                    + "u.role, u.active, u.created_at, NULL AS admin_level FROM users u";
        }
        return "SELECT u.id, u.username, u.password_hash, u.salt, u.full_name, u.email, "
                + "u.role, u.active, u.created_at, " + detail
                + " FROM users u LEFT JOIN " + table + " a ON a.user_id = u.id";
    }

    // ── UserRepository API ──────────────────────────────────────────────

    @Override
    public Optional<User> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        for (Role role : Role.values()) {
            String sql = selectSql(role) + " WHERE u.id = ?";
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Optional<User> user = rowToOptional(rs);
                        if (user.isPresent()) return user;
                    }
                }
            } catch (SQLException ignored) {}
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        for (Role role : Role.values()) {
            String sql = selectSql(role) + " WHERE LOWER(u.username) = ?";
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, username.trim().toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Optional<User> user = rowToOptional(rs);
                        if (user.isPresent()) return user;
                    }
                }
            } catch (SQLException ignored) {}
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        for (Role role : Role.values()) {
            String sql = selectSql(role) + " WHERE LOWER(u.email) = ?";
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Optional<User> user = rowToOptional(rs);
                        if (user.isPresent()) return user;
                    }
                }
            } catch (SQLException ignored) {}
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> all = new ArrayList<>();
        for (Role role : Role.values()) {
            try (PreparedStatement ps = connection().prepareStatement(selectSql(role) + " WHERE u.role = ?")) {
                ps.setString(1, role.name());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rowToOptional(rs).ifPresent(all::add);
                    }
                }
            } catch (SQLException ignored) {}
        }
        all.sort(Comparator.comparing(User::getFullName));
        return all;
    }

    @Override
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(selectSql(role) + " WHERE u.role = ?")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rowToOptional(rs).ifPresent(list::add);
                }
            }
        } catch (SQLException ignored) {}
        list.sort(Comparator.comparing(User::getFullName));
        return list;
    }

    @Override
    public User save(User user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Cannot save null user or user with null username");
        }
        try {
            Connection conn = connection();

            // Upsert base row
            String baseSql = "INSERT INTO users (id, username, password_hash, salt, full_name, email, role, active, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE username = VALUES(username), password_hash = VALUES(password_hash), "
                    + "salt = VALUES(salt), full_name = VALUES(full_name), email = VALUES(email), active = VALUES(active)";
            try (PreparedStatement ps = conn.prepareStatement(baseSql)) {
                ps.setString(1, user.getId());
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, user.getSalt());
                ps.setString(5, user.getFullName());
                ps.setString(6, user.getEmail());
                ps.setString(7, user.getRole().name());
                ps.setBoolean(8, user.isActive());
                ps.setDate(9, new java.sql.Date(user.getCreatedAt().getTime()));
                ps.executeUpdate();
            }

            // Upsert role detail row (CREATE → INSERT IGNORE; UPDATE → values refreshed)
            String detailSql = detailUpsertFor(user);
            if (detailSql != null) {
                try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
                    bindDetailParams(user, ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[MysqlUserRepository] save failed: " + e.getMessage());
        }
        return user;
    }

    private String detailUpsertFor(User user) {
        if (user instanceof AdminUser) {
            return "INSERT INTO admins (user_id, admin_level) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE admin_level = VALUES(admin_level)";
        }
        if (user instanceof FacultyUser) {
            return "INSERT INTO faculty (user_id, employee_id, department, designation, advisor_department, advisor_semester, advisor_batch) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE employee_id = VALUES(employee_id), department = VALUES(department), "
                    + "designation = VALUES(designation), advisor_department = VALUES(advisor_department), "
                    + "advisor_semester = VALUES(advisor_semester), advisor_batch = VALUES(advisor_batch)";
        }
        if (user instanceof StudentUser) {
            return "INSERT INTO students (user_id, roll_number, department, semester, batch) VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE roll_number = VALUES(roll_number), department = VALUES(department), "
                    + "semester = VALUES(semester), batch = VALUES(batch)";
        }
        if (user instanceof ParentUser) {
            return "INSERT INTO parents (user_id, student_roll_number, emergency_contact) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE student_roll_number = VALUES(student_roll_number), "
                    + "emergency_contact = VALUES(emergency_contact)";
        }
        return null;
    }

    private void bindDetailParams(User user, PreparedStatement ps) throws SQLException {
        if (user instanceof AdminUser) {
            ps.setString(1, user.getId());
            ps.setString(2, ((AdminUser) user).getAdminLevel());
        } else if (user instanceof FacultyUser) {
            FacultyUser f = (FacultyUser) user;
            ps.setString(1, user.getId());
            ps.setString(2, f.getEmployeeId());
            ps.setString(3, f.getDepartment());
            ps.setString(4, f.getDesignation());
            ps.setString(5, f.getAdvisorDepartment());
            ps.setInt(6, f.getAdvisorSemester());
            ps.setString(7, f.getAdvisorBatch());
        } else if (user instanceof StudentUser) {
            StudentUser s = (StudentUser) user;
            ps.setString(1, user.getId());
            ps.setString(2, s.getRollNumber());
            ps.setString(3, s.getDepartment());
            ps.setInt(4, s.getSemester());
            ps.setString(5, s.getBatch());
        } else if (user instanceof ParentUser) {
            ParentUser p = (ParentUser) user;
            ps.setString(1, user.getId());
            ps.setString(2, p.getStudentRollNumber());
            ps.setString(3, p.getEmergencyContact());
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) return false;
        // Detail rows cascade via ON DELETE CASCADE foreign keys
        try (PreparedStatement ps = connection().prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MysqlUserRepository] delete failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException ignored) {}
        return 0;
    }

    @Override
    public long countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }
}
