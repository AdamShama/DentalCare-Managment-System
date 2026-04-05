// src/boundary/StaffManagementFrame.java
package boundary;

import control.StaffControl;
import entity.Staff;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * Manager‑only: Create / Read / Update / Delete staff members.
 */
public class StaffManagementFrame extends JFrame {
    private final StaffControl ctrl;
    private final Map<Integer,String> roles;
    private final DefaultTableModel model;
    private final JTable table;

    public StaffManagementFrame() {
        super("📋 Manage Staff");

        // connect + load roles
        try {
            ctrl  = new StaffControl();
            roles = ctrl.getRoles();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "DB error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(ex);
        }

        // table model & JTable
        String[] cols = {
          "Staff ID","Full Name","Role","Contact","Email","Qualification","Specialization"
        };
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){
                return c!=0; // ID read‑only
            }
        };
        table = new JTable(model);

        // in‑cell role dropdown (only the 4 roles)
        TableColumn roleCol = table.getColumnModel().getColumn(2);
        JComboBox<String> combo = new JComboBox<>(
            new String[]{ "Dentist", "Manager", "Secretary", "Hygienist" }
        );
        roleCol.setCellEditor(new DefaultCellEditor(combo));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        JButton btnAdd     = new JButton("Add New");
        JButton btnSave    = new JButton("Save All");
        JButton btnDelete  = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose   = new JButton("Close");
        buttons.add(btnAdd);
        buttons.add(btnSave);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);
        buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);

        // refresh action
        btnRefresh.addActionListener(e -> loadData());

        // add action (now uses new insert(...) that returns key)
        btnAdd.addActionListener(e -> {
            try {
                JTextField txtName    = new JTextField();
                JComboBox<String> cmbRole = new JComboBox<>(
                    new String[]{ "Dentist", "Manager", "Secretary", "Hygienist" }
                );
                JTextField txtContact = new JTextField();
                JTextField txtEmail   = new JTextField();
                JTextField txtQual    = new JTextField();
                JTextField txtSpec    = new JTextField();

                Object[] form = {
                    "Full Name:",           txtName,
                    "Role:",                cmbRole,
                    "Contact #:",           txtContact,
                    "Email:",               txtEmail,
                    "Qualification:",       txtQual,
                    "Specialization ID:",   txtSpec
                };

                int choice = JOptionPane.showConfirmDialog(
                    this, form, "Add New Staff",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );
                if (choice != JOptionPane.OK_OPTION) return;

                // lookup RoleID
                String selRoleName = (String)cmbRole.getSelectedItem();
                int selRoleId = roles.entrySet().stream()
                    .filter(en -> en.getValue().equalsIgnoreCase(selRoleName))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unknown role"));

                // only parse specialization for Dentist
                int specId = 0;
                if ("Dentist".equalsIgnoreCase(selRoleName)) {
                    specId = Integer.parseInt(txtSpec.getText().trim());
                }

                // build Staff (id=0 for DAO)
                Staff s = new Staff(
                    0,
                    txtName.getText().trim(),
                    selRoleId,
                    txtContact.getText().trim(),
                    txtEmail.getText().trim(),
                    txtQual.getText().trim(),
                    specId
                );

                // INSERT & get new key
                int newId = ctrl.insert(s);

                // add to table
                model.addRow(new Object[]{
                    newId,
                    s.getFullName(),
                    selRoleName,
                    s.getContactNumber(),
                    s.getEmail(),
                    s.getQualification(),
                    s.getSpecializationID()
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                  "Error adding staff:\n" + ex.getMessage(),
                  "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // delete action
        btnDelete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r>=0) {
                String idText = model.getValueAt(r,0).toString();
                if (!idText.isBlank()) {
                    try { ctrl.delete(Integer.parseInt(idText)); }
                    catch(SQLException ex){
                        JOptionPane.showMessageDialog(this,
                          "Delete failed:\n"+ex.getMessage(),
                          "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                model.removeRow(r);
            }
        });

        // save all (update only)
        btnSave.addActionListener(e -> {
            for(int i=0; i<model.getRowCount(); i++){
                String idText   = model.getValueAt(i,0).toString().trim();
                String full     = model.getValueAt(i,1).toString().trim();
                String roleName = model.getValueAt(i,2).toString();
                String contact  = model.getValueAt(i,3).toString().trim();
                String email    = model.getValueAt(i,4).toString().trim();
                String qual     = model.getValueAt(i,5).toString().trim();
                String specText = model.getValueAt(i,6).toString().trim();
                int spec        = specText.isBlank() ? 0 : Integer.parseInt(specText);

                int roleID = roles.entrySet().stream()
                    .filter(en -> en.getValue().equalsIgnoreCase(roleName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unknown role"))
                    .getKey();

                try {
                    // only updates existing rows here
                    Staff s = new Staff(
                        Integer.parseInt(idText),
                        full, roleID, contact, email, qual, spec
                    );
                    ctrl.update(s);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                      "Save error row " + (i+1) + ":\n" + ex.getMessage(),
                      "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            loadData();
        });

        // close
        btnClose.addActionListener(e -> dispose());

        // frame setup
        setSize(800,450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        loadData();
        setVisible(true);
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            for (Staff s : ctrl.getAll()) {
                model.addRow(new Object[]{
                  s.getStaffID(),
                  s.getFullName(),
                  roles.get(s.getRoleID()),
                  s.getContactNumber(),
                  s.getEmail(),
                  s.getQualification(),
                  s.getSpecializationID()
                });
            }
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this,
              "Load failed:\n" + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
