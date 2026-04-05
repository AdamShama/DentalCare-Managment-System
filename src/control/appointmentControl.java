// src/control/appointmentControl.java
package control;

import entity.DbConsts;
import entity.Patient;
import entity.Appointment;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import boundary.PaymentDialog;

public class appointmentControl {
    private final Connection conn;

    public appointmentControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /** Returns true if this staffID has RoleName = 'Hygienist'. */
    public boolean isHygienist(int staffID) throws SQLException {
        String sql =
          "SELECT COUNT(*)\n" +
          "  FROM " + DbConsts.TABLE_STAFF + " s\n" +
          "  JOIN Roles r ON s.RoleID = r.RoleID\n" +
          " WHERE s.StaffID = ?\n" +
          "   AND r.RoleName = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            ps.setString(2, "Hygienist");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    
    /** Map a single ResultSet row into an Appointment object. */
    private Appointment mapRow(ResultSet rs) throws SQLException {
        // 1) split your single timestamp into date + time
        Timestamp ts = rs.getTimestamp(DbConsts.COL_APPT_DATETIME);
        LocalDate  d  = ts.toLocalDateTime().toLocalDate();
        LocalTime  t  = ts.toLocalDateTime().toLocalTime();

        // 2) read all fields, including the new Refunded flag
        int     id         = rs.getInt   (DbConsts.COL_APPT_ID);
        int     patientId  = rs.getInt   (DbConsts.COL_APPT_PATIENT_ID);
        String  patientName= rs.getString(DbConsts.COL_PATIENT_FULLNAME);
        int     staffId    = rs.getInt   (DbConsts.COL_APPT_STAFF_ID);
        boolean sterilized = rs.getBoolean(DbConsts.COL_APPT_STERILIZED);
        String  reason     = rs.getString(DbConsts.COL_APPT_REASON);
        String  status     = rs.getString(DbConsts.COL_APPT_STATUS);
        boolean paid       = rs.getBoolean(DbConsts.COL_APPT_PAID);
        boolean refunded   = rs.getBoolean(DbConsts.COL_APPT_REFUNDED);

        // 3) pass the extra 'refunded' into your Appointment constructor
        return new Appointment(id, patientId, patientName, staffId,
                d, t, sterilized, reason, status, paid, refunded,
                Appointment.TreatmentType.ROUTINE,
                List.of());

    }


    /** 1) Gets *every* appointment for a hygienist (no filters). */
    public List<Appointment> getAllForHygienist(int hygienistId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + " AS " + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    /** 2) All Scheduled appointments, clinic‑wide (ignores paid). */
    public List<Appointment> getScheduledForHygienist(int hygienistId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + " AS " + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STATUS + " = 'Scheduled'\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    /** 3) All Scheduled & PAID appointments, clinic‑wide. */
    public List<Appointment> getScheduledPaidForHygienist(int hygienistId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + " AS " + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STATUS + " = 'Scheduled'\n" +
          "   AND a." + DbConsts.COL_APPT_PAID   + " = TRUE\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    /** 4) All Scheduled & UNPAID appointments, clinic‑wide. */
    public List<Appointment> getScheduledUnpaidForHygienist(int hygienistId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + " AS " + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STATUS + " = 'Scheduled'\n" +
          "   AND a." + DbConsts.COL_APPT_PAID   + " = FALSE\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    /** Marks an appointment’s Sterilized flag = true. */
    public boolean markSterilized(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_STERILIZED + " = TRUE" +
          " WHERE " + DbConsts.COL_APPT_ID         + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Marks an appointment’s Paid flag = true. */
    public boolean payAppointment(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_PAID + " = TRUE" +
          " WHERE " + DbConsts.COL_APPT_ID   + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Marks an appointment’s Status = “Completed.” */
    public boolean markComplete(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_STATUS + " = ?" +
          " WHERE " + DbConsts.COL_APPT_ID     + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_COMPLETED);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Cancels the appointment by updating its status to 'Cancelled'. */
    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_STATUS + " = ?" +
          " WHERE " + DbConsts.COL_APPT_ID     + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_CANCELLED);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }
    
    public boolean cancelIfAllowed(int apptId) throws SQLException {
        // fetch the scheduled datetime
        String q = "SELECT AppointmentDate FROM Appointments WHERE AppointmentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, apptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                Timestamp schedTs = rs.getTimestamp(1);
                long millisUntil = schedTs.getTime() - System.currentTimeMillis();
                if (millisUntil < 24L * 3600_000) {
                    // less than 24h left—cannot cancel
                    return false;
                }
            }
        }
        // otherwise proceed with cancellation & refund
        cancelAndRefund(apptId);
        return true;
    }
    
    /**
     * Finds all appointments currently in 'Suspended' status,
     * and if they were suspended more than 24h ago, cancels & refunds them.
     */
    public void autoCancelOverdueSuspensions() throws SQLException {
        String sql =
          "SELECT AppointmentID, SuspendedAt " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " " +
          "WHERE Status = ? AND SuspendedAt IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_SUSPENDED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int apptId      = rs.getInt("AppointmentID");
                    Timestamp suspT = rs.getTimestamp("SuspendedAt");
                    long ageMs      = System.currentTimeMillis() - suspT.getTime();
                    if (ageMs >= 24L * 3600_000) {
                        cancelAndRefund(apptId);
                    }
                }
            }
        }
    }

