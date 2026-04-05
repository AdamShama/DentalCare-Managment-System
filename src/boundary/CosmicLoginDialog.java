package boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.SQLException;

import control.StaffControl;
import control.dentistControl;

public class CosmicLoginDialog extends JDialog {
    private final JTextField idField = new JTextField(10);
    private boolean succeeded = false;

    public CosmicLoginDialog(JFrame parent, String role) {
        super(parent, "🌌 Sign in as " + role, true);
        setUndecorated(true);  // remove title bar for a sleek look

        // build UI
        GradientBackgroundPanel bg = new GradientBackgroundPanel();
        setContentPane(bg);

        // label + field
        JLabel prompt = new JLabel("Enter your " + (role.equals("Patient") ? "Patient" : "Staff") + " ID:");
        prompt.setForeground(Color.WHITE);
        prompt.setFont(prompt.getFont().deriveFont(Font.BOLD, 18f));

        idField.setFont(idField.getFont().deriveFont(16f));
        idField.setHorizontalAlignment(JTextField.CENTER);

        // buttons
        JButton signIn = new JButton("🚀 Go");
        JButton cancel = new JButton("✖ Cancel");

        signIn.setFont(signIn.getFont().deriveFont(Font.BOLD, 14f));
        cancel.setFont(cancel.getFont().deriveFont(Font.PLAIN, 14f));

        // layout
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0; gbc.gridy = 0;
        center.add(prompt, gbc);
        gbc.gridy = 1;
        center.add(idField, gbc);
        gbc.gridy = 2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,0));
        btns.setOpaque(false);
        btns.add(signIn);
        btns.add(cancel);
        center.add(btns, gbc);

        bg.add(center, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);

        // listeners
        cancel.addActionListener(e -> {
            dispose();
        });

     // inside CosmicLoginDialog.java, in the signIn.addActionListener:

        signIn.addActionListener(e -> {
            String txt = idField.getText().trim();
            if (txt.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "ID cannot be blank",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int id;
            try {
                id = Integer.parseInt(txt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid numeric ID",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean ok = false;
            try {
                StaffControl sc = new StaffControl();
                switch (role) {
                    case "Dentist":
                        // allow true dentists *or* managers
                        dentistControl dc = new dentistControl();
                        ok = sc.isDentist(id) || dc.isManager(id);
                        break;
                    case "Hygienist":
                        ok = sc.isHygienist(id);
                        break;
                    case "Secretary":
                        ok = sc.isSecretary(id);
                        break;
                    case "Patient":
                        ok = true; // or check PatientControl if you have one
                        break;
                }
            } catch (SQLException ex2) {
                JOptionPane.showMessageDialog(
                    this,
                    "Database error:\n" + ex2.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!ok) {
                showDeniedAnimation();
                return;
            }

            // success!
            succeeded = true;
            showSuccessAnimation();
            dispose();
        });


        // Enter key = sign in
        idField.addActionListener(signIn.getActionListeners()[0]);
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public String getEnteredId() {
        return idField.getText().trim();
    }

    private void showSuccessAnimation() {
        // 1) Try loading from the classpath
        URL url = Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("images/checkmark.gif");

        // 2) If it's missing, fall back to the standard info icon
        Icon icon = (url != null)
                  ? new ImageIcon(url)
                  : UIManager.getIcon("OptionPane.informationIcon");

        // 3) Build the dialog
        JDialog d = new JDialog(this, "Welcome!", true);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(new Color(0,0,0,200));
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        JLabel msg       = new JLabel("Login Successful!", SwingConstants.CENTER);
        msg.setForeground(Color.GREEN);
        msg.setFont(msg.getFont().deriveFont(Font.BOLD, 24f));

        p.add(iconLabel, BorderLayout.CENTER);
        p.add(msg,       BorderLayout.SOUTH);

        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(this);

        // 4) Auto‑dismiss after 1.5s
        new Timer(1500, e -> d.dispose()) {{
            setRepeats(false);
            start();
        }};

        d.setVisible(true);
    }
    
    
    private void showDeniedAnimation() {
        // 1) Load your “denied” GIF
        URL url = Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("images/accessDenied.gif");
        Icon icon = (url != null)
                  ? new ImageIcon(url)
                  : UIManager.getIcon("OptionPane.errorIcon");

        // 2) Build an undecorated, modal dialog
        JDialog d = new JDialog(this, "Access Denied", true);
        d.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(10,10)) {{
            setBackground(new Color(0,0,0,200));
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        }};

        JLabel ani = new JLabel(icon, SwingConstants.CENTER);
        JLabel msg = new JLabel("Access Denied!", SwingConstants.CENTER);
        msg.setForeground(Color.RED);
        msg.setFont(msg.getFont().deriveFont(Font.BOLD, 24f));

        p.add(ani, BorderLayout.CENTER);
        p.add(msg, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(this);

        // 3) Auto-dismiss after 1.5s
        Timer t = new Timer(1500, e -> d.dispose());
        t.setRepeats(false);
        t.start();

        d.setVisible(true);
    }

}
