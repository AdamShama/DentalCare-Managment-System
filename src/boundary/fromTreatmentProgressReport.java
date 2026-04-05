package boundary;

import control.ReportController;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class fromTreatmentProgressReport extends JFrame {
    private final JTextField     txtDentistId = new JTextField(10);
    private final JButton        btnGenerate  = new JButton("Generate Report");
    private final ReportController rc;

    /** no-arg constructor stays exactly as before */
    public fromTreatmentProgressReport() {
        super("🦷 Treatment Progress Report");

        // instantiate your controller once
        try {
            this.rc = new ReportController();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to connect to reporting service:\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(e);
        }

        setSize(400, 150);
        setLayout(new BorderLayout(10,10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ─── Top panel ───
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        top.add(new JLabel("Enter Dentist ID:"));
        top.add(txtDentistId);
        top.add(btnGenerate);
        add(top, BorderLayout.CENTER);

        // ─── Button action ───
        btnGenerate.addActionListener(e -> generateReport());
    }

    /** new constructor that takes a dentistId */
    public fromTreatmentProgressReport(int dentistId) {
        this();                               // build the UI
        txtDentistId.setText(String.valueOf(dentistId));
        generateReport();                     // immediately fire it once
        setVisible(true);
    }

    private void generateReport() {
        String raw = txtDentistId.getText().trim();
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a Dentist ID.",
                "Input Required",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final int dentistId;
        try {
            dentistId = Integer.parseInt(raw);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(
                this,
                "Dentist ID must be a number.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            List<String> reportLines = rc.generateTreatmentProgressReport(dentistId);
            if (reportLines.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "No active plans found for dentist " + dentistId,
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                String html = "<html>" + String.join("<br>", reportLines) + "</html>";
                JOptionPane.showMessageDialog(
                    this,
                    html,
                    "Report for " + dentistId,
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error generating report:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new fromTreatmentProgressReport());
    }
}
