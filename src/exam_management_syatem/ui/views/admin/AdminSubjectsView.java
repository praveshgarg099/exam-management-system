package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.QuestionService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.components.AppTextField;
import exam_management_syatem.ui.components.SearchField;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;
import exam_management_syatem.ui.shell.NavigationController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AdminSubjectsView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final SubjectService subjectService;
    private final QuestionService questionService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<SubjectData, Void> activeWorker = null;

    private SearchField searchField;
    private AppButton refreshBtn;
    private AppButton addBtn;
    private AppButton renameBtn;
    private AppButton toggleStatusBtn;
    private JLabel timestampLabel;

    private DefaultTableModel tableModel;
    private AppTable table;
    private List<Subject> displayedSubjects = new ArrayList<>();
    private Map<Integer, Integer> questionCountMap = new HashMap<>();

    private static class SubjectData {
        final List<Subject> subjects;
        final Map<Integer, Integer> counts;

        SubjectData(List<Subject> subjects, Map<Integer, Integer> counts) {
            this.subjects = subjects;
            this.counts = counts;
        }
    }

    public AdminSubjectsView(UserSession session) {
        this(session, null);
    }

    public AdminSubjectsView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.subjectService = new SubjectService();
        this.questionService = new QuestionService();

        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Subject Management");
        titleLabel.setFont(Typography.H1);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Define academic subjects, maintain active course catalogs, and audit question quantities.");
        subtitleLabel.setFont(Typography.BODY);
        subtitleLabel.setForeground(Colors.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(Spacing.XS));
        titleBlock.add(subtitleLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        timestampLabel = new JLabel("Refreshing...");
        timestampLabel.setFont(Typography.CAPTION);
        timestampLabel.setForeground(Colors.TEXT_MUTED);
        headerPanel.add(timestampLabel, BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // 2. Toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout(Spacing.MD, 0));
        toolbarPanel.setOpaque(false);

        searchField = new SearchField("Filter subjects by name...");
        searchField.setPreferredSize(new Dimension(360, Dimensions.BUTTON_HEIGHT));
        searchField.addActionListener(e -> filterTableLocally());
        toolbarPanel.add(searchField, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.SM, 0));
        btnGroup.setOpaque(false);

        addBtn = new AppButton("+ Add Subject", AppButton.ButtonStyle.PRIMARY);
        addBtn.addActionListener(e -> openAddSubjectDialog());

        renameBtn = new AppButton("Rename Subject", AppButton.ButtonStyle.SECONDARY);
        renameBtn.setEnabled(false);
        renameBtn.addActionListener(e -> openRenameSubjectDialog());

        toggleStatusBtn = new AppButton("Toggle Active Status", AppButton.ButtonStyle.SECONDARY);
        toggleStatusBtn.setEnabled(false);
        toggleStatusBtn.addActionListener(e -> toggleSubjectStatus());

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        btnGroup.add(addBtn);
        btnGroup.add(renameBtn);
        btnGroup.add(toggleStatusBtn);
        btnGroup.add(refreshBtn);
        toolbarPanel.add(btnGroup, BorderLayout.EAST);

        contentPanel.add(toolbarPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.LG));

        // 3. Card & Table
        AppCard tableCard = new AppCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Subject Name", "Available Questions", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new AppTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof StatusBadge) {
                    StatusBadge badge = (StatusBadge) value;
                    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                    p.setOpaque(isSelected);
                    if (isSelected) p.setBackground(t.getSelectionBackground());
                    p.add(badge);
                    return p;
                }
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSel = table.getSelectedRow() >= 0;
                renameBtn.setEnabled(hasSel);
                toggleStatusBtn.setEnabled(hasSel);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 1));
        scrollPane.getViewport().setBackground(Colors.SURFACE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tableCard);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        final long token = refreshGeneration.incrementAndGet();

        refreshBtn.setEnabled(false);
        refreshBtn.setText("Loading...");
        timestampLabel.setText("Updating...");

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        activeWorker = new SwingWorker<>() {
            @Override
            protected SubjectData doInBackground() throws Exception {
                List<Subject> all = subjectService.getAllSubjects(session);
                Map<Integer, Integer> counts = questionService.getQuestionCountsBySubject(session);
                return new SubjectData(all, counts);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return;
                }
                try {
                    SubjectData data = get();
                    displayedSubjects = data.subjects;
                    questionCountMap = data.counts;
                    filterTableLocally();

                    timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    timestampLabel.setText("Failed to load subjects");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                    renameBtn.setEnabled(false);
                    toggleStatusBtn.setEnabled(false);
                }
            }
        };
        activeWorker.execute();
    }

    private void filterTableLocally() {
        String filter = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Subject s : displayedSubjects) {
            if (!filter.isEmpty() && !s.getName().toLowerCase().contains(filter)) {
                continue;
            }

            int count = questionCountMap.getOrDefault(s.getId(), 0);
            StatusBadge badge = s.isActive()
                    ? new StatusBadge("Active", StatusBadge.StatusType.SUCCESS)
                    : new StatusBadge("Inactive", StatusBadge.StatusType.ERROR);

            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    count + " questions",
                    badge
            });
        }
    }

    private Subject getSelectedSubject() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            int id = (int) tableModel.getValueAt(row, 0);
            for (Subject s : displayedSubjects) {
                if (s.getId() == id) {
                    return s;
                }
            }
        }
        return null;
    }

    private void openAddSubjectDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Subject", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(2, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField nameField = new AppTextField();
        panel.add(new JLabel("Subject Name:"));
        panel.add(nameField);

        AppButton submitBtn = new AppButton("Add Subject", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            try {
                subjectService.addSubject(session, name);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Subject '" + name + "' added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error Adding Subject", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(cancelBtn);
        panel.add(submitBtn);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void openRenameSubjectDialog() {
        Subject s = getSelectedSubject();
        if (s == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Rename Subject — " + s.getName(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(2, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField nameField = new AppTextField();
        nameField.setText(s.getName());
        panel.add(new JLabel("New Name:"));
        panel.add(nameField);

        AppButton submitBtn = new AppButton("Save", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            try {
                subjectService.updateSubject(session, s.getId(), newName);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Subject renamed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Rename Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(cancelBtn);
        panel.add(submitBtn);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void toggleSubjectStatus() {
        Subject s = getSelectedSubject();
        if (s == null) return;

        boolean newStatus = !s.isActive();
        String action = newStatus ? "activate" : "deactivate";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " subject '" + s.getName() + "'?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                subjectService.setSubjectActive(session, s.getId(), newStatus);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Action Prevented", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
