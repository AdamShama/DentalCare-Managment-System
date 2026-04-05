// src/boundary/fromSecretary.java
package boundary;

import control.XMLimportControl;
import control.appointmentControl;
import control.inventoryItemControl;
import entity.Appointment;
import entity.InventoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class fromSecretary extends JFrame {
    private final int secretaryId;
    private final appointmentControl apptCtrl;
    private final XMLimportControl  xmlCtrl;
    private final inventoryItemControl invCtrl;

    private DefaultTableModel apptModel;
    private JTable            apptTable;
    private DefaultTableModel invModel;
    private JTable            invTable;

    public fromSecretary(int secretaryId) {
        super("🗂️ Secretary Dashboard");
        this.secretaryId = secretaryId;

        // --- 1) Controls ---
        try {
            apptCtrl = new appointmentControl();
            xmlCtrl  = new XMLimportControl();
            invCtrl  = new inventoryItemControl();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during initialization:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // --- 2) Frame setup ---
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // --- 3) Gradient background + overlay ---
        GradientBackgroundPanel bg = new GradientBackgroundPanel(
            new Color(60, 20, 80),
            new Color(240, 230, 255)
        );
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        JPanel overlay = new JPanel(new BorderLayout(10,10));
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(10,10,10,10));
        bg.add(overlay, BorderLayout.CENTER);

        // --- 4) Toolbar ---
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        tool.setBackground(new Color(255,255,255,200));
        tool.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JButton btnRefresh = new JButton("↻ Refresh All");
        JButton btnImport  = new JButton("📁 Import XML…");
        JButton btnBack    = new JButton("← Back to Dashboard");

        for (JButton b : new JButton[]{btnRefresh, btnImport, btnBack}) {
            b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
            b.setFocusable(false);
            tool.add(b);
            tool.addSeparator(new Dimension(10,0));
        }
        // remove last separator
        tool.remove(tool.getComponentCount()-1);

        overlay.add(tool, BorderLayout.NORTH);

        // --- 5) Tabs ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setBackground(new Color(0,0,0,0));

        // Appointments tab
        apptModel = createAppointmentModel();
        apptTable = new JTable(apptModel);
        styleTable(apptTable);
        setupAppointmentButtons();
        JPanel apptCard = makeCard("Appointments", new JScrollPane(apptTable));
        tabs.addTab("Appointments", apptCard);

        // Inventory tab
        invModel = createInventoryModel();
        invTable = new JTable(invModel);
        styleTable(invTable);
        JPanel invCard = makeCard("Inventory", new JScrollPane(invTable));
        tabs.addTab("Inventory", invCard);

        overlay.add(tabs, BorderLayout.CENTER);

        // --- 6) Wire buttons ---
        btnBack.addActionListener(e -> {
            new DashboardPanel().setVisible(true);
            dispose();
        });
        btnRefresh.addActionListener(e -> {
            loadAppointments();
            loadInventory();
        });
        btnImport.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("XML files","xml"));
            if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    xmlCtrl.importInventoryXml(f);
                    JOptionPane.showMessageDialog(
                      this, "Import successful", "Import", JOptionPane.INFORMATION_MESSAGE
                    );
                    loadInventory();
                } catch (XMLimportControl.InventoryXmlException ix) {
                    JOptionPane.showMessageDialog(
                      this, "Error importing XML:\n"+ix.getMessage(),
                      "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // --- 7) Initial load & show ---
        loadAppointments();
        loadInventory();
        setVisible(true);
    }

    private DefaultTableModel createAppointmentModel() {
        String[] cols = {
          "Appt ID","Patient","StaffID","Date","Time",
          "Sterilized","Status","Confirm","Cancel"
        };
        return new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){
                return c==7||c==8;
            }
            @Override public Class<?> getColumnClass(int c){
                if (c==0||c==2) return Integer.class;
                if (c==5) return Boolean.class;
                return String.class;
            }
        };
    }

    private DefaultTableModel createInventoryModel() {
        String[] cols = {
          "Serial#","Name","Category","Qty",
          "Expiry","ReorderThreshold","SupplierID"
        };
        return new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Class<?> getColumnClass(int c){
                if (c==0||c==3||c==5) return Integer.class;
                return String.class;
            }
        };
    }

    private void setupAppointmentButtons() {
        apptTable.getColumn("Confirm")
                 .setCellRenderer(new ButtonRenderer("Confirm"));
        apptTable.getColumn("Confirm")
                 .setCellEditor(new ConfirmEditor());

        apptTable.getColumn("Cancel")
                 .setCellRenderer(new ButtonRenderer("Cancel"));
        apptTable.getColumn("Cancel")
                 .setCellEditor(new CancelEditor());
    }

    private JPanel makeCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255,255,255,210));
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), title
        ));
        content.setOpaque(false);
        if (content instanceof JScrollPane) {
            ((JScrollPane)content).getViewport().setOpaque(false);
        }
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void loadAppointments() {
        apptModel.setRowCount(0);
        try {
            List<Appointment> list = apptCtrl.getAllForSecretary(secretaryId);
            for (Appointment a : list) {
                apptModel.addRow(new Object[]{
                    a.getAppointmentID(),
                    a.getFULLNAME(),
                    a.getStaffID(),
                    a.getDate(),
                    a.getTime(),
                    a.isSterilized(),
                    a.getStatus(),
                    "Confirm","Cancel"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
              "Error loading appointments:\n"+ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventory() {
        invModel.setRowCount(0);
        try {
            for (InventoryItem it : invCtrl.getAll()) {
                invModel.addRow(new Object[]{
                    it.getItemID(),
                    it.getItemName(),
                    it.getDescription(),
                    it.getQuantityInStock(),
                    it.getExpirationDate(),
                    it.getReorderThreshold(),
                    it.getSupplier().getSupplierID()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
              "Error loading inventory:\n"+ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Applies striping, hover highlight, and transparent background */
    private void styleTable(JTable tbl) {
        tbl.setRowHeight(28);
        tbl.setFillsViewportHeight(true);
        tbl.setOpaque(false);
        ((JComponent)tbl.getDefaultRenderer(Object.class))
            .setOpaque(false);

        tbl.setDefaultRenderer(Object.class,
            new StripingRenderer());
        tbl.setDefaultRenderer(Boolean.class,
            new BooleanRenderer());
    }

    // ─── RENDERERS & EDITORS ────────────────────────────────────

    private class StripingRenderer extends DefaultTableCellRenderer {
        public StripingRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            super.getTableCellRendererComponent(tbl,val,false,false,row,col);
            Color bg = row%2==0
              ? new Color(240,240,240,180)
              : new Color(255,255,255,180);
            if (tbl.getSelectedRow()==row) bg = new Color(100,150,200,100);
            setBackground(bg);
            return this;
        }
    }

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
            setSelected(val!=null && (Boolean)val);
            return this;
        }
    }

    private static class ButtonRenderer extends JButton
            implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setOpaque(true);
        }
        @Override public Component getTableCellRendererComponent(
            JTable tbl, Object val,
            boolean sel, boolean foc,
            int row, int col)
        {
            setText(val==null?"":val.toString());
            setEnabled(!getText().isBlank());
            return this;
        }
    }

    private class ConfirmEditor extends DefaultCellEditor {
        private final JButton btn = new JButton("Confirm");
        private boolean isPushed;
        public ConfirmEditor() {
            super(new JCheckBox());
            btn.setOpaque(true);
            btn.addActionListener(e->fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(
            JTable tbl, Object val,
            boolean sel, int row, int col)
        {
            isPushed = true; return btn;
        }
        @Override public Object getCellEditorValue() {
            if (isPushed) {
                int r = apptTable.getSelectedRow();
                int id = (Integer)apptModel.getValueAt(r,0);
                try { apptCtrl.confirmAppointment(id); }
                catch(SQLException ex){
                  JOptionPane.showMessageDialog(fromSecretary.this,
                    "Error confirming:\n"+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                }
                loadAppointments();
            }
            isPushed = false;
            return "Confirm";
        }
    }

    private class CancelEditor extends DefaultCellEditor {
        private final JButton btn = new JButton("Cancel");
        private boolean isPushed;
        public CancelEditor() {
            super(new JCheckBox());
            btn.setOpaque(true);
            btn.addActionListener(e->fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(
            JTable tbl, Object val,
            boolean sel, int row, int col)
        {
            isPushed = true; return btn;
        }
        @Override public Object getCellEditorValue() {
            if (isPushed) {
                int r = apptTable.getSelectedRow();
                int id = (Integer)apptModel.getValueAt(r,0);
                try { apptCtrl.cancelAppointment(id); }
                catch(SQLException ex){
                  JOptionPane.showMessageDialog(fromSecretary.this,
                    "Error canceling:\n"+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                }
                loadAppointments();
            }
            isPushed = false;
            return "Cancel";
        }
    }

    // ─── GRADIENT BACKGROUND PANEL ──────────────────────────────
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
