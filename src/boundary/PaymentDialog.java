package boundary;

import control.appointmentControl;
import entity.DbConsts;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

public class PaymentDialog extends JDialog {
    private final JFormattedTextField tfCard;
    private final JFormattedTextField tfExpiry;
    private final JPasswordField       pfCvv;
    private boolean succeeded;

    public PaymentDialog(Window owner, int apptId, appointmentControl ctrl) {
        super(owner, "Pay for Appointment #" + apptId, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10,10));

        // 1) Inputs panel
        JPanel inputs = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;

        try {
            tfCard   = new JFormattedTextField(new MaskFormatter("#### #### #### ####"));
            tfExpiry = new JFormattedTextField(new MaskFormatter("##/##"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        pfCvv    = new JPasswordField(4);

        tfCard   .setColumns(16);
        tfExpiry .setColumns(5);
        pfCvv    .setColumns(3);

        // Row 0: Card #
        gbc.gridx = 0; gbc.gridy = 0;
        inputs.add(new JLabel("Card Number:"), gbc);
        gbc.gridx = 1;
        inputs.add(tfCard, gbc);

        // Row 1: Expiry
        gbc.gridx = 0; gbc.gridy = 1;
        inputs.add(new JLabel("Expiry (MM/YY):"), gbc);
        gbc.gridx = 1;
        inputs.add(tfExpiry, gbc);

        // Row 2: CVV
        gbc.gridx = 0; gbc.gridy = 2;
        inputs.add(new JLabel("CVV:"), gbc);
        gbc.gridx = 1;
        inputs.add(pfCvv, gbc);

        add(inputs, BorderLayout.CENTER);

        // 2) Buttons panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,10));
        JButton btnOK     = new JButton("Pay");
        JButton btnCancel = new JButton("Cancel");
        btnOK    .setEnabled(false);  // start disabled
        buttons.add(btnOK);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // 3) Validation: enable OK only when all fields look filled
        KeyAdapter validator = new KeyAdapter() {
          void validateAll() {
            boolean ok = tfCard.getText().replace(" ","").matches("\\d{16}")
                      && tfExpiry.getText().matches("\\d{2}/\\d{2}")
                      && pfCvv.getPassword().length >= 3;
            btnOK.setEnabled(ok);
          }
          public void keyReleased(KeyEvent e) { validateAll(); }
        };
        tfCard.addKeyListener(validator);
        tfExpiry.addKeyListener(validator);
        pfCvv .addKeyListener(validator);

        // 4) Button actions
        btnOK.addActionListener(e -> {
            try {
                // call your control to mark paid
                if (ctrl.payAppointment(apptId)) {
                    succeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                      "Payment failed!",
                      "Error",
                      JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                  "Database error:\n"+ex.getMessage(),
                  "Error",
                  JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dispose());

        // 5) nice defaults
        getRootPane().setDefaultButton(btnOK);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Show the dialog; returns true if “Pay” was clicked & succeeded */
    public static boolean showPayment(Window owner, int apptId, appointmentControl ctrl) {
        PaymentDialog dlg = new PaymentDialog(owner, apptId, ctrl);
        dlg.setVisible(true);
        return dlg.succeeded;
    }
    
    public static void processRefund(int appointmentId, Connection conn) {
        // 1) Ask the user to confirm
        int choice = JOptionPane.showConfirmDialog(
            null,
            "Do you want to refund payment for appointment #" + appointmentId + "?",
            "Confirm Refund",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;  // user cancelled
        }

        // 2) Update the database
        String sql = "UPDATE " + DbConsts.TABLE_APPOINTMENTS +
                " SET Paid = FALSE, Refunded = TRUE" +
                " WHERE AppointmentID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            int updated = ps.executeUpdate();
            if (updated == 1) {
                JOptionPane.showMessageDialog(
                    null,
                    "Refund processed for appointment #" + appointmentId,
                    "Refund Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "No appointment found with ID " + appointmentId,
                    "Refund Failed",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                null,
                "Error processing refund:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
