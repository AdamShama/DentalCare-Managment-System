// src/control/StaffControl.java
package control;

import entity.DbConsts;
import entity.Staff;

import java.sql.*;
import java.util.*;

/**
 * CRUD operations for Staff + Roles.
 */
public class StaffControl {
    private final Connection conn;

    public StaffControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /** Returns map of RoleID → RoleName. */
    public Map<Integer,String> getRoles() throws SQLException {
        String sql = "SELECT "
                   +   DbConsts.COL_ROLE_ID   + ", "
                   +   DbConsts.COL_ROLE_NAME
                   + " FROM " + DbConsts.TABLE_ROLES
                   + " ORDER BY " + DbConsts.COL_ROLE_NAME;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql))
        {
            Map<Integer,String> m = new LinkedHashMap<>();
            while (rs.next()) {
                m.put(rs.getInt(1), rs.getString(2));
            }
            return m;
        }
    }

    /** Fetches all staff rows. */
    public List<Staff> getAll() throws SQLException {
        String sql = "SELECT "
                   +   DbConsts.COL_STAFF_ID            + ","
                   +   DbConsts.COL_STAFF_FULLNAME      + ","
                   +   DbConsts.COL_STAFF_ROLE_ID       + ","
                   +   DbConsts.COL_STAFF_CONTACT       + ","
                   +   DbConsts.COL_STAFF_EMAIL         + ","
                   +   DbConsts.COL_STAFF_QUALIFICATION + ","
                   +   DbConsts.COL_STAFF_SPECIALIZATION_ID
                   + " FROM " + DbConsts.TABLE_STAFF
                   + " ORDER BY " + DbConsts.COL_STAFF_FULLNAME;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql))
        {
            List<Staff> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Staff(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getInt(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getInt(7)
                ));
            }
            return list;
        }
    }

    public int getNextStaffID() throws SQLException {
        String sql = "SELECT MAX(" + DbConsts.COL_STAFF_ID + ") FROM " + DbConsts.TABLE_STAFF;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql))
        {
            if (rs.next()) {
                int max = rs.getInt(1);
                // if table empty, rs.getInt(1) returns 0
                return max + 1;
            } else {
                return 1;
            }
        }
    }

    /**
     * Insert a new staff row (your own code supplies StaffID).
     * @return the newly‐assigned StaffID
     */
    public int insert(Staff s) throws SQLException {
        int newId = getNextStaffID();
        String sql = "INSERT INTO " + DbConsts.TABLE_STAFF + " ("
                   +   DbConsts.COL_STAFF_ID             + ","
                   +   DbConsts.COL_STAFF_FULLNAME       + ","
                   +   DbConsts.COL_STAFF_ROLE_ID        + ","
                   +   DbConsts.COL_STAFF_CONTACT        + ","
                   +   DbConsts.COL_STAFF_EMAIL          + ","
                   +   DbConsts.COL_STAFF_QUALIFICATION  + ","
                   +   DbConsts.COL_STAFF_SPECIALIZATION_ID
                   + ") VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, newId);
            ps.setString(2, s.getFullName());
            ps.setInt   (3, s.getRoleID());
            ps.setString(4, s.getContactNumber());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getQualification());
            ps.setInt   (7, s.getSpecializationID());
            ps.executeUpdate();
            return newId;
        }
    }

    


    /** Updates an existing staff row. */
    public boolean update(Staff s) throws SQLException {
        String sql = "UPDATE " + DbConsts.TABLE_STAFF + " SET "
                   +   DbConsts.COL_STAFF_FULLNAME       + "=?,"
                   +   DbConsts.COL_STAFF_ROLE_ID        + "=?,"
                   +   DbConsts.COL_STAFF_CONTACT        + "=?,"
                   +   DbConsts.COL_STAFF_EMAIL          + "=?,"
                   +   DbConsts.COL_STAFF_QUALIFICATION  + "=?,"
                   +   DbConsts.COL_STAFF_SPECIALIZATION_ID + "=?"
                   + " WHERE " + DbConsts.COL_STAFF_ID + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setInt   (2, s.getRoleID());
            ps.setString(3, s.getContactNumber());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getQualification());
            ps.setInt   (6, s.getSpecializationID());
            ps.setInt   (7, s.getStaffID());
            return ps.executeUpdate() == 1;
        }
    }


    /** Deletes a staff row by StaffID. */
    public boolean delete(int staffID) throws SQLException {
        String sql = "DELETE FROM " + DbConsts.TABLE_STAFF
                   + " WHERE " + DbConsts.COL_STAFF_ID + "=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            return ps.executeUpdate() == 1;
        }
    }
    
    
    public boolean isDentist(int staffId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM Staff s
             JOIN Roles r ON s.RoleID = r.RoleID
             WHERE s.StaffID = ? AND r.RoleName = 'Dentist'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean isHygienist(int staffId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM Staff s
             JOIN Roles r ON s.RoleID = r.RoleID
             WHERE s.StaffID = ? AND r.RoleName = 'Hygienist'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
    
 // in control/StaffControl.java
    public boolean isManager(int staffId) throws SQLException {
        String sql = """
          SELECT COUNT(*) 
            FROM Staff s
            JOIN Roles r ON s.RoleID = r.RoleID
           WHERE s.StaffID = ? 
             AND r.RoleName = 'Manager'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

 // in control/StaffControl.java
    public String getRoleName(int staffId) throws SQLException {
        String sql = """
          SELECT r.RoleName
            FROM Staff s
            JOIN Roles r ON s.RoleID = r.RoleID
           WHERE s.StaffID = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("RoleName") : null;
            }
        }
    }



    public boolean isSecretary(int staffId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM Staff s
             JOIN Roles r ON s.RoleID = r.RoleID
             WHERE s.StaffID = ? AND r.RoleName = 'Secretary'
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
