package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class StatCard extends AppCard {

    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel subtitleLabel;

    public StatCard(String title, String value) {
        this(title, value, null);
    }

    public StatCard(String title, String value, String subtitle) {
        super(true); // elevated
        setLayout(new BorderLayout(0, Spacing.SM));

        titleLabel = new JLabel(title);
        titleLabel.setFont(Typography.LABEL);
        titleLabel.setForeground(Colors.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(Typography.DISPLAY);
        valueLabel.setForeground(Colors.TEXT_PRIMARY);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);

        add(contentPanel, BorderLayout.CENTER);

        if (subtitle != null) {
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(Typography.CAPTION);
            subtitleLabel.setForeground(Colors.TEXT_MUTED);
            add(subtitleLabel, BorderLayout.SOUTH);
        }
    }
    
    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        if (subtitleLabel == null) {
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(Typography.CAPTION);
            subtitleLabel.setForeground(Colors.TEXT_MUTED);
            add(subtitleLabel, BorderLayout.SOUTH);
        } else {
            subtitleLabel.setText(subtitle);
        }
        revalidate();
        repaint();
    }
}
