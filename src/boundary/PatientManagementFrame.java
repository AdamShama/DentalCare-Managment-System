// src/boundary/PatientManagementFrame.java
package boundary;

import control.patientControl;
import entity.MedicalHistory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Pattern;

public class PatientManagementFrame extends JFrame {
    private final patientControl pc;
    private final JTable tbl;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JTextField txtName, txtPhone, txtEmail, txtAge, txtDentistId;
    private final JTextField txtAllergies, txtPreconds;
    private final TableRowSorter<DefaultTableModel> sorter;

    public PatientManagementFrame() {
        super("🦷 Patient Management");

        // 1) Controller
        try {
            pc = new patientControl();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to database:\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }

        // 2) Load patients into model
        DefaultTableModel tmpModel;
        try {
            tmpModel = pc.getAllPatientsModel(); // cols include PatientID, FullName, PhoneNumber, Email, Age, DentistID
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load patients:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            tmpModel = new DefaultTableModel(
              new Object[]{"PatientID","FullName","PhoneNumber","Email","Age","DentistID"},
              0
            );
        }
        model  = tmpModel;
        tbl    = new JTable(model);

        // 3) Row sorter for Search
        sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);

        // 4) Search field + button
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> {
            String text = txtSearch.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1));
            }
        });

        // 5) Form fields
        txtName      = new JTextField(15);
        txtPhone     = new JTextField(12);
        txtEmail     = new JTextField(15);
        txtAge       = new JTextField(3);
        txtDentistId = new JTextField(3);
        txtAllergies = new JTextField(20);
        txtPreconds  = new JTextField(20);

        // 6) Action buttons
        JButton btnAdd     = new JButton("Add");
        JButton btnUpdate  = new JButton("Update");
        JButton btnDelete  = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");

        // 7) Layout setup
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        north.add(new JLabel("Search Name:"));
        north.add(txtSearch);
        north.add(btnSearch);

        // 7a) Form now has 7 rows
        JPanel form = new JPanel(new GridLayout(7,2,5,5));
        form.setBorder(BorderFactory.createTitledBorder("Patient Details"));
        form.add(new JLabel("Name:"));      form.add(txtName);
        form.add(new JLabel("PhoneNumber:")); form.add(txtPhone);
        form.add(new JLabel("Email:"));     form.add(txtEmail);
        form.add(new JLabel("Age:"));       form.add(txtAge);
        form.add(new JLabel("DentistID:")); form.add(txtDentistId);
        form.add(new JLabel("Allergies:")); form.add(txtAllergies);
        form.add(new JLabel("Pre‑Existing:")); form.add(txtPreconds);

        JPanel btns = new JPanel();
        btns.add(btnAdd);
        btns.add(btnUpdate);
        btns.add(btnDelete);
        btns.add(btnRefresh);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(north,               BorderLayout.NORTH);
        getContentPane().add(form,                BorderLayout.WEST);
        getContentPane().add(new JScrollPane(tbl),BorderLayout.CENTER);
        getContentPane().add(btns,                BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        // 8) Button actions
        btnAdd    .addActionListener(e -> doAdd());
        btnUpdate .addActionListener(e -> doUpdate());
        btnDelete .addActionListener(e -> doDelete());
        btnRefresh.addActionListener(e -> doRefresh());

        // 9) Table row selection populates form + medical history
        tbl.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
            int viewRow = tbl.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = tbl.convertRowIndexToModel(viewRow);

                txtName      .setText(model.getValueAt(modelRow,1).toString());
                txtPhone     .setText(model.getValueAt(modelRow,2).toString());
                txtEmail     .setText(model.getValueAt(modelRow,3).toString());
                txtAge       .setText(model.getValueAt(modelRow,4).toString());
                txtDentistId .setText(model.getValueAt(modelRow,5).toString());

                // --- fetch & display medical history ---
                try {
                    int pid = (Integer)model.getValueAt(modelRow,0);
                    MedicalHistory mh = pc.getMedicalHistoryForPatient(pid);
                    txtAllergies.setText(String.join(", ", mh.getAllergies()));
                    txtPreconds .setText(String.join(", ", mh.getPreExistingConditions()));
                } catch (Exception ex) {
                    // on error, clear the fields
                    txtAllergies.setText("");
                    txtPreconds .setText("");
                }
            }
        });
    }

    private void doAdd() {
        try {
            pc.addPatient(
                txtName.getText(),
                txtPhone.getText(),
                txtEmail.getText(),
                Integer.parseInt(txtAge.getText()),
                Integer.parseInt(txtDentistId.getText())
            );
            doRefresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void doUpdate() {
        try {
            int viewRow = tbl.getSelectedRow();
            if (viewRow < 0) throw new IllegalStateException("Select a patient to update");
            int modelRow = tbl.convertRowIndexToModel(viewRow);
            int id       = (Integer) model.getValueAt(modelRow,0);

            pc.updatePatient(
                id,
                txtName.getText(),
                txtPhone.getText(),
                txtEmail.getText(),
                Integer.parseInt(txtAge.getText()),
                Integer.parseInt(txtDentistId.getText())
            );
            doRefresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void doDelete() {
        try {
            int viewRow = tbl.getSelectedRow();
            if (viewRow < 0) throw new IllegalStateException("Select a patient to delete");
            int modelRow = tbl.convertRowIndexToModel(viewRow);
            int id       = (Integer) model.getValueAt(modelRow,0);
            pc.deletePatient(id);
            doRefresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void doRefresh() {
        try {
            DefaultTableModel sourceModel = pc.getAllPatientsModel();
            model.setRowCount(0);
            for (Object rowObj : (Vector)sourceModel.getDataVector()) {
                Vector rowVector = (Vector)rowObj;
                model.addRow(rowVector.toArray());
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
          ex.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
    }
}
