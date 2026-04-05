// src/control/TreatmentPlanController.java
package control;

import entity.DbConsts;
import entity.Patient;
import entity.TreatmentPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreatmentPlanController {
    private final Connection conn;
    private final patientControl pCtrl;    // match your controller class name

    public TreatmentPlanController() throws SQLException {
        conn  = DriverManager.getConnection(DbConsts.CONN_STR);
        pCtrl = new patientControl();       // initialize here
    }

    private TreatmentPlan mapRow(ResultSet rs) throws SQLException {
        String planId = rs.getString(DbConsts.COL_PLAN_ID);
        String patId  = rs.getString(DbConsts.COL_PLAN_PATIENT_ID);

        // iterate your List<Patient> from getAllPatients():
        Patient pat = null;
        for (Patient p : pCtrl.getAllPatients()) {
            if (p.getPatientId().equals(patId)) {
                pat = p;
                break;
            }
        }
        if (pat == null) {
            throw new SQLException("No patient found for ID " + patId);
        }

        // now build the TreatmentPlan as before
        LocalDate start     = rs.getDate(DbConsts.COL_PLAN_START_DATE).toLocalDate();
        Date    endRaw      = rs.getDate(DbConsts.COL_PLAN_END_DATE);
        LocalDate end       = (endRaw == null ? null : endRaw.toLocalDate());
        String  status      = rs.getString(DbConsts.COL_PLAN_STATUS);
        TreatmentPlan.PlanStatus stEnum =
          TreatmentPlan.PlanStatus.valueOf(status.toUpperCase());

        TreatmentPlan tp = new TreatmentPlan(planId, pat, start, end);
        tp.setStatus(stEnum);
        return tp;
    }

    /** Fetch all plans for a patient. */
    public List<TreatmentPlan> getByPatient(String patientId) throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_TREATMENT_PLANS +
                     " WHERE " + DbConsts.COL_PLAN_PATIENT_ID + " = ?" +
                     " ORDER BY " + DbConsts.COL_PLAN_START_DATE + " DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TreatmentPlan> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    /** Create a new plan (ID generated in code). */
    public boolean insert(TreatmentPlan plan) throws SQLException {
        // 1) figure out next PlanID purely in Java
        String maxSql = "SELECT MAX(" + DbConsts.COL_PLAN_ID + ") FROM "
                        + DbConsts.TABLE_TREATMENT_PLANS;
        int nextId = 0;
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(maxSql)) {
            if (rs.next()) nextId = rs.getInt(1);
        }
        nextId++;

        // 2) now do the INSERT
        String sql = "INSERT INTO " + DbConsts.TABLE_TREATMENT_PLANS + "("
            + DbConsts.COL_PLAN_ID + ","
            + DbConsts.COL_PLAN_PATIENT_ID + ","
            + DbConsts.COL_PLAN_START_DATE + ","
            + DbConsts.COL_PLAN_END_DATE + ","
            + DbConsts.COL_PLAN_STATUS + ") "
          + "VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, nextId);
            ps.setInt   (2, Integer.parseInt(plan.getPatient().getPatientId()));
            ps.setDate  (3, Date.valueOf(plan.getStartDate()));
            if (plan.getEstimatedCompletion() != null)
                ps.setDate(4, Date.valueOf(plan.getEstimatedCompletion()));
            else
                ps.setNull(4, Types.DATE);
            ps.setString(5, plan.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected == 1) {
                plan.setId(String.valueOf(nextId));
            }
            return affected == 1;
        }
    }

    /** Update an existing plan. */
    public boolean update(TreatmentPlan p) throws SQLException {
        String sql = "UPDATE " + DbConsts.TABLE_TREATMENT_PLANS + " SET "
            + DbConsts.COL_PLAN_START_DATE + " = ?, "
            + DbConsts.COL_PLAN_END_DATE   + " = ?, "
            + DbConsts.COL_PLAN_STATUS     + " = ? "
            + "WHERE " + DbConsts.COL_PLAN_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(p.getStartDate()));
            if (p.getEstimatedCompletion() != null)
                ps.setDate(2, Date.valueOf(p.getEstimatedCompletion()));
            else
                ps.setNull(2, Types.DATE);
            ps.setString(3, p.getStatus().name());
            ps.setString(4, p.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Cancel (delete) a plan. */
    public boolean delete(String planId) throws SQLException {
        String sql = "DELETE FROM " + DbConsts.TABLE_TREATMENT_PLANS
                   + " WHERE " + DbConsts.COL_PLAN_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, planId);
            return ps.executeUpdate() == 1;
        }
    }
}
