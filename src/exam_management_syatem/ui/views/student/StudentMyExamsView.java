package exam_management_syatem.ui.views.student;

import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StudentMyExamsView extends JPanel {

    private final UserSession session;
    private final ExamService examService;
    private final SubjectService subjectService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<ExamsData, Void> activeWorker = null;

    private AppButton refreshBtn;
    private JLabel timestampLabel;
    private DefaultTableModel tableModel;
    private AppTable table;
    private JLabel statusLabel;
    private AppButton actionBtn;

    private List<ScheduleItem> displayedItems = new ArrayList<>();
    private Map<Integer, String> subjectMap = new HashMap<>();

    public static class ScheduleItem {
        public final ExamSchedule schedule;
        public final ExamAttempt attempt;
        public final String subjectName;
        public final String statusText;
        public final StatusBadge.StatusType statusType;
        public final boolean canLaunch;
        public final String actionLabel;

        public ScheduleItem(ExamSchedule schedule, ExamAttempt attempt, String subjectName) {
            this.schedule = schedule;
            this.attempt = attempt;
            this.subjectName = subjectName;

            if (attempt == null) {
                this.statusText = "Not Started";
                this.statusType = StatusBadge.StatusType.INFO;
                this.canLaunch = !"CANCELLED".equalsIgnoreCase(schedule.getStatus());
                this.actionLabel = "Start Exam";
            } else if ("IN_PROGRESS".equalsIgnoreCase(attempt.getStatus())) {
                if (attempt.isExpired()) {
                    this.statusText = "Expired (Time Out)";
                    this.statusType = StatusBadge.StatusType.ERROR;
                    this.canLaunch = false;
                    this.actionLabel = "—";
                } else {
                    this.statusText = "In Progress";
                    this.statusType = StatusBadge.StatusType.WARNING;
                    this.canLaunch = true;
                    this.actionLabel = "Resume Exam";
                }
            } else if ("COMPLETED".equalsIgnoreCase(attempt.getStatus())) {
                this.statusText = "Completed";
                this.statusType = StatusBadge.StatusType.SUCCESS;
                this.canLaunch = false;
                this.actionLabel = "—";
            } else if ("EXPIRED".equalsIgnoreCase(attempt.getStatus())) {
                this.statusText = "Expired";
                this.statusType = StatusBadge.StatusType.ERROR;
                this.canLaunch = false;
                this.actionLabel = "—";
            } else {
                this.statusText = attempt.getStatus();
                this.statusType = StatusBadge.StatusType.NEUTRAL;
                this.canLaunch = false;
                this.actionLabel = "—";
            }
        }
    }

    private static class ExamsData {
        final List<ScheduleItem> items;
        final Map<Integer, String> subjects;

        ExamsData(List<ScheduleItem> items, Map<Integer, String> subjects) {
            this.items = items;
            this.subjects = subjects;
        }
    }

    public StudentMyExamsView(UserSession session) {
        this.session = session;
        this.examService = new ExamService();
        this.subjectService = new SubjectService();

        initComponent();
        refreshData();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel pageTitle = new JLabel("My Examinations");
        pageTitle.setFont(Typography.H1);
        pageTitle.setForeground(Colors.TEXT_PRIMARY);

        JLabel pageSubtitle = new JLabel("View your scheduled examinations. Start new sessions or resume ongoing attempts.");
        pageSubtitle.setFont(Typography.BODY);
        pageSubtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(pageTitle);
        titleBlock.add(Box.createVerticalStrut(Spacing.XS));
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Refresh & Metadata Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.MD, 0));
        controls.setOpaque(false);

        timestampLabel = new JLabel("Refreshing...");
        timestampLabel.setFont(Typography.CAPTION);
        timestampLabel.setForeground(Colors.TEXT_MUTED);
        controls.add(timestampLabel);

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());
        controls.add(refreshBtn);
        headerPanel.add(controls, BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // Table Card
        AppCard card = new AppCard();
        card.setLayout(new BorderLayout(Spacing.LG, Spacing.LG));

        String[] cols = {"Schedule ID", "Subject", "Duration", "Questions", "Passing %", "Attempt Status", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new AppTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof StatusBadge) {
                    return (StatusBadge) value;
                }
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtonState();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(Colors.SURFACE);
        tableScroll.setPreferredSize(new Dimension(0, 380));
        card.add(tableScroll, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading scheduled examinations...");
        statusLabel.setFont(Typography.BODY);
        statusLabel.setForeground(Colors.TEXT_MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(new EmptyBorder(Spacing.XXL, 0, Spacing.XXL, 0));

        // Bottom Action Panel
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(Spacing.MD, 0, 0, 0));

        JLabel hintLabel = new JLabel("Select an active exam from the table above to start or resume your session.");
        hintLabel.setFont(Typography.CAPTION);
        hintLabel.setForeground(Colors.TEXT_MUTED);
        actionPanel.add(hintLabel, BorderLayout.WEST);

        actionBtn = new AppButton("Start Exam", AppButton.ButtonStyle.PRIMARY);
        actionBtn.setEnabled(false);
        actionBtn.addActionListener(e -> launchSelectedExam());
        actionPanel.add(actionBtn, BorderLayout.EAST);

        card.add(actionPanel, BorderLayout.SOUTH);
        contentPanel.add(card);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        if (session == null || session.getStudentId() == null) {
            return;
        }

        final long token = refreshGeneration.incrementAndGet();
        refreshBtn.setEnabled(false);
        refreshBtn.setText("Refreshing...");
        timestampLabel.setText("Updating...");

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        activeWorker = new SwingWorker<>() {
            @Override
            protected ExamsData doInBackground() throws Exception {
                // Exactly 3 queries: subjects, schedules, attempts
                List<Subject> subjects = subjectService.getActiveSubjects(session);
                Map<Integer, String> sMap = new HashMap<>();
                for (Subject s : subjects) {
                    sMap.put(s.getId(), s.getName());
                }

                List<ExamSchedule> schedules = examService.getSchedulesByStudentId(session, session.getStudentId());
                List<ExamAttempt> attempts = examService.getAttemptsByStudent(session, session.getStudentId());

                Map<Integer, ExamAttempt> attemptBySchedule = new HashMap<>();
                for (ExamAttempt a : attempts) {
                    if (a.getExamScheduleId() != null) {
                        attemptBySchedule.put(a.getExamScheduleId(), a);
                    }
                }

                List<ScheduleItem> items = new ArrayList<>();
                for (ExamSchedule es : schedules) {
                    String subName = sMap.getOrDefault(es.getSubjectId(), "Subject #" + es.getSubjectId());
                    ExamAttempt att = attemptBySchedule.get(es.getId());
                    items.add(new ScheduleItem(es, att, subName));
                }

                return new ExamsData(items, sMap);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return; // drop stale result
                }
                try {
                    if (!isCancelled()) {
                        ExamsData data = get();
                        displayedItems = data.items;
                        subjectMap = data.subjects;

                        tableModel.setRowCount(0);
                        for (ScheduleItem item : displayedItems) {
                            StatusBadge badge = new StatusBadge(item.statusText, item.statusType);
                            tableModel.addRow(new Object[]{
                                    item.schedule.getId(),
                                    item.subjectName,
                                    item.schedule.getDurationMinutes() + " mins",
                                    item.schedule.getTotalQuestions(),
                                    String.format("%.0f%%", item.schedule.getPassingPercentage()),
                                    badge,
                                    item.actionLabel
                            });
                        }

                        updateActionButtonState();
                        timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                } catch (Exception e) {
                    timestampLabel.setText("Failed to refresh");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                }
            }
        };
        activeWorker.execute();
    }

    private void updateActionButtonState() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedItems.size()) {
            actionBtn.setEnabled(false);
            actionBtn.setText("Start Exam");
            return;
        }

        ScheduleItem item = displayedItems.get(selectedRow);
        if (item.canLaunch) {
            actionBtn.setEnabled(true);
            actionBtn.setText(item.actionLabel);
        } else {
            actionBtn.setEnabled(false);
            actionBtn.setText(item.statusText);
        }
    }

    private void launchSelectedExam() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an examination first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ScheduleItem item = displayedItems.get(selectedRow);
        if (!item.canLaunch) {
            JOptionPane.showMessageDialog(this, "This examination cannot be started or resumed (" + item.statusText + ").", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String confirmMsg = item.attempt == null
                ? "Start examination for '" + item.subjectName + "'?\n\n"
                + "Duration: " + item.schedule.getDurationMinutes() + " minutes\n"
                + "Questions: " + item.schedule.getTotalQuestions() + "\n"
                + "Passing Score: " + item.schedule.getPassingPercentage() + "%\n\n"
                + "The timer will begin immediately upon opening the exam window."
                : "Resume in-progress examination for '" + item.subjectName + "'?\n\n"
                + "Your previously saved answers and remaining time will be restored.";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                confirmMsg,
                item.actionLabel + " Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        actionBtn.setEnabled(false);
        actionBtn.setText("Launching...");

        SwingWorker<ExamAttempt, Void> launcher = new SwingWorker<>() {
            @Override
            protected ExamAttempt doInBackground() throws Exception {
                // Authoritative service call enforces ownership, row-locks, and one-attempt invariant
                return examService.startOrResumeExam(session, item.schedule.getId());
            }

            @Override
            protected void done() {
                try {
                    ExamAttempt authoritativeAttempt = get();
                    // Dispose main window and launch exam taking screen
                    java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(StudentMyExamsView.this);
                    if (win != null) {
                        win.dispose();
                    }
                    exam_management_syatem.ui.views.exam.testtake.mainWithSessionAndSchedule(session, item.schedule);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StudentMyExamsView.this, msg, "Exam Launch Failed", JOptionPane.ERROR_MESSAGE);
                    refreshData();
                }
            }
        };
        launcher.execute();
    }
}
