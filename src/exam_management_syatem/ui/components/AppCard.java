package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class AppCard extends JPanel {

    private boolean elevated;

    public AppCard() {
        this(false);
    }

    public AppCard(boolean elevated) {
        this.elevated = elevated;
        setOpaque(false);
        setBackground(Colors.SURFACE);
        setBorder(new EmptyBorder(Dimensions.CARD_PADDING, Dimensions.CARD_PADDING, Dimensions.CARD_PADDING, Dimensions.CARD_PADDING));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw Shadow/Elevation (simple implementation for Swing)
        if (elevated) {
            g2.setColor(new Color(0, 0, 0, 10)); // Soft shadow
            g2.fillRoundRect(2, 2, width - 4, height - 2, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
        }

        // Draw Surface
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1 - (elevated ? 1 : 0), Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);

        // Draw Border
        g2.setColor(Colors.BORDER);
        g2.drawRoundRect(0, 0, width - 1, height - 1 - (elevated ? 1 : 0), Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);

        g2.dispose();
    }
}
