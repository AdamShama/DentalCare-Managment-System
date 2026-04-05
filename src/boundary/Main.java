package boundary;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // 1) Set Nimbus (or Metal) L&F
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fallback to default
        }

        // 2) Override a few colors & fonts
        UIManager.put("control", new ColorUIResource(245, 240, 255));           // window bg
        UIManager.put("nimbusBase", new ColorUIResource(110,  33, 174));        // dark accent
        UIManager.put("nimbusBlueGrey", new ColorUIResource(200, 190, 255));    // panel bg
        UIManager.put("nimbusFocus", new ColorUIResource(150,  50, 200));       // focus ring
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font",    new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Button.background", new ColorUIResource(150,  50, 200));
        UIManager.put("Button.foreground", Color.WHITE);

        // then spin up your UI…
        SwingUtilities.invokeLater(() -> {
            JFrame menu = new JFrame("🦷 DentalCare – Main Menu");
            menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menu.setSize(400,300);
            menu.setLocationRelativeTo(null);
            menu.setLayout(new BoxLayout(menu.getContentPane(), BoxLayout.Y_AXIS));

            JButton btnReports = new JButton("Generate Treatment Progress Report");
            btnReports.setAlignmentX(Component.CENTER_ALIGNMENT);
            menu.add(Box.createVerticalStrut(30));
            menu.add(btnReports);
            menu.add(Box.createVerticalGlue());

            btnReports.addActionListener(e ->
                new fromTreatmentProgressReport().setVisible(true)
            );

            menu.setVisible(true);
        });
    }
}
