// src/boundary/TreatmentPlanManagementFrame.java
package boundary;

import control.TreatmentPlanController;
import control.patientControl;
import entity.TreatmentPlan;
import entity.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TreatmentPlanManagementFrame extends JFrame {
    private final TreatmentPlanController tpCtrl;
    private final patientControl          pCtrl;
    private final JComboBox<Patient>      cboPatients;
    private final DefaultTableModel       model;
    private final JTable                  table;

    public TreatmentPlanManagementFrame() {
        super("🗂 Treatment Plan Management");

        try {
            tpCtrl = new TreatmentPlanController();
            pCtrl  = new patientControl();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "DB error:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(ex);
        }

        // north: pick patient
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Patient:"));
        cboPatients = new JComboBox<>();
        top.add(cboPatients);
        add(top, BorderLayout.NORTH);

        // center: table
        model = new DefaultTableModel(
            new Object[]{"PlanID", "Start", "End", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // south: buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton bNew = new JButton("New Plan"),
                bEdt = new JButton("Edit Plan"),
                bDel = new JButton("Cancel Plan");
        btns.add(bNew); btns.add(bEdt); btns.add(bDel);
        add(btns, BorderLayout.SOUTH);

        // load patients & hook selection → loadPlans()
        loadPatients();
        cboPatients.addActionListener(e -> loadPlans());

        // New Plan
        bNew.addActionListener(e -> editDialog(null));

        // Edit Plan
        bEdt.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            String planId = model.getValueAt(r, 0).toString();
            editDialog(planId);
        });

        // Cancel Plan
        bDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            String planId = model.getValueAt(r, 0).toString();
            if (JOptionPane.showConfirmDialog(
                  this,
                  "Cancel plan " + planId + "?",
                  "Confirm",
                  JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                try {
                    tpCtrl.delete(planId);
                    loadPlans();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Delete failed:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // frame setup
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        // select first patient & show plans
        if (cboPatients.getItemCount() > 0) {
            cboPatients.setSelectedIndex(0);
            loadPlans();
        }
    }

    private void loadPatients() {
        cboPatients.removeAllItems();
        try {
            // iterate over the List<Patient>, not the table model
            for (Patient p : pCtrl.getAllPatients()) {
                cboPatients.addItem(p);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load patients:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadPlans() {
        Patient sel = (Patient) cboPatients.getSelectedItem();
        if (sel == null) return;
        try {
            // pass the String ID directly:
            String pid = sel.getPatientId();
            List<TreatmentPlan> list = tpCtrl.getByPatient(pid);

            model.setRowCount(0);
            for (TreatmentPlan t : list) {
                model.addRow(new Object[]{
                    t.getId(),
                    t.getStartDate(),
                    t.getEstimatedCompletion(),
                    t.getStatus().name()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Load plans failed:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void editDialog(String planId) {
        Patient sel = (Patient) cboPatients.getSelectedItem();
        if (sel == null) return;

        LocalDate s = LocalDate.now(), e = null;
        TreatmentPlan.PlanStatus st = TreatmentPlan.PlanStatus.ACTIVE;

        if (planId != null) {
            try {
                // pass the patient ID as String, exactly matching your controller API:
                String pid = sel.getPatientId();
                for (TreatmentPlan t : tpCtrl.getByPatient(pid)) {
                    if (t.getId().equals(planId)) {
                        s  = t.getStartDate();
                        e  = t.getEstimatedCompletion();
                        st = t.getStatus();
                        break;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Load plan failed:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        JTextField txtS = new JTextField(s.toString());
        JTextField txtE = new JTextField(e != null ? e.toString() : "");
        JComboBox<String> cmb = new JComboBox<>(
          new String[]{"ACTIVE","COMPLETED","CANCELLED"}
        );
        cmb.setSelectedItem(st.name());

        Object[] form = {
          "Start (YYYY-MM-DD):", txtS,
          "End   (or blank):",   txtE,
          "Status:",             cmb
        };

        int ch = JOptionPane.showConfirmDialog(
          this, form,
          planId == null ? "New Plan" : "Edit Plan",
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.PLAIN_MESSAGE
        );
        if (ch != JOptionPane.OK_OPTION) return;

        try {
            LocalDate ns = LocalDate.parse(txtS.getText().trim());
            LocalDate ne = txtE.getText().trim().isEmpty()
                         ? null
                         : LocalDate.parse(txtE.getText().trim());
            TreatmentPlan tp = new TreatmentPlan(
               planId == null ? "" : planId,
               sel, ns, ne
            );
            tp.setStatus(TreatmentPlan.PlanStatus.valueOf(
                ((String) cmb.getSelectedItem()).toUpperCase()
            ));

            if (planId == null) tpCtrl.insert(tp);
            else               tpCtrl.update(tp);

            loadPlans();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
              this,
              "Save failed:\n" + ex.getMessage(),
              "Error",
              JOptionPane.ERROR_MESSAGE
            );
        }
    }

}
