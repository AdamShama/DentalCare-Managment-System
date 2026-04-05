package boundary;

import javax.swing.*;
import java.awt.*;

public class GradientBackgroundPanel extends JPanel {
    public GradientBackgroundPanel() {
        setLayout(new BorderLayout(20,20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        int w = getWidth(), h = getHeight();
        // from deep space blue to near black
        GradientPaint grad = new GradientPaint(
            0, 0, new Color(5, 5, 30),
            0, h, new Color(0, 0, 0)
        );
        g2.setPaint(grad);
        g2.fillRect(0,0,w,h);
        g2.dispose();
    }
}