    /**
     * Cancels the appointment (sets Status = 'Cancelled') and
     * flags Paid = FALSE, Refunded = TRUE in one atomic update.
     */
    public void cancelAndRefund(int apptId) throws SQLException {
        String upd =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS + " " +
          "SET Status   = ?, " +
          "    Paid     = FALSE, " +
          "    Refunded = TRUE, " +
          "    SuspendedAt = NULL " +    // clear suspension timestamp
          "WHERE AppointmentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, Appointment.STATUS_CANCELLED);
            ps.setInt   (2, apptId);
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Expected to cancel 1 row, but updated " + rows);
            }
        }
    }


    /** Confirms (resets) an appointment’s Status = Scheduled. */
    public boolean confirmAppointment(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_STATUS + " = ?" +
          " WHERE " + DbConsts.COL_APPT_ID     + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_SCHEDULED);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Fetches all appointments for a given dentist. */
    public List<Appointment> getAllForDentist(int staffID) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STAFF_ID + " = ?\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }

    /** Fetches appointments for a dentist by Status. */
    public List<Appointment> getAppointmentsForDentistByStatus(int staffID, String status)
            throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STAFF_ID + " = ?\n" +
          "   AND a." + DbConsts.COL_APPT_STATUS   + " = ?\n" +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }

    /** Fetches all appointments for a given patient. */
    public List<Appointment> getAllForPatient(int patientId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_PATIENT_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }


    /** Fetches all appointments for a secretary (no filter). */
    public List<Appointment> getAllForSecretary(int staffID) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + " AS PatientName\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Appointment> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    /** Lists distinct patients for a dentist. */
    public List<entity.Patient> getPatientsForDentist(int staffID) throws SQLException {
        String sql =
          "SELECT DISTINCT p.PatientID, p.FullName, p.PhoneNumber, p.Email, p.Age, p.InsuranceID\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a." + DbConsts.COL_APPT_PATIENT_ID + " = p." + DbConsts.COL_PATIENT_ID + "\n" +
          " WHERE a." + DbConsts.COL_APPT_STAFF_ID + " = ?\n" +
          " ORDER BY p.FullName";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                List<entity.Patient> out = new ArrayList<>();
                while (rs.next()) {
                    entity.Patient p = new entity.Patient();
                    p.setPatientId   (rs.getString("PatientID"));
                    p.setFullName    (rs.getString("FullName"));
                    p.setPhoneNumber (rs.getString("PhoneNumber"));
                    p.setEmail       (rs.getString("Email"));
                    p.setAge         (rs.getInt   ("Age"));
                    p.setInsuranceID (rs.getInt   ("InsuranceID"));
                    out.add(p);
                }
                return out;
            }
        }
    }
    
    public void suspendAppointment(int apptId) throws SQLException {
        String sql = "UPDATE Appointments " +
                     "SET Status = ?, SuspendedAt = ? " +
                     "WHERE AppointmentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_SUSPENDED);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, apptId);
            ps.executeUpdate();
        }
    }
    
    public boolean markPaid(int appointmentId) throws SQLException {
        String sql =
          "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
          " SET "   + DbConsts.COL_APPT_PAID + " = TRUE" +
          " WHERE " + DbConsts.COL_APPT_ID   + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        }
    }
    
    
    public Patient getPatient(int patientId) throws SQLException {
        String sql =
          "SELECT p.PatientID, p.FullName, p.PhoneNumber, p.Email, p.Age, " +
          "       i.ProviderName AS InsuranceName " +
          "  FROM Patients p " +
          "  LEFT JOIN InsuranceDetails i " +
          "    ON p.InsuranceID = i.InsuranceID " +
          " WHERE p.PatientID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Patient p = new Patient();
                p.setPatientId   (rs.getString("PatientID"));
                p.setFullName    (rs.getString("FullName"));
                p.setPhoneNumber (rs.getString("PhoneNumber"));
                p.setEmail       (rs.getString("Email"));
                p.setAge         (rs.getInt   ("Age"));
                p.setInsuranceName(rs.getString("InsuranceName"));  // ← now the provider name
                return p;
            }
        }
    }

    /**
     * BOOK an appointment.
     * Defaults to Status='Scheduled', Sterilized=false, Paid=false.
     */
    public boolean bookAppointment(int patientId,
            int staffId,
            LocalDateTime when,
            String visitReason)
            throws SQLException {
// 1) Compute next AppointmentID
    	int nextId;
    	String maxSql = "SELECT MAX(AppointmentID) FROM " + DbConsts.TABLE_APPOINTMENTS;
    	try (Statement st = conn.createStatement();
    			ResultSet rs = st.executeQuery(maxSql)) {
    		rs.next();
    		// if table is empty MAX(...) returns null, rs.getInt(1) gives 0
    		nextId = rs.getInt(1) + 1;
    	}

// 2) Insert new appointment with that ID
    	String insertSql =
    			"INSERT INTO " + DbConsts.TABLE_APPOINTMENTS + " " +
    					"(AppointmentID, PatientID, AppointmentDate, StaffID, VisitReason, Status, Sterilized, Paid) " +
    					"VALUES (?, ?, ?, ?, ?, 'Scheduled', FALSE, FALSE)";
    	try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
    		ps.setInt      (1, nextId);
    		ps.setInt      (2, patientId);
    		ps.setTimestamp(3, Timestamp.valueOf(when));
    		ps.setInt      (4, staffId);
    		ps.setString   (5, visitReason);
    		return ps.executeUpdate() == 1;
    	}
}

    
    
    
    /**
     * RESCHEDULE an existing appointment back to Scheduled.
     */
    public boolean rescheduleAppointment(int appointmentId,
                                         LocalDateTime newDateTime)
                                         throws SQLException {
      String sql = "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
                   " SET AppointmentDate = ?, Status = 'Scheduled'" +
                   " WHERE AppointmentID = ?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setTimestamp(1, Timestamp.valueOf(newDateTime));
        ps.setInt(2, appointmentId);
        return ps.executeUpdate() == 1;
      }
    }
    
    
    public List<Appointment> getPastForPatient(int patientId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a.PatientID = p.PatientID\n" +
          " WHERE a.PatientID = ?\n" +
          "   AND a.AppointmentDate < ?\n" +
          " ORDER BY a.AppointmentDate DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }
    
    public List<Appointment> getUpcomingForPatient(int patientId) throws SQLException {
        String sql =
          "SELECT a.*, p." + DbConsts.COL_PATIENT_FULLNAME + "\n" +
          "  FROM " + DbConsts.TABLE_APPOINTMENTS + " a\n" +
          "  JOIN " + DbConsts.TABLE_PATIENTS     + " p  ON a.PatientID = p.PatientID\n" +
          " WHERE a.PatientID = ?\n" +
          "   AND a.AppointmentDate >= ?\n" +
          " ORDER BY a.AppointmentDate";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> out = new ArrayList<>();
                while (rs.next()) out.add(mapRow(rs));
                return out;
            }
        }
    }
    
    public LocalDateTime findNextAvailableSlot() throws SQLException {
        // 1) list all booked times after now
    	// REPLACEMENT
    	String sql =
    	  "SELECT "  + DbConsts.COL_APPT_DATETIME +
    	  " FROM "    + DbConsts.TABLE_APPOINTMENTS +
    	  " WHERE "   + DbConsts.COL_APPT_DATETIME + " >= ?" +
    	  " ORDER BY "+ DbConsts.COL_APPT_DATETIME;

        List<LocalDateTime> booked = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    booked.add(rs.getTimestamp(1).toLocalDateTime());
                }
            }
        }

        // 2) starting at now, in 30‑minute increments, pick the first that’s not in the list
        LocalDateTime slot = LocalDateTime.now().withSecond(0).withNano(0);
        while (true) {
            if (!booked.contains(slot)) {
                return slot;
            }
            slot = slot.plusMinutes(30);
            // optionally add a cutoff here (e.g. only search next 7 days)
        }
    }
    
    
    /** Run the Jasper “treatment_progress” report. */
    public void showTreatmentProgressJasper(int dentistId) throws JRException {
        InputStream jrXml = getClass()
          .getResourceAsStream("/reports/treatment_progress.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(jrXml);
        Map<String,Object> params = new HashMap<>();
        params.put("DentistParam", dentistId);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
        JasperViewer.viewReport(jasperPrint, false);
    }
}
