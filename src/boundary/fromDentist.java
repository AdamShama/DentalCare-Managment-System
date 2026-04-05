// src/boundary/fromDentist.java
package boundary;

import control.appointmentControl;
import entity.Appointment;
import entity.Patient;
import net.sf.jasperreports.engine.JRException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class fromDentist extends JFrame {
    private final int dentistId;
    private final appointmentControl ctrl;
    private final DefaultTableModel modelPatients;
    private final DefaultTableModel modelAppts;

    public fromDentist(int dentistId) {
        super("🦷 Dentist Dashboard");
        this.dentistId = dentistId;

        // 1) connect to DB
        try {
            this.ctrl = new appointmentControl();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "DB connection failed:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // 2) frame setup
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 3) use gradient panel as background
        GradientBackgroundPanel bg = new GradientBackgroundPanel(
            new Color(30, 30, 60),   // top color
            new Color(220, 220, 255) // bottom color
        );
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // 4) overlay to hold your cards
        JPanel overlay = new JPanel(new BorderLayout(10,10));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(10,10,10,10));
        bg.add(overlay, BorderLayout.CENTER);

        // 5) toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(255,255,255,200));
        toolbar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnRefresh     = new JButton("↻ Refresh");
        JButton btnProgressRep = new JButton("📈 Progress Report");
        JButton btnBack        = new JButton("🔙 Back");
        for (JButton b : new JButton[]{btnRefresh, btnProgressRep, btnBack}) {
            b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
            b.setFocusable(false);
            toolbar.add(b);
        }
        toolbar.addSeparator(new Dimension(20,0));
        toolbar.add(new JLabel("Show:") {{
            setFont(getFont().deriveFont(Font.PLAIN,14f));
        }});
        JComboBox<String> cbStatus = new JComboBox<>(
            new String[]{ "All", "Scheduled", "Completed", "Active" }
        );
        toolbar.add(cbStatus);

        JPanel tbCard = new JPanel(new BorderLayout());
        tbCard.setOpaque(true);
        tbCard.setBackground(new Color(255,255,255,200));
        tbCard.add(toolbar, BorderLayout.CENTER);
        overlay.add(tbCard, BorderLayout.NORTH);

        // 6) patients table
        modelPatients = new DefaultTableModel(
            new String[]{"Patient ID","Name","Phone","Email","Age","InsuranceID"},0);
        JTable tblP = new JTable(modelPatients);
        JScrollPane spP = new JScrollPane(tblP);

        // 7) appointments table
        modelAppts = new DefaultTableModel(
            new String[]{"Appt ID","Patient","Date","Time","Reason","Status"},0);
        JTable tblA = new JTable(modelAppts);
        JScrollPane spA = new JScrollPane(tblA);

        // wrap in white cards
        JPanel pCard = makeWhiteCard("My Patients", spP);
        JPanel aCard = makeWhiteCard("Appointments", spA);

        // ── transparency ───────────────────────────────────────
        for (JPanel card : new JPanel[]{pCard, aCard}) {
            card.setOpaque(false);
            card.setBackground(new Color(0,0,0,0));
        }
        for (JScrollPane sp : new JScrollPane[]{spP, spA}) {
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
        }
        for (JTable tbl : new JTable[]{tblP, tblA}) {
            tbl.setOpaque(false);
            tbl.setBackground(new Color(0,0,0,0));
            tbl.setShowGrid(true);
            tbl.setGridColor(new Color(200,200,200,120));
            tbl.setRowSelectionAllowed(false);
            tbl.clearSelection();

            // transparent cells
            DefaultTableCellRenderer tr = new DefaultTableCellRenderer();
            tr.setOpaque(false);
            for (int i=0; i<tbl.getColumnCount(); i++) {
                tbl.getColumnModel()
                   .getColumn(i)
                   .setCellRenderer(tr);
            }
            // transparent header
            JTableHeader hdr = tbl.getTableHeader();
            hdr.setOpaque(false);
            hdr.setDefaultRenderer(tr);
        }
        // ───────────────────────────────────────────────────────

        // 8) tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setBackground(new Color(0,0,0,0));
        tabs.addTab("My Patients", pCard);
        tabs.addTab("Appointments", aCard);
        overlay.add(tabs, BorderLayout.CENTER);

        // 9) wire actions
        btnRefresh.addActionListener(e -> {
            loadPatients();
            loadAppointments((String)cbStatus.getSelectedItem());
        });
        btnProgressRep.addActionListener(e -> runReport());
        btnBack.addActionListener(e -> {
            new DashboardPanel().setVisible(true);
            dispose();
        });
        cbStatus.addActionListener(e ->
            loadAppointments((String)cbStatus.getSelectedItem())
        );

        // 10) initial load
        loadPatients();
        loadAppointments("All");

        setVisible(true);
    }

    private void runReport() {
        try {
            ctrl.showTreatmentProgressJasper(dentistId);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not run report:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPatients() {
        modelPatients.setRowCount(0);
        try {
            for (Patient p : ctrl.getPatientsForDentist(dentistId)) {
                modelPatients.addRow(new Object[]{
                    p.getPatientId(),
                    p.getFullName(),
                    p.getPhoneNumber(),
                    p.getEmail(),
                    p.getAge(),
                    p.getInsuranceID()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Could not load patients:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAppointments(String status) {
        modelAppts.setRowCount(0);
        try {
            List<Appointment> list = "All".equals(status)
              ? ctrl.getAllForDentist(dentistId)
              : ctrl.getAppointmentsForDentistByStatus(dentistId, status);
            for (Appointment a : list) {
                modelAppts.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getFULLNAME(),
                    a.getDate(),
                    a.getTime(),
                    a.getVisitReason(),
                    a.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Could not load appointments:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel makeWhiteCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255,255,255,230));
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), title));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── gradient background panel ────────────────────────────────────────
    private static class GradientBackgroundPanel extends JPanel {
        private final Color top, bottom;
        GradientBackgroundPanel(Color top, Color bottom) {
            this.top = top; this.bottom = bottom;
            setOpaque(true);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0,0,top, 0,h,bottom));
            g2.fillRect(0,0,w,h);
        }
    }
}
