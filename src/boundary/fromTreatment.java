// boundary/fromTreatment.java
package boundary;

import control.treatmentControl;
import entity.Treatment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class fromTreatment extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtDesc, txtCost;
    private treatmentControl ctrl;

    public fromTreatment() {
        super("📝 Manage Treatments");

        // ─── initialize the DB controller ─────────────────────────────────────────
        try {
            ctrl = new treatmentControl();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Cannot connect to database:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            // if you can’t talk to the DB, there’s no point keeping the UI alive
            System.exit(1);
        }

        // ─── basic window setup ───────────────────────────────────────────────────
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ─── top: entry form ───────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Name:"));
        txtName = new JTextField();
        form.add(txtName);

        form.add(new JLabel("Description:"));
        txtDesc = new JTextField();
        form.add(txtDesc);

        form.add(new JLabel("Cost:"));
        txtCost = new JTextField();
        form.add(txtCost);

        add(form, BorderLayout.NORTH);

        // ─── center: table ─────────────────────────────────────────────────────────
        model = new DefaultTableModel(new String[]{"ID", "Name", "Desc", "Cost"}, 0);
        table = new JTable(model);
        refreshTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ─── bottom: buttons ──────────────────────────────────────────────────────
        JPanel btns = new JPanel();
        JButton btnAdd = new JButton("Add"),
                btnUpd = new JButton("Update"),
                btnDel = new JButton("Delete"),
                btnBack = new JButton("Back");
        btns.add(btnAdd);
        btns.add(btnUpd);
        btns.add(btnDel);
        btns.add(btnBack);
        add(btns, BorderLayout.SOUTH);

        // ─── listeners ─────────────────────────────────────────────────────────────
        // row → form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                txtName.setText(model.getValueAt(r, 1).toString());
                txtDesc.setText(model.getValueAt(r, 2).toString());
                txtCost.setText(model.getValueAt(r, 3).toString());
            }
        });

        // Add
        btnAdd.addActionListener(e -> {
            Treatment t = new Treatment(
                0,
                txtName.getText(),
                txtDesc.getText(),
                Double.parseDouble(txtCost.getText())
            );
            if (ctrl.addTreatment(t)) refreshTable();
        });

        // Update
        btnUpd.addActionListener(e -> {
            int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
            Treatment t = new Treatment(
                id,
                txtName.getText(),
                txtDesc.getText(),
                Double.parseDouble(txtCost.getText())
            );
            if (ctrl.updateTreatment(t)) refreshTable();
        });

        // Delete
        btnDel.addActionListener(e -> {
            int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
            if (ctrl.deleteTreatment(id)) refreshTable();
        });

        // Back
        btnBack.addActionListener(e -> dispose());
    }

    private void refreshTable() {
        model.setRowCount(0);
        List<Treatment> list = ctrl.getAllTreatments();
        for (Treatment t : list) {
            model.addRow(new Object[]{
                t.getTreatmentId(),
                t.getName(),
                t.getDescription(),
                t.getCost()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new fromTreatment().setVisible(true));
    }
}
