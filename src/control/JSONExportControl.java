package control;

import entity.Appointment;
import entity.DbConsts;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Exports completed treatments for a month into a JSON file.
 */
public class JSONExportControl {
    private final Connection conn;

    public JSONExportControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /**
     * Export completed treatments for the given year/month to the given file.
     */
    public void exportMonthlyTreatments(int year, int month, File targetFile) throws Exception {
        // Make sure you have this in DbConsts:
        // public static final String COL_APPT_DATE = "AppointmentDate";
        String dtCol = DbConsts.COL_APPT_DATE;

        String sql =
          "SELECT a.AppointmentID, " +
          "       p.FullName    AS PatientName, " +
          "       s.FullName    AS StaffName, " +
          "       a." + dtCol   + " AS Date, " +
          "       t." + DbConsts.COL_TREAT_TYPE + " AS TreatmentType, " +
          "       t." + DbConsts.COL_TREAT_COST + " AS Cost " +
          "FROM "   + DbConsts.TABLE_APPOINTMENTS + " a " +
          " JOIN "  + DbConsts.TABLE_PATIENTS     + " p ON p." + DbConsts.COL_PATIENT_ID     + " = a." + DbConsts.COL_APPT_PATIENT_ID +
          " JOIN "  + DbConsts.TABLE_STAFF        + " s ON s." + DbConsts.COL_STAFF_ID       + " = a." + DbConsts.COL_APPT_STAFF_ID +
          " JOIN "  + DbConsts.TABLE_TREATMENTS   + " t ON t." + DbConsts.COL_TREAT_APPT_ID    + " = a." + DbConsts.COL_APPT_ID +
          " WHERE a." + DbConsts.COL_APPT_STATUS  + " = ? " +
          "   AND Year(a." + dtCol + ") = ? " +
          "   AND Month(a." + dtCol + ") = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_COMPLETED);
            ps.setInt   (2, year);
            ps.setInt   (3, month);

            try (ResultSet rs = ps.executeQuery();
                 OutputStream os = new FileOutputStream(targetFile);
                 JsonGenerator gen = Json.createGenerator(os)) {

                // Build the JSON array of treatment records
                JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
                while (rs.next()) {
                    JsonObjectBuilder obj = Json.createObjectBuilder()
                      .add("appointmentId", rs.getInt("AppointmentID"))
                      .add("patientName",   rs.getString("PatientName"))
                      .add("staffName",     rs.getString("StaffName"))
                      .add("date",          rs.getDate("Date").toString())
                      .add("treatmentType", rs.getString("TreatmentType"))
                      .add("cost",          rs.getDouble("Cost"));
                    arrBuilder.add(obj);
                }
                JsonArray treatments = arrBuilder.build();

                // Write out the final JSON object
                gen.writeStartObject()
                   .write("year",       year)
                   .write("month",      month)
                   .write("treatments", treatments)
                   .writeEnd();
            }
        }
    }
}
