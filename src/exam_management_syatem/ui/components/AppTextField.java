package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class AppTextField extends JTextField implements FocusListener {

    private boolean isFocused = false;
    private String placeholder = "";

    public AppTextField() {
        this("", 0);
    }

    public AppTextField(String placeholder) {
        this(placeholder, 0);
    }

    public AppTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder != null ? placeholder : "";
        initComponent();
    }

    private void initComponent() {
        setFont(Typography.BODY);
        setForeground(Colors.TEXT_PRIMARY);
        setBackground(Colors.SURFACE);
        setCaretColor(Colors.PRIMARY);
        setOpaque(false);
        setPreferredSize(new Dimension(getPreferredSize().width, Dimensions.INPUT_HEIGHT));
        setMinimumSize(new Dimension(100, Dimensions.INPUT_HEIGHT));

        setBorder(new CompoundBorder(
            new EmptyBorder(0, 0, 0, 0),
            new EmptyBorder(0, Spacing.MD, 0, Spacing.MD)
        ));

        addFocusListener(this);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public void focusGained(FocusEvent e) {
        isFocused = true;
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        isFocused = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw background
        g2.setColor(isEnabled() ? getBackground() : Colors.SURFACE_ELEVATED);
        g2.fillRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);

        // Draw border & focus ring
        if (isFocused && isEnabled()) {
            g2.setColor(Colors.BORDER_FOCUS);
            g2.drawRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
            g2.setColor(new Color(Colors.BORDER_FOCUS.getRed(), Colors.BORDER_FOCUS.getGreen(), Colors.BORDER_FOCUS.getBlue(), 45));
            g2.drawRoundRect(1, 1, width - 3, height - 3, Dimensions.BORDER_RADIUS - 1, Dimensions.BORDER_RADIUS - 1);
        } else {
            g2.setColor(Colors.BORDER);
            g2.drawRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);
        }

        g2.dispose();
        super.paintComponent(g);

        // Draw placeholder if text is empty and not focused
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gPlaceholder.setFont(getFont());
            gPlaceholder.setColor(Colors.TEXT_MUTED);
            int paddingLeft = Spacing.MD;
            int textY = (getHeight() - gPlaceholder.getFontMetrics().getHeight()) / 2 + gPlaceholder.getFontMetrics().getAscent();
            gPlaceholder.drawString(placeholder, paddingLeft, textY);
            gPlaceholder.dispose();
        }
    }
}
