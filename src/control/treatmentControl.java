// src/control/treatmentControl.java
package control;

import entity.DbConsts;
import entity.Treatment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class treatmentControl {
    private final Connection conn;

    public treatmentControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

 // ───────────────────── CREATE ─────────────────────
    public boolean addTreatment(Treatment t) {
        String sql = "INSERT INTO " + DbConsts.TABLE_TREATMENTS +
                     " ("  + DbConsts.COL_TREAT_TYPE + "," 
                            + DbConsts.COL_TREAT_PHASE + ","
                            + DbConsts.COL_TREAT_COST  + ")" +
                     " VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getDescription());
            // NOTE: your Treatment.cost field is BigDecimal in the DB
            ps.setBigDecimal(3, BigDecimal.valueOf(t.getCost()));
            return ps.executeUpdate() == 1;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ───────────────────── UPDATE ─────────────────────
    public boolean updateTreatment(Treatment t) {
        String sql = "UPDATE " + DbConsts.TABLE_TREATMENTS + " SET "
                   + DbConsts.COL_TREAT_TYPE  + " = ?, "
                   + DbConsts.COL_TREAT_PHASE + " = ?, "
                   + DbConsts.COL_TREAT_COST  + " = ? "
                   + "WHERE " + DbConsts.COL_TREAT_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getDescription());
            ps.setBigDecimal(3, BigDecimal.valueOf(t.getCost()));
            ps.setInt(4, t.getTreatmentId());
            return ps.executeUpdate() == 1;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ───────────────────── DELETE ─────────────────────
    public boolean deleteTreatment(int id) {
        String sql = "DELETE FROM " + DbConsts.TABLE_TREATMENTS +
                     " WHERE " + DbConsts.COL_TREAT_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ───────────────────── READ ALL ────────────────────
    public List<Treatment> getAllTreatments() {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT * FROM " + DbConsts.TABLE_TREATMENTS;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Treatment(
                    rs.getInt   (DbConsts.COL_TREAT_ID),
                    rs.getString(DbConsts.COL_TREAT_TYPE),
                    rs.getString(DbConsts.COL_TREAT_PHASE),
                    rs.getBigDecimal(DbConsts.COL_TREAT_COST).doubleValue()
                ));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

}
