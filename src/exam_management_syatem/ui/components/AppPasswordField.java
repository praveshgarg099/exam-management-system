package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class AppPasswordField extends JPanel {

    private final JPasswordField passwordField;
    private final JButton toggleButton;
    private boolean isFocused = false;
    private boolean passwordVisible = false;
    private String placeholder = "";

    public AppPasswordField() {
        this("");
    }

    public AppPasswordField(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(getPreferredSize().width, Dimensions.INPUT_HEIGHT));
        setMinimumSize(new Dimension(100, Dimensions.INPUT_HEIGHT));

        passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !AppPasswordField.this.placeholder.isEmpty() && !isFocused) {
                    Graphics2D gPlaceholder = (Graphics2D) g.create();
                    gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gPlaceholder.setFont(getFont());
                    gPlaceholder.setColor(Colors.TEXT_MUTED);
                    int textY = (getHeight() - gPlaceholder.getFontMetrics().getHeight()) / 2 + gPlaceholder.getFontMetrics().getAscent();
                    gPlaceholder.drawString(AppPasswordField.this.placeholder, 2, textY);
                    gPlaceholder.dispose();
                }
            }
        };

        passwordField.setFont(Typography.BODY);
        passwordField.setForeground(Colors.TEXT_PRIMARY);
        passwordField.setCaretColor(Colors.PRIMARY);
        passwordField.setOpaque(false);
        passwordField.setBorder(new EmptyBorder(0, Spacing.MD, 0, Spacing.SM));

        passwordField.addFocusListener(new FocusListener() {
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
        });

        // Show/Hide Toggle Button
        toggleButton = new JButton("Show");
        toggleButton.setFont(Typography.CAPTION);
        toggleButton.setForeground(Colors.TEXT_SECONDARY);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setOpaque(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Show password");
        toggleButton.setBorder(new EmptyBorder(0, Spacing.SM, 0, Spacing.MD));

        toggleButton.addActionListener(e -> togglePasswordVisibility());

        add(passwordField, BorderLayout.CENTER);
        add(toggleButton, BorderLayout.EAST);
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            toggleButton.setText("Hide");
            toggleButton.setToolTipText("Hide password");
        } else {
            passwordField.setEchoChar('\u2022');
            toggleButton.setText("Show");
            toggleButton.setToolTipText("Show password");
        }
        passwordField.requestFocusInWindow();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void setText(String t) {
        passwordField.setText(t);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        repaint();
    }

    public void addActionListener(ActionListener l) {
        passwordField.addActionListener(l);
    }

    @Override
    public boolean requestFocusInWindow() {
        return passwordField.requestFocusInWindow();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(isEnabled() ? Colors.SURFACE : Colors.SURFACE_ELEVATED);
        g2.fillRoundRect(0, 0, width - 1, height - 1, Dimensions.BORDER_RADIUS, Dimensions.BORDER_RADIUS);

        // Border / Focus Glow
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
    }
}
