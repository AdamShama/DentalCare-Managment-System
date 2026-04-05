// treatmentReportsControl.java
package control;

import entity.TreatmentReport;
import entity.DbConsts;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class treatmentReportsControl {
    private final Connection conn;

    public treatmentReportsControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    private TreatmentReport mapRow(ResultSet rs) throws SQLException {
        return new TreatmentReport(
            rs.getInt("ReportID"),
            rs.getInt("TreatmentID"),
            rs.getInt("PatientID"),
            rs.getTimestamp("ReportDate").toLocalDateTime(),
            rs.getString("Notes")
        );
    }

    public List<TreatmentReport> getAll() throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_TREATMENT_REPORTS;
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            List<TreatmentReport> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    public boolean insert(TreatmentReport r) throws SQLException {
        String sql = "INSERT INTO " + DbConsts.TABLE_TREATMENT_REPORTS +
                     " (ReportID,TreatmentID,PatientID,ReportDate,Notes) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getReportId());
            ps.setInt(2, r.getTreatmentId());
            ps.setInt(3, r.getPatientId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getReportDate()));
            ps.setString(5, r.getNotes());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int reportId) throws SQLException {
        String sql = "DELETE FROM " + DbConsts.TABLE_TREATMENT_REPORTS
                   + " WHERE ReportID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            return ps.executeUpdate() == 1;
        }
    }
}
