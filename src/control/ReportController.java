// src/control/ReportController.java
package control;

import entity.Appointment;
import entity.DbConsts;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.table.DefaultTableModel;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportController {
    private final Connection conn;

    public ReportController() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    //────────────────────────────────────────────────────────────────────
    // 1) Your old “treatment‐progress” as a List<String> + Jasper viewer
    //────────────────────────────────────────────────────────────────────
    public List<String> generateTreatmentProgressReport(int dentistId) throws SQLException {
        String sql =
          "SELECT p.FullName    AS PatientName, " +
          "       tp." + DbConsts.COL_PLAN_START_DATE + " AS StartDate, " +
          "       tp." + DbConsts.COL_PLAN_STATUS     + " AS PlanStatus, " +
          "       ph." + DbConsts.COL_TREAT_TYPE      + " AS TreatmentType " +
          "FROM "   + DbConsts.TABLE_TREATMENT_PLANS + " tp " +
          "  JOIN " + DbConsts.TABLE_PATIENTS       + " p  ON p." + DbConsts.COL_PATIENT_ID + "=tp." + DbConsts.COL_PLAN_PATIENT_ID + " " +
          "  JOIN " + DbConsts.TABLE_TREATMENTS     + " ph ON ph." + DbConsts.COL_TREAT_PLAN_ID + "=tp." + DbConsts.COL_PLAN_ID + " " +
          "  JOIN " + DbConsts.TABLE_APPOINTMENTS   + " a  ON a." + DbConsts.COL_APPT_ID + "=ph." + DbConsts.COL_TREAT_APPT_ID + " " +
          "WHERE (tp." + DbConsts.COL_PLAN_STATUS + "='Active' " +
          "   OR a."  + DbConsts.COL_APPT_STATUS + "='" + Appointment.STATUS_SCHEDULED + "') " +
          "  AND a." + DbConsts.COL_APPT_STAFF_ID + "=? " +
          "ORDER BY tp." + DbConsts.COL_PLAN_START_DATE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            List<String> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(String.format("%s | %s | %s | %s",
                      rs.getString("PatientName"),
                      rs.getDate("StartDate"),
                      rs.getString("PlanStatus"),
                      rs.getString("TreatmentType")
                    ));
                }
            }
            return out;
        }
    }

    public void showTreatmentProgressJasper(int dentistId) throws JRException, SQLException {
        InputStream jr = getClass().getResourceAsStream("/reports/treatment_progress.jrxml");
        JasperReport rpt = JasperCompileManager.compileReport(jr);
        Map<String,Object> params = new HashMap<>();
        params.put("DentistParam", dentistId);
        JasperPrint print = JasperFillManager.fillReport(rpt, params, conn);
        JasperViewer.viewReport(print, false);
    }

    //────────────────────────────────────────────────────────────────────
    // 2) Your old “revenue” String‐list report
    //────────────────────────────────────────────────────────────────────
    public List<String> generateRevenueReport(int month, int year) throws SQLException {
        String sql =
          "SELECT ph.TreatmentType, ph.AssignedStaffID, SUM(i.Amount) AS Total " +
          "FROM " + DbConsts.TABLE_INVOICES + " i " +
          " JOIN " + DbConsts.TABLE_TREATMENTS + " ph ON ph.PlanID=i.PlanID " +
          "WHERE MONTH(i.IssuedDate)=? AND YEAR(i.IssuedDate)=? " +
          "GROUP BY ph.TreatmentType, ph.AssignedStaffID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            List<String> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("Total");
                    out.add(String.format("%s | %s | $%.2f",
                      rs.getString("TreatmentType"),
                      rs.getString("AssignedStaffID"),
                      total));
                }
            }
            return out;
        }
    }

    //────────────────────────────────────────────────────────────────────
    // 3) Your old “inventory usage” String‐list report
    //────────────────────────────────────────────────────────────────────
    public List<String> generateInventoryUsageReport(LocalDate from, LocalDate to) throws SQLException {
        String sql =
          "SELECT il.ItemName, SUM(il.ChangeAmount) AS Used " +
          "FROM InventoryLog il " +
          "WHERE il.ChangeDate BETWEEN ? AND ? " +
          "GROUP BY il.ItemName";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            List<String> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(String.format("%s | %d",
                      rs.getString("ItemName"),
                      rs.getInt("Used")));
                }
            }
            return out;
        }
    }


    //────────────────────────────────────────────────────────────────────
    // 4) TableModel: daily revenue by date (for in‐memory JRXML)
    //────────────────────────────────────────────────────────────────────
    public DefaultTableModel getMonthlyRevenue(int year, int month) throws SQLException {
        String sql =
          "SELECT " +
          "  Format(" + DbConsts.COL_INV_DATE   + ",'yyyy-MM-dd') AS SalesDate, " +
          "  SUM("   + DbConsts.COL_INV_AMOUNT + ")           AS TotalRevenue " +
          "FROM "   + DbConsts.TABLE_INVOICES   + " " +
          "WHERE Year("  + DbConsts.COL_INV_DATE + ") = ? " +
          "  AND Month(" + DbConsts.COL_INV_DATE + ") = ? " +
          "GROUP BY Format(" + DbConsts.COL_INV_DATE + ",'yyyy-MM-dd') " +
          "ORDER BY Format(" + DbConsts.COL_INV_DATE + ",'yyyy-MM-dd')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            DefaultTableModel mdl = new DefaultTableModel(
                new Object[]{"SalesDate","TotalRevenue"}, 0);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mdl.addRow(new Object[]{
                      rs.getString("SalesDate"),
                      rs.getDouble("TotalRevenue")
                    });
                }
            }
            return mdl;
        }
    }

    //────────────────────────────────────────────────────────────────────
    // 5) TableModel: expiration‐range inventory
    //────────────────────────────────────────────────────────────────────
    /**
     * Inventory Usage Report: for a given date range, returns
     * (UsageID, AppointmentID, ItemID, QuantityUsed, UsageDate).
     */
    public DefaultTableModel getInventoryUsage(LocalDate start, LocalDate end) throws SQLException {
        String sql =
          "SELECT " +
          "  iu.UsageID, " +
          "  iu.AppointmentID, " +
          "  iu.ItemID, " +
          "  iu.QuantityUsed, " +
          "  iu.UsageDate " +
          "FROM InventoryUsage iu " +
          "WHERE iu.UsageDate BETWEEN ? AND ? " +
          "ORDER BY iu.UsageDate";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Access wants java.sql.Date for a Date/Time column
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                DefaultTableModel model = new DefaultTableModel(
                    new String[]{"UsageID","AppointmentID","ItemID","QuantityUsed","UsageDate"}, 
                    0
                );
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt   ("UsageID"),
                        rs.getInt   ("AppointmentID"),
                        rs.getInt   ("ItemID"),
                        rs.getInt   ("QuantityUsed"),
                        rs.getDate  ("UsageDate")
                    });
                }
                return model;
            }
        }
    }


    //────────────────────────────────────────────────────────────────────
    // 6) Jasper: export daily‐revenue to PDF
    //────────────────────────────────────────────────────────────────────
    public void exportMonthlyRevenuePdf(int year, int month,
                                        String jrxmlRes,
                                        String pdfOut)
      throws JRException, SQLException
    {
        InputStream jr = getClass().getResourceAsStream(jrxmlRes);
        JasperReport report = JasperCompileManager.compileReport(jr);
        Map<String,Object> params = new HashMap<>();
        params.put("ReportTitle", "Revenue for " + year + "-" + String.format("%02d", month));
        DefaultTableModel tbl = getMonthlyRevenue(year, month);
        JRTableModelDataSource ds = new JRTableModelDataSource(tbl);
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperExportManager.exportReportToPdfFile(print, pdfOut);
    }

    /** view it on‐screen instead of PDF **/
    public void showMonthlyRevenueJasper(int year, int month,
                                         String jrxmlRes)
      throws JRException, SQLException
    {
        InputStream jr = getClass().getResourceAsStream(jrxmlRes);
        JasperReport report = JasperCompileManager.compileReport(jr);
        Map<String,Object> params = new HashMap<>();
        params.put("ReportTitle", "Revenue for " + year + "-" + String.format("%02d",month));
        JRTableModelDataSource ds = new JRTableModelDataSource(getMonthlyRevenue(year,month));
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperViewer.viewReport(print, false);
    }

    //────────────────────────────────────────────────────────────────────
    // 7) Jasper: export inventory‐usage to PDF
    //────────────────────────────────────────────────────────────────────
    public void exportInventoryUsagePdf(LocalDate from, LocalDate to,
                                        String jrxmlRes,
                                        String pdfOut)
      throws JRException, SQLException
    {
        InputStream jr = getClass().getResourceAsStream(jrxmlRes);
        JasperReport report = JasperCompileManager.compileReport(jr);
        Map<String,Object> params = new HashMap<>();
        params.put("DateFrom", from);
        params.put("DateTo", to);
        DefaultTableModel tbl = getInventoryUsage(from,to);
        JRTableModelDataSource ds = new JRTableModelDataSource(tbl);
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperExportManager.exportReportToPdfFile(print, pdfOut);
    }

    /** view it on‐screen instead of PDF **/
    public void showInventoryUsageJasper(LocalDate from, LocalDate to,
                                         String jrxmlRes)
      throws JRException, SQLException
    {
        InputStream jr = getClass().getResourceAsStream(jrxmlRes);
        JasperReport report = JasperCompileManager.compileReport(jr);
        Map<String,Object> params = new HashMap<>();
        params.put("DateFrom", from);
        params.put("DateTo", to);
        JRTableModelDataSource ds = new JRTableModelDataSource(getInventoryUsage(from,to));
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperViewer.viewReport(print,false);
    }
    
    public DefaultTableModel getMonthlyAppointmentCosts(int year, int month) throws SQLException {
        String sql =
          "SELECT a." + DbConsts.COL_APPT_ID   + " AS AppointmentID, " +
                 "t." + DbConsts.COL_TREAT_COST + " AS Cost " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " a " +
          "  JOIN " + DbConsts.TABLE_TREATMENTS + " t " +
          "    ON t." + DbConsts.COL_TREAT_APPT_ID + "=a." + DbConsts.COL_APPT_ID + " " +
          "WHERE Year(a." + DbConsts.COL_APPT_DATETIME + ")=? " +
          "  AND Month(a." + DbConsts.COL_APPT_DATETIME + ")=? " +
          "ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            DefaultTableModel mdl = new DefaultTableModel(
                new Object[]{"AppointmentID", "Cost"}, 0);
            double sum = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("AppointmentID");
                    double c = rs.getDouble("Cost");
                    mdl.addRow(new Object[]{ id, c });
                    sum += c;
                }
            }
            // add a final “TOTAL” row:
            mdl.addRow(new Object[]{ "TOTAL", sum });
            return mdl;
        }
    }

    /**
     * Show the above as a Jasper report (view only).
     * You must create /reports/AppointmentRevenue.jrxml on your classpath.
     */
    /**
     * Show the above as a Jasper report (view only).
     * You must create /reports/AppointmentRevenue.jrxml on your classpath.
     */
    public void showAppointmentRevenueJasper(int year, int month)
            throws JRException, SQLException
    {
        // 1) compile
        InputStream jr = getClass()
          .getResourceAsStream("/reports/AppointmentRevenue.jrxml");
        JasperReport report = JasperCompileManager.compileReport(jr);

        // 2) parameters
        Map<String,Object> params = new HashMap<>();
        params.put("ReportTitle",
          String.format("Appointments Revenue for %04d-%02d", year, month));

        // 3) data (this can throw SQLException)
        DefaultTableModel model = getMonthlyAppointmentCosts(year, month);
        JRTableModelDataSource ds = new JRTableModelDataSource(model);

        // 4) fill & view
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperViewer.viewReport(print, false);
    }

    
    /** View the monthly revenue Jasper report in a viewer. */
    public void showMonthlyRevenueJasper(int year, int month)
            throws JRException, SQLException, IOException
    {
        try (InputStream jr = getClass().getResourceAsStream("/reports/RevenueReport.jrxml")) {
            JasperReport report = JasperCompileManager.compileReport(jr);
            Map<String,Object> params = new HashMap<>();
            params.put("ReportTitle", "Revenue for " + year + "-" + String.format("%02d", month));

            // use your table‑model data source
            DefaultTableModel model = getMonthlyRevenue(year, month);
            JRTableModelDataSource ds = new JRTableModelDataSource(model);

            JasperPrint print = JasperFillManager.fillReport(report, params, ds);
            JasperViewer.viewReport(print, false);
        }
    }

    /** View the inventory usage Jasper report in a viewer. */
    public void showInventoryUsageJasper(LocalDate from, LocalDate to)
            throws JRException, SQLException, IOException
    {
        // compile
        try (InputStream jr = getClass().getResourceAsStream("/reports/InventoryUsage.jrxml")) {
            JasperReport report = JasperCompileManager.compileReport(jr);

            // no parameters needed, but you could pass from/to here if you want
            Map<String,Object> params = new HashMap<>();

            // 3) data source
            DefaultTableModel model = getInventoryUsage(from, to);
            JRTableModelDataSource ds = new JRTableModelDataSource(model);

            // 4) fill & view
            JasperPrint print = JasperFillManager.fillReport(report, params, ds);
            JasperViewer.viewReport(print, false);
        }
    }


    /**
     * Build a JRTableModelDataSource with all completed
     * treatments for a single patient in the given window.
     */
    public JRTableModelDataSource generateInvoiceForPatient(int patientId,
                                                            LocalDate from,
                                                            LocalDate to)
        throws SQLException
    {
        String sql =
          "SELECT a." + DbConsts.COL_APPT_DATETIME + " AS ApptDate, " +
          "       ph." + DbConsts.COL_TREAT_TYPE   + " AS TreatmentType, " +
          "       ph." + DbConsts.COL_TREAT_COST   + " AS Cost " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " a " +
          " JOIN " + DbConsts.TABLE_TREATMENTS   + " ph " +
          "   ON ph." + DbConsts.COL_TREAT_APPT_ID + "=a." + DbConsts.COL_APPT_ID + " " +
          "WHERE a." + DbConsts.COL_APPT_STATUS   + " = ? " +
          "  AND a." + DbConsts.COL_APPT_PATIENT_ID + " = ? " +
          "  AND a." + DbConsts.COL_APPT_DATETIME + " BETWEEN ? AND ? " +
          "ORDER BY a." + DbConsts.COL_APPT_DATETIME;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_COMPLETED);
            ps.setInt   (2, patientId);
            ps.setDate  (3, Date.valueOf(from));
            ps.setDate  (4, Date.valueOf(to));

            DefaultTableModel mdl = new DefaultTableModel(
              new Object[]{"ApptDate","TreatmentType","Cost"}, 0);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mdl.addRow(new Object[]{
                      rs.getDate("ApptDate").toLocalDate().toString(),
                      rs.getString("TreatmentType"),
                      rs.getDouble("Cost")
                    });
                }
            }
            return new JRTableModelDataSource(mdl);
        }
    }

    /**
     * Compile & show the per‑patient invoice Jasper report.
     */
    public void showPatientInvoice(int patientId,
                                   LocalDate from,
                                   LocalDate to)
        throws JRException, SQLException
    {
        // 1) compile
        InputStream jr = getClass().getResourceAsStream("/reports/PatientInvoice.jrxml");
        JasperReport report = JasperCompileManager.compileReport(jr);

        // 2) parameters
        Map<String,Object> params = new HashMap<>();
        params.put("PatientID",   patientId);
        params.put("FromDate",    from.toString());
        params.put("ToDate",      to.toString());

        // 3) data
        JRTableModelDataSource ds = generateInvoiceForPatient(patientId, from, to);

        // 4) view
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperViewer.viewReport(print, false);
    }

    
    public DefaultTableModel getCompletedAppointmentsForPatient(int patientId) throws SQLException {
        String sql =
          "SELECT a." + DbConsts.COL_APPT_ID       + " AS AppointmentID, " +
          "       Format(a." + DbConsts.COL_APPT_DATETIME + ",'yyyy-MM-dd') AS Date, " +
          "       t." + DbConsts.COL_TREAT_COST   + " AS Cost " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " a " +
          " JOIN " + DbConsts.TABLE_TREATMENTS   + " t " +
          "   ON t." + DbConsts.COL_TREAT_APPT_ID + "=a." + DbConsts.COL_APPT_ID + " " +
          "WHERE a." + DbConsts.COL_APPT_PATIENT_ID + "=? " +
          "  AND a." + DbConsts.COL_APPT_STATUS     + "=? " +
          "GROUP BY a." + DbConsts.COL_APPT_ID + ", a." + DbConsts.COL_APPT_DATETIME + ", t." + DbConsts.COL_TREAT_COST +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, Appointment.STATUS_COMPLETED);
            DefaultTableModel mdl = new DefaultTableModel(
                new Object[]{"AppointmentID","Date","Cost"}, 0
            );
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mdl.addRow(new Object[]{
                      rs.getInt("AppointmentID"),
                      rs.getString("Date"),
                      rs.getDouble("Cost")
                    });
                }
            }
            return mdl;
        }
    }

    
    /**
     * Data source: all treatments (type & cost) for one appointment.
     */
    /**
     * Data source: the appointment date + all treatments (type & cost) for one appointment.
     */
    public JRTableModelDataSource getInvoiceDetailForAppointment(int apptId) throws SQLException {
        String sql =
          "SELECT a." + DbConsts.COL_APPT_DATETIME + " AS ApptDate, " +
          "       ph." + DbConsts.COL_TREAT_TYPE   + " AS TreatmentType, " +
          "       ph." + DbConsts.COL_TREAT_COST   + " AS Cost " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " a " +
          " JOIN " + DbConsts.TABLE_TREATMENTS   + " ph " +
          "   ON ph." + DbConsts.COL_TREAT_APPT_ID + "=a." + DbConsts.COL_APPT_ID + " " +
          "WHERE a." + DbConsts.COL_APPT_ID + " = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apptId);
            DefaultTableModel mdl = new DefaultTableModel(
                new Object[]{"ApptDate", "TreatmentType", "Cost"}, 0
            );
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // format the date to string exactly as your JRXML expects
                    String date = rs.getTimestamp("ApptDate")
                                     .toLocalDateTime()
                                     .toLocalDate()
                                     .toString();
                    mdl.addRow(new Object[]{
                        date,
                        rs.getString("TreatmentType"),
                        rs.getDouble("Cost")
                    });
                }
            }
            return new JRTableModelDataSource(mdl);
        }
    }


    /**
     * Show Jasper for a single appointment’s invoice details.
     */
    public void showAppointmentInvoiceDetail(int apptId)
            throws JRException, SQLException
    {
        // 1) fetch the patient’s name
        String patientName;
        String sqlName =
          "SELECT p.FullName " +
          "FROM " + DbConsts.TABLE_PATIENTS + " p " +
          "  JOIN " + DbConsts.TABLE_APPOINTMENTS + " a " +
          "    ON a." + DbConsts.COL_APPT_PATIENT_ID + "=p." + DbConsts.COL_PATIENT_ID + " " +
          "WHERE a." + DbConsts.COL_APPT_ID + "=?";

        try (PreparedStatement ps = conn.prepareStatement(sqlName)) {
            ps.setInt(1, apptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    patientName = "Unknown Patient";
                } else {
                    patientName = rs.getString("FullName");
                }
            }
        }

        // 2) compile the report
        InputStream jr = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("reports/PatientInvoice.jrxml");
        if (jr == null) {
            throw new IllegalStateException("Cannot find reports/PatientInvoice.jrxml");
        }
        JasperReport report = JasperCompileManager.compileReport(jr);

        // 3) parameters: add ClinicName and PatientName
        Map<String,Object> params = new HashMap<>();
        params.put("ClinicName",  "My DentalCare Clinic");  // or load from config
        params.put("PatientName", patientName);

        // 4) data & view
        JRTableModelDataSource ds = getInvoiceDetailForAppointment(apptId);
        JasperPrint print = JasperFillManager.fillReport(report, params, ds);
        JasperViewer.viewReport(print, false);
    }



    /** Return a list of all patient IDs. */
    public List<Integer> getAllPatientIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT PatientID FROM " + DbConsts.TABLE_PATIENTS;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt("PatientID"));
            }
        }
        return ids;
    }

    
    public Connection getConnection() {
        return conn;
    }
    
        /**
         * Pretend to send an email reminder.
         * Just waits 1 second to simulate work.
         */
        public void sendEmailReminder(int apptId) throws InterruptedException {
            Thread.sleep(1000);
        }

        /**
         * Pretend to send an SMS reminder.
         * Just waits 1 second to simulate work.
         */
        public void sendSmsReminder(int apptId) throws InterruptedException {
            Thread.sleep(1000);
        }
    
    
}
