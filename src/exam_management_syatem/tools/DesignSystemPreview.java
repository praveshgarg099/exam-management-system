package exam_management_syatem.tools;

import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.components.SearchField;
import exam_management_syatem.ui.components.StatCard;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.DesignSystem;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;
import exam_management_syatem.ui.theme.AppTheme;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class DesignSystemPreview extends JFrame {

    public static void main(String[] args) {
        AppTheme.setup();
        java.awt.EventQueue.invokeLater(() -> {
            new DesignSystemPreview().setVisible(true);
        });
    }

    public DesignSystemPreview() {
        setTitle("UI.1 - Premium Design System Preview");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new javax.swing.BoxLayout(mainContent, javax.swing.BoxLayout.Y_AXIS));
        mainContent.setBackground(Colors.BACKGROUND);
        mainContent.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        mainContent.add(createTypographySection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createColorsSection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createButtonsSection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createStatusSection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createInputsSection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createCardsSection());
        mainContent.add(javax.swing.Box.createRigidArea(new Dimension(0, Spacing.XXL)));
        mainContent.add(createTableSection());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        // Simple Sidebar simulation
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(Dimensions.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(Colors.SURFACE);
        sidebar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER));
        sidebar.setLayout(new FlowLayout(FlowLayout.LEFT, Spacing.LG, Spacing.LG));
        
        JLabel brand = new JLabel("Exam Platform");
        brand.setFont(Typography.H2);
        brand.setForeground(Colors.PRIMARY);
        sidebar.add(brand);
        
        String[] navs = {"Dashboard", "Students", "Subjects", "Questions", "Exams", "Results", "Settings"};
        for (String nav : navs) {
            AppButton navBtn = new AppButton(nav, AppButton.ButtonStyle.GHOST);
            navBtn.setPreferredSize(new Dimension(Dimensions.SIDEBAR_WIDTH - Spacing.LG * 2, Dimensions.BUTTON_HEIGHT));
            navBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            sidebar.add(navBtn);
        }

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel(new BorderLayout(0, Spacing.MD));
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(Typography.H2);
        l.setForeground(Colors.TEXT_PRIMARY);
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    private JPanel createTypographySection() {
        JPanel p = createSection("Typography");
        JPanel content = new JPanel(new GridLayout(0, 1, 0, Spacing.SM));
        content.setOpaque(false);

        addTypoRow(content, "Display - 36px Bold", Typography.DISPLAY);
        addTypoRow(content, "H1 - 24px Bold", Typography.H1);
        addTypoRow(content, "H2 - 20px Bold", Typography.H2);
        addTypoRow(content, "H3 - 16px Bold", Typography.H3);
        addTypoRow(content, "Body - 14px Regular", Typography.BODY);
        addTypoRow(content, "Body Small - 13px Regular", Typography.BODY_SMALL);
        addTypoRow(content, "Label - 13px Bold", Typography.LABEL);
        addTypoRow(content, "Caption - 12px Regular", Typography.CAPTION);

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void addTypoRow(JPanel parent, String text, java.awt.Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(Colors.TEXT_PRIMARY);
        parent.add(l);
    }

    private JPanel createColorsSection() {
        JPanel p = createSection("Colors");
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, Spacing.MD));
        content.setOpaque(false);

        content.add(createColorBox("Primary", Colors.PRIMARY));
        content.add(createColorBox("Hover", Colors.PRIMARY_HOVER));
        content.add(createColorBox("Success", Colors.STATUS_SUCCESS));
        content.add(createColorBox("Warning", Colors.STATUS_WARNING));
        content.add(createColorBox("Error", Colors.STATUS_ERROR));
        content.add(createColorBox("Info", Colors.STATUS_INFO));
        content.add(createColorBox("Surface", Colors.SURFACE));
        content.add(createColorBox("Text Pri", Colors.TEXT_PRIMARY));
        content.add(createColorBox("Border", Colors.BORDER));

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createColorBox(String name, Color c) {
        JPanel box = new JPanel(new BorderLayout());
        box.setPreferredSize(new Dimension(80, 80));
        box.setBackground(c);
        if (c.equals(Colors.SURFACE) || c.equals(Colors.BACKGROUND)) {
            box.setBorder(javax.swing.BorderFactory.createLineBorder(Colors.BORDER));
        }
        JLabel l = new JLabel(name, javax.swing.SwingConstants.CENTER);
        l.setFont(Typography.CAPTION);
        l.setForeground(isDark(c) ? Colors.TEXT_ON_PRIMARY : Colors.TEXT_PRIMARY);
        box.add(l, BorderLayout.CENTER);
        return box;
    }
    
    private boolean isDark(Color c) {
        double brightness = (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000;
        return brightness < 128;
    }

    private JPanel createButtonsSection() {
        JPanel p = createSection("Buttons");
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, Spacing.MD));
        content.setOpaque(false);

        content.add(new AppButton("Primary Button", AppButton.ButtonStyle.PRIMARY));
        content.add(new AppButton("Secondary Button", AppButton.ButtonStyle.SECONDARY));
        content.add(new AppButton("Danger Button", AppButton.ButtonStyle.DANGER));
        content.add(new AppButton("Ghost Button", AppButton.ButtonStyle.GHOST));
        
        AppButton dis = new AppButton("Disabled Button");
        dis.setEnabled(false);
        content.add(dis);

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatusSection() {
        JPanel p = createSection("Status Badges");
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, Spacing.MD));
        content.setOpaque(false);

        content.add(new StatusBadge("ACTIVE", StatusBadge.StatusType.SUCCESS));
        content.add(new StatusBadge("SCHEDULED", StatusBadge.StatusType.INFO));
        content.add(new StatusBadge("IN PROGRESS", StatusBadge.StatusType.WARNING));
        content.add(new StatusBadge("COMPLETED", StatusBadge.StatusType.SUCCESS));
        content.add(new StatusBadge("EXPIRED", StatusBadge.StatusType.ERROR));
        content.add(new StatusBadge("CANCELLED", StatusBadge.StatusType.ERROR));
        content.add(new StatusBadge("PENDING", StatusBadge.StatusType.WARNING));

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createInputsSection() {
        JPanel p = createSection("Inputs");
        JPanel content = new JPanel(new GridLayout(2, 2, Spacing.MD, Spacing.MD));
        content.setOpaque(false);

        SearchField tf = new SearchField(20);
        tf.setText("Standard text field");
        
        JPasswordField pf = new JPasswordField(20);
        pf.setText("password123");
        pf.setFont(Typography.BODY);
        pf.setPreferredSize(new Dimension(200, Dimensions.INPUT_HEIGHT));
        pf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(Colors.BORDER),
            new EmptyBorder(0, Spacing.MD, 0, Spacing.MD)
        ));
        
        JComboBox<String> cb = new JComboBox<>(new String[]{"Option 1", "Option 2"});
        cb.setFont(Typography.BODY);
        cb.setPreferredSize(new Dimension(200, Dimensions.INPUT_HEIGHT));

        content.add(tf);
        content.add(pf);
        content.add(cb);

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCardsSection() {
        JPanel p = createSection("Cards");
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, Spacing.MD));
        content.setOpaque(false);

        AppCard card1 = new AppCard();
        card1.setPreferredSize(new Dimension(300, 150));
        card1.setLayout(new BorderLayout());
        JLabel l1 = new JLabel("Standard Card");
        l1.setFont(Typography.H3);
        card1.add(l1, BorderLayout.NORTH);
        JLabel l2 = new JLabel("Used for grouped content.");
        l2.setFont(Typography.BODY);
        l2.setForeground(Colors.TEXT_SECONDARY);
        card1.add(l2, BorderLayout.CENTER);

        StatCard card2 = new StatCard("Total Students", "1,248", "Active in system");
        card2.setPreferredSize(new Dimension(250, 120));

        content.add(card1);
        content.add(card2);

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel createTableSection() {
        JPanel p = createSection("Table");
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Subject", "Status"}, 0);
        model.addRow(new Object[]{"1", "John Doe", "Mathematics", "COMPLETED"});
        model.addRow(new Object[]{"2", "Jane Smith", "Physics", "IN PROGRESS"});
        model.addRow(new Object[]{"3", "Alice Johnson", "Chemistry", "SCHEDULED"});

        AppTable table = new AppTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(800, 200));
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(Colors.BORDER));
        sp.getViewport().setBackground(Colors.SURFACE);

        p.add(sp, BorderLayout.CENTER);
        return p;
    }
}
