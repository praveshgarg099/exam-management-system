package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AdminResultsView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final ResultService resultService;
    private final ExamService examService;
    private final SubjectService subjectService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<ResultsPayload, Void> activeWorker = null;

    private SearchField searchField;
    private JComboBox<String> subjectCombo;
    private JComboBox<String> statusCombo;
    private AppButton refreshBtn;
    private AppButton viewDetailsBtn;
    private AppButton deleteBtn;
    private JLabel timestampLabel;

    private DefaultTableModel tableModel;
    private AppTable table;
    private List<ExamResult> displayedResults = new ArrayList<>();
    private List<Subject> allSubjects = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private static class ResultsPayload {
        final List<ExamResult> results;
        final List<Subject> subjects;

        ResultsPayload(List<ExamResult> results, List<Subject> subjects) {
            this.results = results;
            this.subjects = subjects;
        }
    }

    public AdminResultsView(UserSession session) {
        this(session, null);
    }

    public AdminResultsView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.resultService = new ResultService();
        this.examService = new ExamService();
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

        JLabel titleLabel = new JLabel("Examination Results & Analytics");
        titleLabel.setFont(Typography.H1);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Audit student exam submissions, verify passing percentages, and inspect attempt question keys.");
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

        // 2. Multi-Criteria Filter Bar
        JPanel toolbarPanel = new JPanel(new BorderLayout(Spacing.MD, 0));
        toolbarPanel.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.SM, 0));
        leftFilters.setOpaque(false);

        searchField = new SearchField("Search student name...");
        searchField.setPreferredSize(new Dimension(240, Dimensions.BUTTON_HEIGHT));
        searchField.addActionListener(e -> refreshData());
        leftFilters.add(searchField);

        subjectCombo = new JComboBox<>(new String[]{"All Subjects"});
        subjectCombo.setPreferredSize(new Dimension(160, Dimensions.BUTTON_HEIGHT));
        subjectCombo.addActionListener(e -> refreshData());
        leftFilters.add(subjectCombo);

        statusCombo = new JComboBox<>(new String[]{"All Outcomes", "Pass", "Fail"});
        statusCombo.setPreferredSize(new Dimension(140, Dimensions.BUTTON_HEIGHT));
        statusCombo.addActionListener(e -> refreshData());
        leftFilters.add(statusCombo);

        toolbarPanel.add(leftFilters, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.SM, 0));
        rightButtons.setOpaque(false);

        viewDetailsBtn = new AppButton("View Attempt Details", AppButton.ButtonStyle.PRIMARY);
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> openAttemptDetailsDialog());

        deleteBtn = new AppButton("Delete Result", AppButton.ButtonStyle.DANGER);
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteSelectedResult());

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        rightButtons.add(viewDetailsBtn);
        rightButtons.add(deleteBtn);
        rightButtons.add(refreshBtn);
        toolbarPanel.add(rightButtons, BorderLayout.EAST);

        contentPanel.add(toolbarPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.LG));

        // 3. Results Table Card
        AppCard tableCard = new AppCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Result ID", "Attempt ID", "Student Name", "Subject", "Total Qs", "Score", "Percentage", "Status", "Submitted At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new AppTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        // Status renderer for StatusBadge
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
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
                viewDetailsBtn.setEnabled(hasSel);
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

        final String studentQuery = searchField.getText().trim();
        final String subFilter = (String) subjectCombo.getSelectedItem();
        final String statusFilter = (String) statusCombo.getSelectedItem();

        refreshBtn.setEnabled(false);
        refreshBtn.setText("Loading...");
        timestampLabel.setText("Updating...");

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        activeWorker = new SwingWorker<>() {
            @Override
            protected ResultsPayload doInBackground() throws Exception {
                List<Subject> subjects = subjectService.getAllSubjects(session);

                String chosenSub = (subFilter != null && !subFilter.equals("All Subjects")) ? subFilter : null;
                String chosenStatus = (statusFilter != null && !statusFilter.equals("All Outcomes")) ? statusFilter : null;

                List<ExamResult> results = resultService.searchAndFilter(session, studentQuery, chosenSub, chosenStatus);
                return new ResultsPayload(results, subjects);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return;
                }
                try {
                    ResultsPayload payload = get();
                    displayedResults = payload.results;
                    allSubjects = payload.subjects;

                    // Populate subject combo once
                    if (subjectCombo.getItemCount() <= 1 && !allSubjects.isEmpty()) {
                        for (Subject s : allSubjects) {
                            subjectCombo.addItem(s.getName());
                        }
                    }

                    tableModel.setRowCount(0);
                    for (ExamResult er : displayedResults) {
                        boolean isPass = "Pass".equalsIgnoreCase(er.getResult());
                        StatusBadge badge = isPass
                                ? new StatusBadge("Pass", StatusBadge.StatusType.SUCCESS)
                                : new StatusBadge("Fail", StatusBadge.StatusType.ERROR);

                        String dateStr = er.getSubmittedAt() != null ? DATE_FORMATTER.format(er.getSubmittedAt()) : "—";
                        String attemptDisplay = er.getAttemptId() != null ? "#" + er.getAttemptId() : "Historical";

                        tableModel.addRow(new Object[]{
                                er.getId(),
                                attemptDisplay,
                                er.getStudentName() != null ? er.getStudentName() : "Student #" + er.getStudentId(),
                                er.getSubjectName() != null ? er.getSubjectName() : "Subject #" + er.getSubjectId(),
                                er.getTotalQuestions(),
                                String.format("%.0f", er.getMarks()),
                                String.format("%.2f%%", er.getPercentage()),
                                badge,
                                dateStr
                        });
                    }

                    timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    timestampLabel.setText("Failed to load results");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                    viewDetailsBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                }
            }
        };
        activeWorker.execute();
    }

    private ExamResult getSelectedResult() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedResults.size()) {
            return displayedResults.get(row);
        }
        return null;
    }

    private void openAttemptDetailsDialog() {
        ExamResult er = getSelectedResult();
        if (er == null) return;

        if (er.getAttemptId() == null) {
            JOptionPane.showMessageDialog(this,
                    "This record represents migrated historical data from the legacy system.\nDetailed question-level breakdown is not available for historical results.",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Attempt Breakdown — Result #" + er.getId() + " (" + er.getStudentName() + ")", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colors.BACKGROUND);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        // Header summary
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, Spacing.SM));
        summary.setOpaque(false);
        summary.add(new JLabel("Student: " + er.getStudentName()));
        summary.add(new JLabel(" | Subject: " + er.getSubjectName()));
        summary.add(new JLabel(" | Score: " + er.getCorrectAnswers() + " / " + er.getTotalQuestions()));
        summary.add(new JLabel(" | Percentage: " + String.format("%.2f%%", er.getPercentage())));
        panel.add(summary, BorderLayout.NORTH);

        String[] detailCols = {"#", "Question Text", "Student Selected", "Correct Answer", "Evaluation"};
        DefaultTableModel detailModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        AppTable detailTable = new AppTable(detailModel);
        detailTable.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        detailTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                if (val instanceof StatusBadge) {
                    StatusBadge badge = (StatusBadge) val;
                    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                    p.setOpaque(isSel);
                    if (isSel) p.setBackground(t.getSelectionBackground());
                    p.add(badge);
                    return p;
                }
                return super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            }
        });

        try {
            List<ExamAttemptQuestion> attemptQuestions = examService.getAttemptQuestions(session, er.getAttemptId());
            for (ExamAttemptQuestion eq : attemptQuestions) {
                boolean correct = Boolean.TRUE.equals(eq.getIsCorrect());
                StatusBadge badge = correct
                        ? new StatusBadge("Correct", StatusBadge.StatusType.SUCCESS)
                        : new StatusBadge("Incorrect", StatusBadge.StatusType.ERROR);

                String qText = eq.getQuestion() != null ? eq.getQuestion().getQuestionText() : "Question #" + eq.getQuestionId();
                String corrAns = eq.getQuestion() != null ? eq.getQuestion().getCorrectAnswer() : "—";
                String userAns = eq.getSelectedOption() != null ? eq.getSelectedOption() : "(No Answer)";

                detailModel.addRow(new Object[]{
                        eq.getQuestionOrder(),
                        qText,
                        userAns,
                        corrAns,
                        badge
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading attempt breakdown: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        panel.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        AppButton closeBtn = new AppButton("Close", AppButton.ButtonStyle.SECONDARY);
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bPanel.setOpaque(false);
        bPanel.add(closeBtn);
        panel.add(bPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelectedResult() {
        ExamResult er = getSelectedResult();
        if (er == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Result #" + er.getId() + " for student '" + er.getStudentName() + "'?",
                "Confirm Result Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                resultService.deleteResultById(session, er.getId());
                JOptionPane.showMessageDialog(this, "Result deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
