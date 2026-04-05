// src/boundary/fromPatient.java
package boundary;

import control.appointmentControl;
import entity.Appointment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class fromPatient extends JFrame {
    private final int patientId;
    private final appointmentControl ctrl;

    private final DefaultTableModel dashModel;
    private final JTable          dashTable;

    public fromPatient(int patientId) {
        super("🙂 Patient Dashboard");
        this.patientId = patientId;

        // 1) connect
        try {
            this.ctrl = new appointmentControl();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "DB connection error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // 2) setup gradient background + overlay panel
        GradientBackgroundPanel bg = new GradientBackgroundPanel();
        bg.setLayout(new BorderLayout(10,10));
        setContentPane(bg);

        JPanel overlay = new JPanel(new BorderLayout(15,15));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(20,20,20,20));
        bg.add(overlay, BorderLayout.CENTER);

        // 3) toolbar card
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);

        JButton btnRefresh = new JButton("↻ Refresh");
        JButton btnBook    = new JButton("📅 Book New");
        JButton btnProfile = new JButton("👤 Profile");
        JButton btnBack    = new JButton("🔙 Back");

        for (JButton b : new JButton[]{ btnRefresh, btnBook, btnProfile, btnBack }) {
            b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
            b.setFocusPainted(false);
            toolbar.add(b);
            toolbar.addSeparator(new Dimension(10,0));
        }

        JPanel tbCard = makeWhiteCard(null, toolbar);
        overlay.add(tbCard, BorderLayout.NORTH);

        // 4) dashboard table card
        String[] dashCols = {
            "Appointment ID", "Date", "Time", "Reason", "Status",
            "Paid", "Refunded", "Pay", "Suspend"
        };
        dashModel = new DefaultTableModel(dashCols, 0) {
            @Override public boolean isCellEditable(int r,int c) {
                return c == 7 || c == 8;
            }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                if (c == 5 || c == 6) return Boolean.class;
                return String.class;
            }
        };
        dashTable = new JTable(dashModel);
        dashTable.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(dashTable);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        // wire Pay button (col 7)
        dashTable.getColumnModel().getColumn(7)
                 .setCellRenderer(new ButtonRenderer());
        dashTable.getColumnModel().getColumn(7)
                 .setCellEditor(new PayEditor(new JCheckBox()));

        // wire Suspend button (col 8)
        dashTable.getColumnModel().getColumn(8)
                 .setCellRenderer(new ButtonRenderer());
        dashTable.getColumnModel().getColumn(8)
                 .setCellEditor(new SuspendEditor(new JCheckBox()));

        JPanel tableCard = makeWhiteCard("My Appointments", sp);
        overlay.add(tableCard, BorderLayout.CENTER);

        // 5) button actions
        btnRefresh.addActionListener(e -> loadDashboard());
        btnBook   .addActionListener(e -> doBooking());
        btnProfile.addActionListener(e ->
            new PatientProfileFrame(patientId).setVisible(true)
        );
        btnBack   .addActionListener(e -> {
            new DashboardPanel().setVisible(true);
            dispose();
        });

        // 6) initial load & frame settings
        loadDashboard();
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void loadDashboard() {
        // auto‑cancel old suspensions
        try {
            ctrl.autoCancelOverdueSuspensions();
        } catch (SQLException ex) {
            System.err.println("Auto‑cancel failed: " + ex.getMessage());
        }

        dashModel.setRowCount(0);
        try {
            for (Appointment a : ctrl.getAllForPatient(patientId)) {
                dashModel.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getDate(),
                    a.getTime(),
                    a.getVisitReason(),
                    a.getStatus(),
                    a.isPaid(),
                    a.isRefunded(),
                    a.isPaid() ? "" : "Pay",
                    "Suspend"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading appointments:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doBooking() {
        try {
            int resp = JOptionPane.showConfirmDialog(
                this,
                "Urgent appointment?\nYes=next free slot, No=choose date/time",
                "Book Appointment",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (resp != JOptionPane.YES_OPTION && resp != JOptionPane.NO_OPTION) return;

            LocalDateTime when;
            if (resp == JOptionPane.YES_OPTION) {
                when = ctrl.findNextAvailableSlot();
                if (when == null) {
                    JOptionPane.showMessageDialog(
                        this, "No slots available right now.",
                        "Scheduling Error", JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } else {
                String dtStr = JOptionPane.showInputDialog(
                    this,
                    "Enter Date & Time (yyyy-MM-dd HH:mm):"
                );
                if (dtStr == null) return;
                when = LocalDateTime.parse(
                    dtStr.trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
            }

            String reason = JOptionPane.showInputDialog(
                this, "Reason for visit:"
            );
            if (reason == null) return;

            boolean ok = ctrl.bookAppointment(patientId, 1, when, reason);
            if (!ok) {
                JOptionPane.showMessageDialog(
                    this, "Failed to book the appointment.",
                    "Booking Error", JOptionPane.ERROR_MESSAGE
                );
            } else {
                loadDashboard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // small utility: wrap a component in a white card with optional title
    private JPanel makeWhiteCard(String title, JComponent comp) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        if (title != null) {
            card.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title
            ));
        } else {
            card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        card.add(comp, BorderLayout.CENTER);
        return card;
    }

    // paints a subtle vertical gradient
    private static class GradientBackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(30, 30, 60),
                0, h, new Color(60, 60, 100)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    // renders our “Pay”/“Suspend” buttons
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val, boolean sel, boolean foc, int row, int col
        ) {
            setText(val == null ? "" : val.toString());
            setEnabled(val != null && !val.toString().isEmpty());
            return this;
        }
    }

    private abstract class BaseEditor extends DefaultCellEditor {
        protected final JButton button = new JButton();
        protected boolean isPushed;
        public BaseEditor(JCheckBox chk) {
            super(chk);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
    }

    private class PayEditor extends BaseEditor {
        public PayEditor(JCheckBox chk) { super(chk); }
        @Override public Component getTableCellEditorComponent(
            JTable tbl, Object val, boolean sel, int row, int col
        ) {
            button.setText("Pay");
            button.setEnabled(val != null && "Pay".equals(val.toString()));
            isPushed = true;
            return button;
        }
        @Override public Object getCellEditorValue() {
            if (isPushed) {
                int r = dashTable.getSelectedRow();
                int apptId = (Integer) dashModel.getValueAt(r, 0);
                boolean paid = PaymentDialog.showPayment(fromPatient.this, apptId, ctrl);
                if (paid) {
                    try { ctrl.payAppointment(apptId); }
                    catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                            fromPatient.this,
                            "Error marking paid:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
                loadDashboard();
            }
            isPushed = false;
            return "Pay";
        }
    }

    private class SuspendEditor extends BaseEditor {
        public SuspendEditor(JCheckBox chk) { super(chk); }
        @Override public Component getTableCellEditorComponent(
            JTable tbl, Object val, boolean sel, int row, int col
        ) {
            button.setText("Suspend");
            isPushed = true;
            return button;
        }
        @Override public Object getCellEditorValue() {
            if (isPushed) {
                int r = dashTable.getSelectedRow();
                int apptId = (Integer) dashModel.getValueAt(r, 0);
                try { ctrl.suspendAppointment(apptId); }
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        fromPatient.this,
                        "Error suspending:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
                loadDashboard();
            }
            isPushed = false;
            return "Suspend";
        }
    }
}
