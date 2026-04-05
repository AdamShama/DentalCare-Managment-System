package control;

import entity.Dentist;               // Dentist(String, String, String, String, boolean, List<Appointment>) :contentReference[oaicite:0]{index=0}
import entity.DbConsts;
import entity.Appointment;           // Schedule is a List<Appointment> :contentReference[oaicite:1]{index=1}

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class dentistControl {
    private final Connection conn;

    public dentistControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /**
     * Map a single ResultSet row (from Staff) into a Dentist.
     */
    private Dentist mapRow(ResultSet rs) throws SQLException {
        String id             = rs.getString(DbConsts.COL_STAFF_ID);
        String fullName       = rs.getString(DbConsts.COL_STAFF_FULLNAME);
        String contact        = rs.getString(DbConsts.COL_STAFF_CONTACT);
        String specialization = rs.getString(DbConsts.COL_STAFF_SPECIALTY);
        boolean isManager     = rs.getBoolean(DbConsts.COL_STAFF_IS_MANAGER);

        // start with an empty schedule; you can load real Appointments as needed
        List<Appointment> schedule = new ArrayList<>();
        return new Dentist(id, fullName, contact, specialization, isManager, schedule);
    }

    /**
     * Fetch all Dentists (i.e. Staff WHERE Role='Dentist').
     */
    public List<Dentist> getAll() throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_STAFF +
                     " WHERE " + DbConsts.COL_STAFF_ROLE + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Dentist");
            try (ResultSet rs = ps.executeQuery()) {
                List<Dentist> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        }
    }

    /**
     * Fetch one Dentist by their staff‐ID.
     */
    public Dentist getById(String id) throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_STAFF +
                     " WHERE " + DbConsts.COL_STAFF_ID + " = ?" +
                     "   AND "   + DbConsts.COL_STAFF_ROLE + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, "Dentist");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Insert a new Dentist.
     */
    public boolean insert(Dentist d) throws SQLException {
        String sql = "INSERT INTO " + DbConsts.TABLE_STAFF +
                     " (" +
                       DbConsts.COL_STAFF_ID           + ", " +
                       DbConsts.COL_STAFF_FULLNAME         + ", " +
                       DbConsts.COL_STAFF_CONTACT      + ", " +
                       DbConsts.COL_STAFF_ROLE         + ", " +
                       DbConsts.COL_STAFF_SPECIALTY    + ", " +
                       DbConsts.COL_STAFF_IS_MANAGER   +
                     ") VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, d.getStaffID());
            ps.setString(2, d.getFullName());
            ps.setString(3, d.getContactNumber());
            ps.setString(4, "Dentist");
            ps.setString(5, d.getSpecialization());
            ps.setBoolean(6, d.isManager());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Update an existing Dentist.
     */
    public boolean update(Dentist d) throws SQLException {
        String sql = "UPDATE " + DbConsts.TABLE_STAFF + " SET " +
                     DbConsts.COL_STAFF_FULLNAME         + " = ?, " +
                     DbConsts.COL_STAFF_CONTACT      + " = ?, " +
                     DbConsts.COL_STAFF_SPECIALTY    + " = ?, " +
                     DbConsts.COL_STAFF_IS_MANAGER   + " = ? " +
                     "WHERE " + DbConsts.COL_STAFF_ID   + " = ?" +
                     "  AND "   + DbConsts.COL_STAFF_ROLE + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getFullName());
            ps.setString(2, d.getContactNumber());
            ps.setString(3, d.getSpecialization());
            ps.setBoolean(4, d.isManager());
            ps.setInt   (5, d.getStaffID());
            ps.setString(6, "Dentist");
            return ps.executeUpdate() == 1;
        }
    }


    /**
     * Delete a Dentist by ID.
     */
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM " + DbConsts.TABLE_STAFF +
                     " WHERE " + DbConsts.COL_STAFF_ID   + " = ?" +
                     "   AND "   + DbConsts.COL_STAFF_ROLE + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, "Dentist");
            return ps.executeUpdate() == 1;
        }
    }
    
    public boolean isManager(int staffId) throws SQLException {
        String sql =
          "SELECT r." + DbConsts.COL_ROLE_NAME +
          "  FROM " + DbConsts.TABLE_ROLES + " r" +
          "  JOIN " + DbConsts.TABLE_STAFF + " s ON r." + DbConsts.COL_ROLE_ID +
              " = s." + DbConsts.COL_STAFF_ROLE_ID +
          " WHERE s." + DbConsts.COL_STAFF_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "Manager".equalsIgnoreCase(rs.getString(1));
            }
        }
    }
    
    
 // in control/dentistControl.java
    public boolean isDentist(int staffId) throws SQLException {
        String sql = """
          SELECT COUNT(*) 
            FROM Staff s 
            JOIN Roles r ON s.RoleID = r.RoleID
           WHERE s.StaffID = ? 
             AND r.RoleName = 'Dentist'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // in control/hygienistControl.java
    public boolean isHygienist(int staffId) throws SQLException {
        String sql = """
          SELECT COUNT(*) 
            FROM Staff s 
            JOIN Roles r ON s.RoleID = r.RoleID
           WHERE s.StaffID = ? 
             AND r.RoleName = 'Hygienist'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

}
