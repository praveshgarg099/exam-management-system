package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.Question;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

public class AdminQuestionsView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final QuestionService questionService;
    private final SubjectService subjectService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<QuestionData, Void> activeWorker = null;

    private JComboBox<String> subjectFilterCombo;
    private SearchField searchField;
    private AppButton refreshBtn;
    private AppButton addBtn;
    private AppButton editBtn;
    private AppButton toggleStatusBtn;
    private AppButton deleteBtn;
    private JLabel timestampLabel;

    private DefaultTableModel tableModel;
    private AppTable table;
    private List<Question> displayedQuestions = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();
    private Map<Integer, String> subjectNameMap = new HashMap<>();

    private static class QuestionData {
        final List<Subject> subjects;
        final List<Question> questions;

        QuestionData(List<Subject> subjects, List<Question> questions) {
            this.subjects = subjects;
            this.questions = questions;
        }
    }

    public AdminQuestionsView(UserSession session) {
        this(session, null);
    }

    public AdminQuestionsView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.questionService = new QuestionService();
        this.subjectService = new SubjectService();

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

        JLabel titleLabel = new JLabel("Question Repository");
        titleLabel.setFont(Typography.H1);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Curate multiple-choice questions, set difficulty tiers, and configure authoritative answer keys.");
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

        // 2. Filter & Action Toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout(Spacing.MD, 0));
        toolbarPanel.setOpaque(false);

        JPanel leftFilterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.SM, 0));
        leftFilterGroup.setOpaque(false);

        subjectFilterCombo = new JComboBox<>(new String[]{"All Subjects"});
        subjectFilterCombo.setPreferredSize(new Dimension(180, Dimensions.BUTTON_HEIGHT));
        subjectFilterCombo.addActionListener(e -> filterTableLocally());
        leftFilterGroup.add(subjectFilterCombo);

        searchField = new SearchField("Search questions or answers...");
        searchField.setPreferredSize(new Dimension(280, Dimensions.BUTTON_HEIGHT));
        searchField.addActionListener(e -> filterTableLocally());
        leftFilterGroup.add(searchField);

        toolbarPanel.add(leftFilterGroup, BorderLayout.WEST);

        JPanel rightBtnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.SM, 0));
        rightBtnGroup.setOpaque(false);

        addBtn = new AppButton("+ Add Question", AppButton.ButtonStyle.PRIMARY);
        addBtn.addActionListener(e -> openAddQuestionDialog());

        editBtn = new AppButton("Edit Question", AppButton.ButtonStyle.SECONDARY);
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> openEditQuestionDialog());

        toggleStatusBtn = new AppButton("Toggle Status", AppButton.ButtonStyle.SECONDARY);
        toggleStatusBtn.setEnabled(false);
        toggleStatusBtn.addActionListener(e -> toggleQuestionStatus());

        deleteBtn = new AppButton("Delete", AppButton.ButtonStyle.DANGER);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteSelectedQuestion());

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        rightBtnGroup.add(addBtn);
        rightBtnGroup.add(editBtn);
        rightBtnGroup.add(toggleStatusBtn);
        rightBtnGroup.add(deleteBtn);
        rightBtnGroup.add(refreshBtn);
        toolbarPanel.add(rightBtnGroup, BorderLayout.EAST);

        contentPanel.add(toolbarPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.LG));

        // 3. Question Table Card
        AppCard tableCard = new AppCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Subject", "Question Statement", "Option A", "Option B", "Option C", "Option D", "Correct Answer", "Difficulty", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new AppTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        // Status badge renderer for Status column
        table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
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
                editBtn.setEnabled(hasSel);
                toggleStatusBtn.setEnabled(hasSel);
                deleteBtn.setEnabled(hasSel);
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
            protected QuestionData doInBackground() throws Exception {
                List<Subject> subjects = subjectService.getAllSubjects(session);
                List<Question> questions = new ArrayList<>();
                for (Subject s : subjects) {
                    try {
                        questions.addAll(questionService.getQuestionsBySubjectId(session, s.getId()));
                    } catch (Exception ignored) {}
                }
                return new QuestionData(subjects, questions);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return;
                }
                try {
                    QuestionData data = get();
                    allSubjects = data.subjects;
                    displayedQuestions = data.questions;

                    subjectNameMap.clear();
                    subjectFilterCombo.removeAllItems();
                    subjectFilterCombo.addItem("All Subjects");
                    for (Subject s : allSubjects) {
                        subjectNameMap.put(s.getId(), s.getName());
                        subjectFilterCombo.addItem(s.getName());
                    }

                    filterTableLocally();
                    timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    timestampLabel.setText("Failed to load questions");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                    editBtn.setEnabled(false);
                    toggleStatusBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                }
            }
        };
        activeWorker.execute();
    }

    private void filterTableLocally() {
        String selSubject = (String) subjectFilterCombo.getSelectedItem();
        String search = searchField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);

        for (Question q : displayedQuestions) {
            String subName = subjectNameMap.getOrDefault(q.getSubjectId(), "Subject #" + q.getSubjectId());

            if (selSubject != null && !selSubject.equals("All Subjects") && !subName.equalsIgnoreCase(selSubject)) {
                continue;
            }

            if (!search.isEmpty()) {
                boolean matchText = q.getQuestionText() != null && q.getQuestionText().toLowerCase().contains(search);
                boolean matchAns = q.getCorrectAnswer() != null && q.getCorrectAnswer().toLowerCase().contains(search);
                if (!matchText && !matchAns) {
                    continue;
                }
            }

            StatusBadge badge = q.isActive()
                    ? new StatusBadge("Active", StatusBadge.StatusType.SUCCESS)
                    : new StatusBadge("Inactive", StatusBadge.StatusType.ERROR);

            tableModel.addRow(new Object[]{
                    q.getId(),
                    subName,
                    q.getQuestionText(),
                    q.getOption1(),
                    q.getOption2(),
                    q.getOption3(),
                    q.getOption4(),
                    q.getCorrectAnswer(),
                    q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM",
                    badge
            });
        }
    }

    private Question getSelectedQuestion() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            int qId = (int) tableModel.getValueAt(row, 0);
            for (Question q : displayedQuestions) {
                if (q.getId() == qId) {
                    return q;
                }
            }
        }
        return null;
    }

    private void openAddQuestionDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add Question to Repository", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        JComboBox<String> subCombo = new JComboBox<>();
        for (Subject s : allSubjects) {
            if (s.isActive()) subCombo.addItem(s.getName());
        }

        AppTextField qField = new AppTextField();
        AppTextField o1Field = new AppTextField();
        AppTextField o2Field = new AppTextField();
        AppTextField o3Field = new AppTextField();
        AppTextField o4Field = new AppTextField();
        AppTextField ansField = new AppTextField();
        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD"});
        diffCombo.setSelectedItem("MEDIUM");

        panel.add(new JLabel("Subject:"));
        panel.add(subCombo);
        panel.add(new JLabel("Question Statement:"));
        panel.add(qField);
        panel.add(new JLabel("Option 1:"));
        panel.add(o1Field);
        panel.add(new JLabel("Option 2:"));
        panel.add(o2Field);
        panel.add(new JLabel("Option 3:"));
        panel.add(o3Field);
        panel.add(new JLabel("Option 4:"));
        panel.add(o4Field);
        panel.add(new JLabel("Correct Answer (Must match an option):"));
        panel.add(ansField);
        panel.add(new JLabel("Difficulty Tier:"));
        panel.add(diffCombo);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        AppButton submitBtn = new AppButton("Save Question", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String selectedSubName = (String) subCombo.getSelectedItem();
            if (selectedSubName == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an active subject.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int subId = -1;
            for (Subject s : allSubjects) {
                if (s.getName().equalsIgnoreCase(selectedSubName)) {
                    subId = s.getId();
                    break;
                }
            }

            try {
                questionService.addQuestion(
                        session,
                        subId,
                        qField.getText().trim(),
                        o1Field.getText().trim(),
                        o2Field.getText().trim(),
                        o3Field.getText().trim(),
                        o4Field.getText().trim(),
                        ansField.getText().trim(),
                        (String) diffCombo.getSelectedItem()
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Question added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(submitBtn);
        wrapper.add(bottomPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(wrapper);
        dialog.setVisible(true);
    }

    private void openEditQuestionDialog() {
        Question q = getSelectedQuestion();
        if (q == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Question #" + q.getId(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(560, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField qField = new AppTextField();
        qField.setText(q.getQuestionText());
        AppTextField o1Field = new AppTextField();
        o1Field.setText(q.getOption1());
        AppTextField o2Field = new AppTextField();
        o2Field.setText(q.getOption2());
        AppTextField o3Field = new AppTextField();
        o3Field.setText(q.getOption3());
        AppTextField o4Field = new AppTextField();
        o4Field.setText(q.getOption4());
        AppTextField ansField = new AppTextField();
        ansField.setText(q.getCorrectAnswer());
        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD"});
        diffCombo.setSelectedItem(q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");

        panel.add(new JLabel("Question Statement:"));
        panel.add(qField);
        panel.add(new JLabel("Option 1:"));
        panel.add(o1Field);
        panel.add(new JLabel("Option 2:"));
        panel.add(o2Field);
        panel.add(new JLabel("Option 3:"));
        panel.add(o3Field);
        panel.add(new JLabel("Option 4:"));
        panel.add(o4Field);
        panel.add(new JLabel("Correct Answer:"));
        panel.add(ansField);
        panel.add(new JLabel("Difficulty Tier:"));
        panel.add(diffCombo);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        AppButton submitBtn = new AppButton("Save Changes", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            try {
                questionService.updateQuestion(
                        session,
                        q.getId(),
                        qField.getText().trim(),
                        o1Field.getText().trim(),
                        o2Field.getText().trim(),
                        o3Field.getText().trim(),
                        o4Field.getText().trim(),
                        ansField.getText().trim(),
                        (String) diffCombo.getSelectedItem()
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Question updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        bottomPanel.add(cancelBtn);
        bottomPanel.add(submitBtn);
        wrapper.add(bottomPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(wrapper);
        dialog.setVisible(true);
    }

    private void toggleQuestionStatus() {
        Question q = getSelectedQuestion();
        if (q == null) return;

        boolean newStatus = !q.isActive();
        String action = newStatus ? "activate" : "deactivate";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " Question #" + q.getId() + "?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                questionService.setQuestionActive(session, q.getId(), newStatus);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedQuestion() {
        Question q = getSelectedQuestion();
        if (q == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Question #" + q.getId() + "?\n" +
                        "(If question is referenced by past student exam attempts, it will be safely deactivated to preserve historical accuracy.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                questionService.deleteQuestion(session, q.getId());
                JOptionPane.showMessageDialog(this, "Question processed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
