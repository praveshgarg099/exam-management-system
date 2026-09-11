package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AppButton extends JButton {
    
    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        DANGER,
        GHOST
    }

    private ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressedState = false;

    public AppButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public AppButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        initComponent();
    }

    private void initComponent() {
        setFont(Typography.BUTTON);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width + 40, Dimensions.BUTTON_HEIGHT));
        
        Border padding = new EmptyBorder(0, 16, 0, 16);
        setBorder(padding);

        updateColors();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = false;
                    isPressedState = false;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressedState = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    isPressedState = false;
                    repaint();
                }
            }
        });
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        setCursor(b ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        updateColors();
        repaint();
    }
    
    private void updateColors() {
        if (!isEnabled()) {
            setForeground(Colors.TEXT_MUTED);
            return;
        }

        switch (style) {
            case PRIMARY:
            case DANGER:
                setForeground(Colors.TEXT_ON_PRIMARY);
                break;
            case SECONDARY:
            case GHOST:
                setForeground(Colors.TEXT_PRIMARY);
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg = getBackgroundColor();
        Color border = getBorderColor();

        // Draw Background
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
        }

        // Draw Border
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
        }

        g2.dispose();
        super.paintComponent(g); // Paint text and icon
    }

    private Color getBackgroundColor() {
        if (!isEnabled()) {
            if (style == ButtonStyle.GHOST) return null;
            return Colors.SURFACE_ELEVATED; // disabled bg
        }

        switch (style) {
            case PRIMARY:
                if (isPressedState) return Colors.PRIMARY_PRESSED;
                if (isHovered) return Colors.PRIMARY_HOVER;
                return Colors.PRIMARY;
            case DANGER:
                if (isPressedState) return Color.decode("#B91C1C"); // Tailwind Red 700
                if (isHovered) return Color.decode("#DC2626"); // Tailwind Red 600
                return Colors.STATUS_ERROR;
            case SECONDARY:
                if (isPressedState) return Colors.BORDER;
                if (isHovered) return Colors.SURFACE_ELEVATED;
                return Colors.SURFACE;
            case GHOST:
                if (isPressedState) return Colors.BORDER;
                if (isHovered) return Colors.SURFACE_ELEVATED;
                return null;
        }
        return Colors.SURFACE;
    }

    private Color getBorderColor() {
        if (!isEnabled()) {
            if (style == ButtonStyle.GHOST) return null;
            return Colors.BORDER;
        }

        switch (style) {
            case PRIMARY:
            case DANGER:
            case GHOST:
                return null;
            case SECONDARY:
                return Colors.BORDER;
        }
        return null;
    }
}
