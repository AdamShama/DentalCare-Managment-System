package boundary;

import control.ReportController;
import entity.DbConsts;
import entity.Appointment;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Dialog to list upcoming appointments (next 24 hrs) and let the manager
 * send a simulated Email or SMS reminder.
 */
public class ReminderDialog extends JDialog {
    private final JTable table;
    private final DefaultTableModel model;
    private final JRadioButton rbEmail, rbSms;
    private final ReportController rc;

    public ReminderDialog(JFrame owner) {
        super(owner, "Send Appointment Reminders", true);

        // Attempt to build a ReportController (holds your Connection)
        try {
            rc = new ReportController();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(owner,
                "Cannot connect to database:\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        setLayout(new BorderLayout(5,5));

        // 1) Options panel (Email vs SMS)
        JPanel north = new JPanel();
        rbEmail = new JRadioButton("Email", true);
        rbSms   = new JRadioButton("SMS");
        ButtonGroup grp = new ButtonGroup();
        grp.add(rbEmail); grp.add(rbSms);
        north.add(new JLabel("Send via:"));
        north.add(rbEmail);
        north.add(rbSms);
        add(north, BorderLayout.NORTH);

        // 2) Table setup
        model = new DefaultTableModel(new Object[]{
            "AppointmentID","DateTime","Patient","Dentist","Action"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == 4;
            }
        };
        table = new JTable(model);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3) Load data
        loadUpcomingAppointments();

        // 4) Dialog sizing
        setSize(600, 300);
        setLocationRelativeTo(owner);
    }

    private void loadUpcomingAppointments() {
        model.setRowCount(0);
        LocalDateTime now    = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(24);

        String sql =
          "SELECT a." + DbConsts.COL_APPT_ID       + " AS ApptID, " +
          "       a." + DbConsts.COL_APPT_DATETIME + " AS When, " +
          "       p.FullName                      AS Patient, " +
          "       s.FullName                      AS Dentist " +
          "FROM " + DbConsts.TABLE_APPOINTMENTS + " a " +
          " JOIN " + DbConsts.TABLE_PATIENTS     + " p ON p." + DbConsts.COL_PATIENT_ID + "=a." + DbConsts.COL_APPT_PATIENT_ID +
          " JOIN " + DbConsts.TABLE_STAFF        + " s ON s." + DbConsts.COL_STAFF_ID   + "=a." + DbConsts.COL_APPT_STAFF_ID +
          " WHERE a." + DbConsts.COL_APPT_STATUS + "=? " +
          "   AND a." + DbConsts.COL_APPT_DATETIME + " BETWEEN ? AND ? " +
          " ORDER BY a." + DbConsts.COL_APPT_DATETIME;

        try (PreparedStatement ps = rc.getConnection().prepareStatement(sql)) {
            ps.setString(1, Appointment.STATUS_SCHEDULED);
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setTimestamp(3, Timestamp.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ApptID"),
                        rs.getTimestamp("When").toLocalDateTime(),
                        rs.getString("Patient"),
                        rs.getString("Dentist"),
                        "Send"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to load appointments:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Renderer for the Send button
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Editor that simulates sending and animates the button
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private int apptId;
        private boolean isSending;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (isSending) return;
                isSending = true;
                button.setText("Sending...");
                button.setEnabled(false);

                // simulate send in background
                new SwingWorker<Void,Void>() {
                    @Override protected Void doInBackground() throws Exception {
                        if (rbEmail.isSelected()) {
                            rc.sendEmailReminder(apptId);
                        } else {
                            rc.sendSmsReminder(apptId);
                        }
                        return null;
                    }
                    @Override protected void done() {
                        button.setText("Sent!");
                    }
                }.execute();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            apptId = (Integer) table.getValueAt(row, 0);
            button.setText("Send");
            button.setEnabled(true);
            isSending = false;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}
