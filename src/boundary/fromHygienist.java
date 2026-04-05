// src/boundary/fromHygienist.java
package boundary;

import control.appointmentControl;
import entity.Appointment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class fromHygienist extends JFrame {
    private final int hygienistId;
    private final appointmentControl ctrl;

    // no longer final so early return is safe
    private DefaultTableModel model;
    private JTable            table;
    private JRadioButton      rbPaid, rbUnpaid;

    public fromHygienist(int hygienistId) {
        super("🧼 Hygienist Dashboard");
        this.hygienistId = hygienistId;

        // 1) connect & role‑check
        try {
            this.ctrl = new appointmentControl();
            if (!ctrl.isHygienist(hygienistId)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Access denied: you are not a Hygienist.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                new DashboardPanel().setVisible(true);
                dispose();
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "DB connection failed:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(ex);
        }

        // 2) frame setup
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 3) gradient background
        GradientBackgroundPanel bg = new GradientBackgroundPanel(
            new Color(20, 80, 100),    // top color
            new Color(200, 230, 240)   // bottom color
        );
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // 4) transparent overlay
        JPanel overlay = new JPanel(new BorderLayout(10,10));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(10,10,10,10));
        bg.add(overlay, BorderLayout.CENTER);

        // 5) toolbar
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        tool.setBackground(new Color(255,255,255,200));
        tool.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));

        JButton btnRefresh = new JButton("↻ Refresh");
        JButton btnBack    = new JButton("← Back");
        for (JButton b : new JButton[]{btnRefresh, btnBack}) {
            b.setFont(b.getFont().deriveFont(Font.BOLD,14f));
            b.setFocusable(false);
            tool.add(b);
        }

        tool.addSeparator(new Dimension(20,0));

        // paid/unpaid toggles
        ButtonGroup grp = new ButtonGroup();
        rbPaid   = new JRadioButton("Show paid", true);
        rbUnpaid = new JRadioButton("Show unpaid");
        for (JRadioButton rb : new JRadioButton[]{rbPaid, rbUnpaid}) {
            rb.setOpaque(false);
            tool.add(rb);
            grp.add(rb);
        }

        // spacer + back again on right (optional)
        tool.add(Box.createHorizontalGlue());
        tool.add(btnBack);

        overlay.add(tool, BorderLayout.NORTH);

        // 6) table & model
        String[] cols = {
            "Appt ID","Patient","Date","Time",
            "Reason","Status","Sterilized","Action"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == 7;  // only Action column
            }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0) return Integer.class;
                if (col == 6) return Boolean.class;
                return String.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);

        // striping + hover
        table.setDefaultRenderer(Object.class, new StripingRenderer());
        // checkbox renderer
        table.setDefaultRenderer(Boolean.class, new BooleanRenderer());
        // button renderer & editor
        table.getColumn("Action")
             .setCellRenderer(new ButtonRenderer("Sterilize"));
        table.getColumn("Action")
             .setCellEditor(new ButtonEditor("Sterilize"));

        JScrollPane jsp = new JScrollPane(table);
        jsp.setOpaque(false);
        jsp.getViewport().setOpaque(false);

        // 7) wrap in card
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255,255,255,210));
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Scheduled Appointments"
        ));
        card.add(jsp, BorderLayout.CENTER);
        overlay.add(card, BorderLayout.CENTER);

        // 8) actions
        btnRefresh.addActionListener(e -> loadAppointments());
        btnBack    .addActionListener(e -> {
            new DashboardPanel().setVisible(true);
            dispose();
        });
        rbPaid    .addActionListener(e -> loadAppointments());
        rbUnpaid  .addActionListener(e -> loadAppointments());

        // 9) initial load
        loadAppointments();
        setVisible(true);
    }

    private void loadAppointments() {
        model.setRowCount(0);
        try {
            List<Appointment> list = rbUnpaid.isSelected()
                ? ctrl.getScheduledUnpaidForHygienist(hygienistId)
                : ctrl.getScheduledPaidForHygienist(hygienistId);

            for (Appointment a : list) {
                String action = (!a.isSterilized() && a.isPaid())
                                ? "Sterilize" : "";
                model.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getFULLNAME(),
                    a.getDate(),
                    a.getTime(),
                    a.getVisitReason(),
                    a.getStatus(),
                    a.isSterilized(),
                    action
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading appointments:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==== RENDERERS & EDITORS ====

    /** Stripes rows + hover highlight */
    private class StripingRenderer extends DefaultTableCellRenderer {
        public StripingRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            super.getTableCellRendererComponent(tbl, val, false, false, row, col);
            Color bg = (row % 2 == 0
                ? new Color(240,240,240,180)
                : new Color(255,255,255,180));
            if (tbl.getSelectedRow() == row) bg = new Color(100,150,200,100);
            setBackground(bg);
            return this;
        }
    }

    /** Centered transparent checkbox */
    private class BooleanRenderer extends JCheckBox implements TableCellRenderer {
        public BooleanRenderer() {
            setHorizontalAlignment(CENTER);
            setOpaque(false);
        }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            setSelected(val != null && (Boolean) val);
            return this;
        }
    }

    /** Button renderer */
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setOpaque(true);
        }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            setText(val == null ? "" : val.toString());
            setEnabled(!getText().isBlank());
            return this;
        }
    }

    /** Button editor calls sterilize */
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton();
        private String label;
        public ButtonEditor(String text) {
            super(new JCheckBox());
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column
        ) {
            label = (value == null ? "" : value.toString());
            button.setText(label);
            button.setEnabled(!label.isBlank());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (!label.isBlank()) {
                int row    = table.getSelectedRow();
                int apptId = (Integer)model.getValueAt(row, 0);
                try {
                    ctrl.markSterilized(apptId);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        fromHygienist.this,
                        "Error sterilizing:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
                loadAppointments();
            }
            return label;
        }
    }

    // ==== GRADIENT BACKGROUND PANEL ====
    private static class GradientBackgroundPanel extends JPanel {
        private final Color top, bottom;
        public GradientBackgroundPanel(Color top, Color bottom) {
            this.top = top; this.bottom = bottom;
            setOpaque(true);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0,0,top, 0,h,bottom));
            g2.fillRect(0,0,w,h);
        }
    }
}
