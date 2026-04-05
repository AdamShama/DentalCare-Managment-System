// src/control/patientControl.java
package control;

import entity.DbConsts;
import entity.MedicalHistory;
import entity.Patient;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class patientControl {
    private final Connection conn;

    public patientControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /** Fetch all patients into a Swing TableModel */
    public DefaultTableModel getAllPatientsModel() throws SQLException {
        String[] cols = {
          "PatientID", "FullName", "PhoneNumber",
          "Email",     "Age",      "InsuranceID", "DentistID"
        };
        DefaultTableModel mdl = new DefaultTableModel(cols, 0);

        String sql = "SELECT PatientID, FullName, PhoneNumber, Email, Age, InsuranceID, dentistID "
                   + "FROM " + DbConsts.TABLE_PATIENTS;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                mdl.addRow(new Object[]{
                  rs.getInt("PatientID"),
                  rs.getString("FullName"),
                  rs.getString("PhoneNumber"),
                  rs.getString("Email"),
                  rs.getInt("Age"),
                  rs.getInt("InsuranceID"),
                  rs.getInt("dentistID")
                });
            }
        }
        return mdl;
    }

    /** Returns a real List<Patient> so you can foreach it. */
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT PatientID, FullName, PhoneNumber, Email, Age "
                   + "FROM " + DbConsts.TABLE_PATIENTS;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient();  // uses your no-arg ctor + setters
                p.setPatientId(  rs.getString("PatientID"));
                p.setFullName(   rs.getString("FullName"));
                p.setPhoneNumber(rs.getString("PhoneNumber"));
                p.setEmail(      rs.getString("Email"));
                p.setAge(        rs.getInt("Age"));
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Add a new patient.
     * You provide name, phone, email, age & dentistId.
     * InsuranceID is picked automatically as MAX+1.
     */
    public void addPatient(String name,
                           String phoneNumber,
                           String email,
                           int age,
                           int dentistId) throws SQLException {
        // 1) Pick the next InsuranceID
        String maxInsSql = "SELECT NZ(MAX(" + DbConsts.COL_INSURANCE_ID + "),0) FROM "
                         + DbConsts.TABLE_INSURANCE;
        int nextInsId;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(maxInsSql)) {
            rs.next();
            nextInsId = rs.getInt(1) + 1;
        }

        // 2) Insert a blank InsuranceDetails row
        String insInsert =
            "INSERT INTO " + DbConsts.TABLE_INSURANCE +
            " (" + DbConsts.COL_INSURANCE_ID + ",ProviderName,PolicyNumber)" +
            " VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(insInsert)) {
            ps.setInt   (1, nextInsId);
            ps.setString(2, "");    // default provider name
            ps.setString(3, "");    // default policy #
            ps.executeUpdate();
        }

        // 3) Insert the patient, referencing that new InsuranceID
        String patInsert =
            "INSERT INTO " + DbConsts.TABLE_PATIENTS +
            " (FullName,PhoneNumber,Email,Age,InsuranceID,dentistID) " +
            "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(patInsert)) {
            ps.setString(1, name);
            ps.setString(2, phoneNumber);
            ps.setString(3, email);
            ps.setInt   (4, age);
            ps.setInt   (5, nextInsId);
            ps.setInt   (6, dentistId);
            ps.executeUpdate();
        }
    }

    /** Update an existing patient record */
    public void updatePatient(int id,
                              String name,
                              String phoneNumber,
                              String email,
                              int age,
                              int dentistId) throws SQLException {
        String sql =
            "UPDATE " + DbConsts.TABLE_PATIENTS +
            " SET FullName=?, PhoneNumber=?, Email=?, Age=?, dentistID=? " +
            "WHERE PatientID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phoneNumber);
            ps.setString(3, email);
            ps.setInt   (4, age);
            ps.setInt   (5, dentistId);
            ps.setInt   (6, id);
            ps.executeUpdate();
        }
    }

    /** Delete a patient by ID */
    public void deletePatient(int id) throws SQLException {
        String sql = "DELETE FROM " + DbConsts.TABLE_PATIENTS + " WHERE PatientID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Fetch the medical‐history row for a given patient.
     */
    public MedicalHistory getMedicalHistoryForPatient(int patientId) throws SQLException {
        String sql = "SELECT Allergies, PreExistingCond "
                   + "FROM MedicalHistory WHERE PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String allCsv  = rs.getString("Allergies");
                    String condCsv = rs.getString("PreExistingCond");
                    List<String> allergies = (allCsv == null || allCsv.isBlank())
                        ? Collections.emptyList()
                        : Arrays.asList(allCsv.split("\\s*,\\s*"));
                    List<String> preconds = (condCsv == null || condCsv.isBlank())
                        ? Collections.emptyList()
                        : Arrays.asList(condCsv.split("\\s*,\\s*"));
                    return new MedicalHistory(allergies, preconds);
                }
            }
        }
        // no history row → return empty lists
        return new MedicalHistory(
            Collections.emptyList(),
            Collections.emptyList()
        );
    }
    
    
    public Patient getPatientById(int patientId) throws SQLException {
        String sql =
            "SELECT p.PatientID, p.FullName, p.PhoneNumber, p.Email, p.Age, " +
            "       p.InsuranceID, p.DentistID, i.ProviderName " +
            "FROM " + DbConsts.TABLE_PATIENTS + " p " +
            "LEFT JOIN " + DbConsts.TABLE_INSURANCE + " i " +
            "  ON p." + DbConsts.COL_INSURANCE_ID + " = i." + DbConsts.COL_INSURANCE_ID + " " +
            "WHERE p.PatientID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient p = new Patient();
                    // PatientID is stored as String in your entity:
                    p.setPatientId    ( rs.getString("PatientID") );
                    p.setFullName     ( rs.getString("FullName") );
                    p.setPhoneNumber  ( rs.getString("PhoneNumber") );
                    p.setEmail        ( rs.getString("Email") );
                    p.setAge          ( rs.getInt   ("Age") );
                    p.setInsuranceID  ( rs.getInt   ("InsuranceID") );
                    p.setDentistID    ( rs.getInt   ("DentistID") );
                    // this matches your setter:
                    p.setInsuranceName( rs.getString("ProviderName") );
                    return p;
                }
                return null;
            }
        }
    }

}
