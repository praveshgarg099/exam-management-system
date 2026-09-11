package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class StatusBadge extends JPanel {

    public enum StatusType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO,
        NEUTRAL
    }

    private JLabel label;
    private StatusType type;

    public StatusBadge(String text, String rawStatus) {
        this(text, mapRawStatusToType(rawStatus));
    }

    public StatusBadge(String text, StatusType type) {
        this.type = type;
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        
        setBorder(new EmptyBorder(2, Spacing.SM, 2, Spacing.SM));

        label = new JLabel(text.toUpperCase());
        label.setFont(Typography.CAPTION);
        
        updateColors();
        add(label, java.awt.BorderLayout.CENTER);
    }

    private void updateColors() {
        switch (type) {
            case SUCCESS:
                setBackground(Colors.STATUS_SUCCESS_BG);
                label.setForeground(Colors.STATUS_SUCCESS_TEXT);
                break;
            case WARNING:
                setBackground(Colors.STATUS_WARNING_BG);
                label.setForeground(Colors.STATUS_WARNING_TEXT);
                break;
            case ERROR:
                setBackground(Colors.STATUS_ERROR_BG);
                label.setForeground(Colors.STATUS_ERROR_TEXT);
                break;
            case INFO:
                setBackground(Colors.STATUS_INFO_BG);
                label.setForeground(Colors.STATUS_INFO_TEXT);
                break;
            case NEUTRAL:
            default:
                setBackground(Colors.SURFACE_ELEVATED);
                label.setForeground(Colors.TEXT_SECONDARY);
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(getBackground());
        // Fully rounded pill shape
        g2.fillRoundRect(0, 0, width - 1, height - 1, height - 1, height - 1);

        g2.dispose();
        super.paintComponent(g);
    }

    public static StatusType mapRawStatusToType(String rawStatus) {
        if (rawStatus == null) return StatusType.NEUTRAL;
        
        switch (rawStatus.trim().toUpperCase()) {
            case "ACTIVE":
            case "PASSED":
            case "COMPLETED":
                return StatusType.SUCCESS;
            case "IN_PROGRESS":
            case "PENDING":
                return StatusType.WARNING;
            case "FAILED":
            case "EXPIRED":
            case "CANCELLED":
            case "INACTIVE":
                return StatusType.ERROR;
            case "SCHEDULED":
                return StatusType.INFO;
            default:
                return StatusType.NEUTRAL;
        }
    }
}
