// src/boundary/ManagerDashboardFrame.java
package boundary;

import control.JSONExportControl;
import control.ReportController;
import control.XMLimportControl;
import control.XMLimportControl.InventoryXmlException;
import entity.Appointment;
import net.sf.jasperreports.engine.JRException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ManagerDashboardFrame extends JFrame {
    private final int managerStaffId;

    // fields for invoice section
    private JComboBox<Integer> patientIdCombo;
    private JTable             tblAppointments;

    public ManagerDashboardFrame(int managerStaffId) {
        super("🦷 Clinic Manager Dashboard");
        this.managerStaffId = managerStaffId;

        // --- 1) Controllers ---
        ReportController       reportCtrl;
        XMLimportControl       xmlCtrl;
        JSONExportControl      jsonCtrl;
        try {
            reportCtrl = new ReportController();
            xmlCtrl    = new XMLimportControl();
            jsonCtrl   = new JSONExportControl();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Initialization error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // --- 2) Frame & background ---
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GradientBackgroundPanel bg = new GradientBackgroundPanel(
            new Color(40, 0, 80),
            new Color(230, 200, 255)
        );
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        JPanel overlay = new JPanel(new BorderLayout(10,10));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(15,15,15,15));
        bg.add(overlay, BorderLayout.CENTER);

        // --- 3) Header + toolbar ---
        // Welcome banner
        JLabel lblWelcome = new JLabel("Welcome, Clinic Manager", SwingConstants.CENTER);
        lblWelcome.setFont(lblWelcome.getFont().deriveFont(Font.BOLD, 20f));
        lblWelcome.setForeground(Color.WHITE);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        String[] btnTitles = {
            "Manage Staff", "Reports", "Manage Plans", "Reminders",
            "Patients", "Import XML", "Export XML", "Submit JSON",
            "Dentist View", "Logout"
        };

        for (String title : btnTitles) {
            JButton b = new JButton(title);
            b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
            b.setFocusable(false);
            toolbar.add(b);
            toolbar.addSeparator(new Dimension(8,0));

            // wire each button
            switch (title) {
                case "Manage Staff":
                    b.addActionListener(e -> new StaffManagementFrame().setVisible(true));
                    break;
                case "Reports":
                    b.addActionListener(e -> showReportsDialog(reportCtrl));
                    break;
                case "Manage Plans":
                    b.addActionListener(e -> new TreatmentPlanManagementFrame().setVisible(true));
                    break;
                case "Reminders":
                    b.addActionListener(e -> new ReminderDialog(this).setVisible(true));
                    break;
                case "Patients":
                    b.addActionListener(e -> {
                        try {
                            new PatientManagementFrame().setVisible(true);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                "Failed opening Patients:\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    break;
                case "Import XML":
                    b.addActionListener(e -> importInventoryXml(xmlCtrl));
                    break;
                case "Export XML":
                    b.addActionListener(e -> exportInventoryXml(xmlCtrl));
                    break;
                case "Submit JSON":
                    b.addActionListener(e -> submitToMinistry(jsonCtrl));
                    break;
                case "Dentist View":
                    b.addActionListener(e -> new fromDentist(managerStaffId).setVisible(true));
                    break;
                case "Logout":
                    b.addActionListener(e -> {
                        new DashboardPanel().setVisible(true);
                        dispose();
                    });
                    break;
            }
        }

        JPanel topPanel = new JPanel(new BorderLayout(5,5));
        topPanel.setOpaque(false);
        topPanel.add(lblWelcome, BorderLayout.NORTH);
        topPanel.add(toolbar,    BorderLayout.SOUTH);
        overlay.add(topPanel, BorderLayout.NORTH);

        // --- 4) Invoice section as a “card” ---
        JPanel invoiceCard = new JPanel(new BorderLayout(10,10));
        invoiceCard.setOpaque(true);
        invoiceCard.setBackground(new Color(255,255,255,230));
        invoiceCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Generate Invoice"
        ));

        // Patient ID selector
        patientIdCombo = new JComboBox<>();
        try {
            for (Integer id : reportCtrl.getAllPatientIds())
                patientIdCombo.addItem(id);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed loading patients:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        patientIdCombo.addActionListener(e -> refreshInvoiceTable(reportCtrl));

        JPanel invTop = new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));
        invTop.setOpaque(false);
        invTop.add(new JLabel("Patient ID:"));
        invTop.add(patientIdCombo);
        invoiceCard.add(invTop, BorderLayout.NORTH);

        // Appointment table
        tblAppointments = new JTable(new DefaultTableModel(
            new Object[]{"AppointmentID","Date","Cost"}, 0
        ));
        tblAppointments.setFillsViewportHeight(true);
        tblAppointments.setDefaultRenderer(Object.class, new StripingRenderer());
        JScrollPane jsp = new JScrollPane(tblAppointments);
        jsp.setOpaque(false);
        jsp.getViewport().setOpaque(false);
        invoiceCard.add(jsp, BorderLayout.CENTER);

        // Invoice button
        JButton btnInvoice = new JButton("Invoice Selected Appt");
        btnInvoice.setFont(btnInvoice.getFont().deriveFont(Font.BOLD, 14f));
        btnInvoice.addActionListener(e -> {
            int row = tblAppointments.getSelectedRow();
            if (row<0) {
                JOptionPane.showMessageDialog(this,
                    "Select an appointment first.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int apptId = (Integer)tblAppointments.getValueAt(row,0);
            try {
                reportCtrl.showAppointmentInvoiceDetail(apptId);
            } catch (JRException|SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error generating invoice:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel invBottom = new JPanel();
        invBottom.setOpaque(false);
        invBottom.add(btnInvoice);
        invoiceCard.add(invBottom, BorderLayout.SOUTH);

        overlay.add(invoiceCard, BorderLayout.CENTER);

        // initial fill
        refreshInvoiceTable(reportCtrl);

        // --- 5) Finalize ---
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------- helpers ----------------

    private void showReportsDialog(ReportController rc) {
        String[] opts = {"Revenue","Inventory","Cancel"};
        int c = JOptionPane.showOptionDialog(
            this, "Choose report:", "Reports",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, opts, opts[0]
        );
        if (c==0) {
            String ym = JOptionPane.showInputDialog(this,
                "Enter month (YYYY-MM):", "Revenue", JOptionPane.QUESTION_MESSAGE);
            if (ym!=null) {
                try {
                    String[] p = ym.split("-");
                    rc.showMonthlyRevenueJasper(
                        Integer.parseInt(p[0]), Integer.parseInt(p[1])
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                      "Invalid or error:\n"+ex.getMessage(),
                      "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (c==1) {
            JTextField from = new JTextField(), to = new JTextField();
            Object[] prompts = {
                "Start (YYYY-MM-DD):", from,
                "End   (YYYY-MM-DD):", to
            };
            if (JOptionPane.showConfirmDialog(this, prompts,
                    "Inventory Usage", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
                try {
                    rc.showInventoryUsageJasper(
                        LocalDate.parse(from.getText().trim()),
                        LocalDate.parse(to.getText().trim())
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                      "Error:\n"+ex.getMessage(),
                      "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void importInventoryXml(XMLimportControl xmlCtrl) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("XML","xml"));
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            try {
                xmlCtrl.importInventoryXml(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this,
                  "Import successful!", "Imported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                  "Import failed:\n"+ex.getMessage(),
                  "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportInventoryXml(XMLimportControl xmlCtrl) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("XML","xml"));
        fc.setSelectedFile(new File("inventory_export.xml"));
        if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
            try {
                xmlCtrl.exportInventoryXml(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this,
                  "Export successful!", "Exported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                  "Export failed:\n"+ex.getMessage(),
                  "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void submitToMinistry(JSONExportControl jctrl) {
        String[] opts = {"Save to file","Send email","Cancel"};
        int sel = JOptionPane.showOptionDialog(
            this, "Submit JSON:", "Submit",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, opts, opts[0]);
        if (sel<0||sel==2) return;

        String ym = JOptionPane.showInputDialog(this,
            "Enter YYYY-MM to submit:", "Submit", JOptionPane.QUESTION_MESSAGE);
        if (ym==null) return;
        try {
            String[] p = ym.split("-");
            int year = Integer.parseInt(p[0]), month = Integer.parseInt(p[1]);
            String fn = String.format("treatments_%d-%02d.json", year, month);

            if (sel==0) {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File(fn));
                fc.setFileFilter(new FileNameExtensionFilter("JSON","json"));
                if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
                    jctrl.exportMonthlyTreatments(
                        year, month, fc.getSelectedFile()
                    );
                    JOptionPane.showMessageDialog(this,
                      "Saved to " + fc.getSelectedFile(),
                      "Done", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                File tmp = File.createTempFile("treatments_","json");
                jctrl.exportMonthlyTreatments(year, month, tmp);
                String subj = URLEncoder.encode(fn, StandardCharsets.UTF_8);
                String body = URLEncoder.encode("See attached", StandardCharsets.UTF_8);
                Desktop.getDesktop().mail(
                    new URI("mailto:health@min.gov?subject="+subj+"&body="+body)
                );
                JOptionPane.showMessageDialog(this,
                  "Email draft created with " + tmp,
                  "Email", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error submitting:\n"+ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshInvoiceTable(ReportController rc) {
        try {
            // Ask the ReportController directly for the TableModel
            TableModel tm = rc.getCompletedAppointmentsForPatient(
                (Integer)patientIdCombo.getSelectedItem()
            );
            tblAppointments.setModel(tm);

            // (Optional) reapply your striping renderer if you use one:
            TableColumnModel cols = tblAppointments.getColumnModel();
            for (int i = 0; i < cols.getColumnCount(); i++) {
                tblAppointments.getColumnModel()
                              .getColumn(i)
                              .setCellRenderer(new StripingRenderer());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading appointments:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ─── RENDERERS & HELPERS ─────────────────────────

    /** Stripes rows + hover highlight */
    private static class StripingRenderer extends DefaultTableCellRenderer {
        public StripingRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            super.getTableCellRendererComponent(tbl,val,false,false,row,col);
            Color bg = (row%2==0
              ? new Color(240,240,240,180)
              : new Color(255,255,255,180));
            if (tbl.getSelectedRow()==row) bg = new Color(100,150,200,100);
            setBackground(bg);
            return this;
        }
    }

    /** Full‑frame vertical gradient */
    private static class GradientBackgroundPanel extends JPanel {
        private final Color top, bottom;
        public GradientBackgroundPanel(Color top, Color bottom) {
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
