// src/boundary/DashboardPanel.java
package boundary;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

import control.SecretaryControl;
import control.StaffControl;
import control.dentistControl;
import boundary.fromDentist;
import boundary.fromHygienist;
import boundary.fromSecretary;
import boundary.fromPatient;
import boundary.ManagerDashboardFrame;

public class DashboardPanel extends JFrame {

    public DashboardPanel() {
        super("🦷 DentalCare");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);                   // back to fixed size
        setLocationRelativeTo(null);

        // swap in our animated, cover‑scaled BackgroundPanel
        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);

        // create your overlays
        JPanel header  = createAdPanel();    header.setOpaque(false);
        JPanel buttons = createRoleButtonPanel(); buttons.setOpaque(false);

        bg.add(header,  BorderLayout.NORTH);
        bg.add(buttons, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createAdPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel ad = new JLabel(
            "<html><h1>❤️ DentalCare ❤️</h1><p>Your smile, our passion!</p></html>",
            SwingConstants.CENTER
        );
        ad.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        p.add(ad, BorderLayout.CENTER);
        return p;
    }

    private JPanel createRoleButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        String[] roles = { "Dentist", "Hygienist", "Secretary", "Patient" };
        for (String role : roles) {
            JButton btn = new JButton("Sign in as " + role);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> openLoginForm(role));
            p.add(btn);
        }
        return p;
    }

    private void openLoginForm(String role) {
        System.out.println("Opening login for role: " + role);

        CosmicLoginDialog login = new CosmicLoginDialog(this, role);
        login.setVisible(true);

        System.out.println("Dialog closed");
        System.out.println("Succeeded: " + login.isSucceeded());
        System.out.println("Entered ID: " + login.getEnteredId());

        if (!login.isSucceeded()) {
            System.out.println("Login failed or dialog returned false");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(login.getEnteredId());
            System.out.println("Parsed ID: " + id);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid ID format");
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid ID format");
            return;
        }

        try {
            switch (role) {
                case "Dentist": {
                    System.out.println("Opening Dentist flow");
                    dentistControl dc = new dentistControl();
                    if (dc.isManager(id)) {
                        System.out.println("Opening ManagerDashboardFrame");
                        new ManagerDashboardFrame(id).setVisible(true);
                    } else {
                        System.out.println("Opening fromDentist");
                        new fromDentist(id).setVisible(true);
                    }
                    break;
                }
                case "Hygienist":
                    System.out.println("Opening fromHygienist");
                    new fromHygienist(id).setVisible(true);
                    break;
                case "Secretary":
                    System.out.println("Opening fromSecretary");
                    new fromSecretary(id).setVisible(true);
                    break;
                case "Patient":
                    System.out.println("Opening fromPatient");
                    new fromPatient(id).setVisible(true);
                    break;
                default:
                    System.out.println("Access denied");
                    showAccessDenied(id);
                    return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage());
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error:\n" + ex.getMessage());
            return;
        }

        System.out.println("Closing DashboardPanel");
        dispose();
    }

    // ... elsewhere in your class:



    private void showAccessDenied(int id) {
       JOptionPane.showMessageDialog(
            this,
            "Access denied: ID " + id,
           "Unauthorized",
            JOptionPane.WARNING_MESSAGE
        );
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(DashboardPanel::new);
    }
}
