// src/boundary/PatientProfileFrame.java
package boundary;

import control.patientControl;
import entity.MedicalHistory;
import entity.Patient;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Collections;

public class PatientProfileFrame extends JFrame {
    private final int patientId;
    private final patientControl pc;

    public PatientProfileFrame(int patientId) {
        super("👤 Patient Details");
        this.patientId = patientId;

        // 1) Controller
        try {
            pc = new patientControl();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to database:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        // 2) Fetch patient record
        Patient p;
        try {
            p = pc.getPatientById(patientId);
            if (p == null) {
                JOptionPane.showMessageDialog(this,
                    "No such patient: " + patientId,
                    "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error fetching patient:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // 3) Fetch medical history
        MedicalHistory mh;
        try {
            mh = pc.getMedicalHistoryForPatient(patientId);
        } catch (SQLException ex) {
            // on error, show empty history
            mh = new MedicalHistory(Collections.emptyList(), Collections.emptyList());
        }

        // 4) Build UI
        JPanel info = new JPanel(new GridLayout(0, 2, 5, 5));
        info.setBorder(BorderFactory.createTitledBorder("Patient Info"));

        info.add(new JLabel("Patient ID:"));    info.add(new JLabel(String.valueOf(p.getPatientId())));
        info.add(new JLabel("Name:"));          info.add(new JLabel(p.getFullName()));
        info.add(new JLabel("Phone:"));         info.add(new JLabel(p.getPhoneNumber()));
        info.add(new JLabel("Email:"));         info.add(new JLabel(p.getEmail()));
        info.add(new JLabel("Age:"));           info.add(new JLabel(String.valueOf(p.getAge())));
        info.add(new JLabel("Insurance:"));     info.add(new JLabel(p.getInsuranceName()));

        // Medical history fields
        info.add(new JLabel("Allergies:"));
        info.add(new JLabel(mh.getAllergies().isEmpty() ? "None" : String.join(", ", mh.getAllergies())));
        info.add(new JLabel("Pre‑Existing Conditions:"));
        info.add(new JLabel(mh.getPreExistingConditions().isEmpty() ? "None" : String.join(", ", mh.getPreExistingConditions())));

        // 5) Layout & display
        setLayout(new BorderLayout(10, 10));
        add(info, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}
