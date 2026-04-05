// src/boundary/BackgroundPanel.java
package boundary;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BackgroundPanel extends JPanel {
    private final Image bg;
    private final Timer animator;

    public BackgroundPanel() {
        // 1) Load the animated GIF from the classpath
        URL url = Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("images/dashboard.gif");
        if (url == null) {
            throw new IllegalStateException(
              "dashboard.gif not found on classpath at images/dashboard.gif"
            );
        }
        ImageIcon icon = new ImageIcon(url);
        bg = icon.getImage();  // no load-status check needed

        // 2) Let your other panels float on top
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // 3) Repaint regularly so Swing advances the GIF frames
        animator = new Timer(100, e -> repaint());
        animator.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int pw = getWidth(), ph = getHeight();
        int iw = bg.getWidth(this), ih = bg.getHeight(this);

        // “cover” scale logic
        double scale = Math.max((double)pw/iw, (double)ph/ih);
        int w = (int)(iw * scale), h = (int)(ih * scale);
        int x = (pw - w) / 2, y = (ph - h) / 2;

        g.drawImage(bg, x, y, w, h, this);
    }
}
