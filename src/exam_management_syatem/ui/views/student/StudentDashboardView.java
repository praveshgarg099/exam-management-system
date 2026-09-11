package exam_management_syatem.ui.views.student;

import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.components.StatCard;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Icons;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;
import exam_management_syatem.ui.shell.NavigationController;

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
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StudentDashboardView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;

    private final StudentService studentService;
    private final ExamService examService;
    private final ResultService resultService;
    private final SubjectService subjectService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<DashboardData, Void> activeWorker = null;

    // Header Components
    private JLabel greetingLabel;
    private JLabel studentMetaLabel;
    private JLabel timestampLabel;
    private AppButton refreshBtn;

    // 4 Telemetry StatCards
    private StatCard availableExamsCard;
    private StatCard examsCompletedCard;
    private StatCard avgScoreCard;
    private StatCard passingRateCard;

    // Active Exams Table (Preview)
    private DefaultTableModel activeExamsModel;
    private AppTable activeExamsTable;
    private AppButton launchExamBtn;
    private List<StudentMyExamsView.ScheduleItem> currentAvailableItems = new ArrayList<>();

    // Recent Results Table (Preview)
    private DefaultTableModel recentResultsModel;
    private AppTable recentResultsTable;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private static class DashboardData {
        final Student student;
        final int availableCount;
        final int completedCount;
        final double avgScore;
        final boolean hasScore;
        final double passingRate;
        final boolean hasPassingRate;
        final int passedCount;
        final int failedCount;
        final List<StudentMyExamsView.ScheduleItem> activeSchedules;
        final List<ExamResult> recentResults;

        DashboardData(Student student, int availableCount, int completedCount, double avgScore, boolean hasScore,
                      double passingRate, boolean hasPassingRate, int passedCount, int failedCount,
                      List<StudentMyExamsView.ScheduleItem> activeSchedules, List<ExamResult> recentResults) {
            this.student = student;
            this.availableCount = availableCount;
            this.completedCount = completedCount;
            this.avgScore = avgScore;
            this.hasScore = hasScore;
            this.passingRate = passingRate;
            this.hasPassingRate = hasPassingRate;
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.activeSchedules = activeSchedules;
            this.recentResults = recentResults;
        }
    }

    public StudentDashboardView(UserSession session) {
        this(session, null);
    }

    public StudentDashboardView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;

        this.studentService = new StudentService();
        this.examService = new ExamService();
        this.resultService = new ResultService();
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

        // 1. Header & Welcome Banner
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // 2. 4 Personal Telemetry KPI Cards
        contentPanel.add(createKpiRow());
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // 3. Main Dashboard Sections Grid (Active Exams Preview & Recent Results Preview)
        contentPanel.add(createTablesGrid());
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // 4. Quick Actions Panel
        contentPanel.add(createQuickActionsCard());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        greetingLabel = new JLabel("Welcome back, Student");
        greetingLabel.setFont(Typography.H1);
        greetingLabel.setForeground(Colors.TEXT_PRIMARY);

        String username = session != null ? session.getUsername() : "—";
        String studentIdStr = (session != null && session.getStudentId() != null) ? String.valueOf(session.getStudentId()) : "—";
        studentMetaLabel = new JLabel("Student ID: #" + studentIdStr + "  •  Username: " + username + "  •  Account Status: Active");
        studentMetaLabel.setFont(Typography.BODY);
        studentMetaLabel.setForeground(Colors.TEXT_MUTED);

        titleBlock.add(greetingLabel);
        titleBlock.add(Box.createVerticalStrut(Spacing.XS));
        titleBlock.add(studentMetaLabel);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.MD, 0));
        controls.setOpaque(false);

        timestampLabel = new JLabel("Refreshing...");
        timestampLabel.setFont(Typography.CAPTION);
        timestampLabel.setForeground(Colors.TEXT_MUTED);
        controls.add(timestampLabel);

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());
        controls.add(refreshBtn);

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, Spacing.LG, Spacing.LG));
        row.setOpaque(false);

        availableExamsCard = new StatCard("Available Exams", "—", "Ready to start or resume");
        examsCompletedCard = new StatCard("Exams Completed", "—", "Graded assessments");
        avgScoreCard = new StatCard("Average Score", "—", "Across completed exams");
        passingRateCard = new StatCard("Passing Rate", "—", "Passed vs failed");

        row.add(availableExamsCard);
        row.add(examsCompletedCard);
        row.add(avgScoreCard);
        row.add(passingRateCard);

        return row;
    }

    private JPanel createTablesGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 2, Spacing.XL, Spacing.XL));
        grid.setOpaque(false);

        // Left: Active & Upcoming Exams Card
        AppCard examsCard = new AppCard();
        examsCard.setLayout(new BorderLayout(Spacing.MD, Spacing.MD));

        JPanel examsCardHeader = new JPanel(new BorderLayout());
        examsCardHeader.setOpaque(false);
        JLabel examsTitle = new JLabel("Active & Upcoming Exams");
        examsTitle.setFont(Typography.H2);
        examsTitle.setForeground(Colors.TEXT_PRIMARY);
        examsCardHeader.add(examsTitle, BorderLayout.WEST);

        if (navigationController != null) {
            AppButton viewAllExamsBtn = new AppButton("View All Exams ->", AppButton.ButtonStyle.GHOST);
            viewAllExamsBtn.setFont(Typography.CAPTION);
            viewAllExamsBtn.addActionListener(e -> navigationController.navigateTo("STUDENT_MY_EXAMS"));
            examsCardHeader.add(viewAllExamsBtn, BorderLayout.EAST);
        }
        examsCard.add(examsCardHeader, BorderLayout.NORTH);

        activeExamsModel = new DefaultTableModel(new String[]{"Schedule ID", "Subject", "Duration", "Passing %", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        activeExamsTable = new AppTable(activeExamsModel);
        activeExamsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeExamsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof StatusBadge) return (StatusBadge) value;
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });
        activeExamsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateLaunchButtonState();
            }
        });

        JScrollPane examsScroll = new JScrollPane(activeExamsTable);
        examsScroll.setBorder(null);
        examsScroll.getViewport().setBackground(Colors.SURFACE);
        examsScroll.setPreferredSize(new Dimension(0, 240));
        examsCard.add(examsScroll, BorderLayout.CENTER);

        JPanel examsBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        examsBottom.setOpaque(false);
        launchExamBtn = new AppButton("Start Exam", AppButton.ButtonStyle.PRIMARY);
        launchExamBtn.setEnabled(false);
        launchExamBtn.addActionListener(e -> launchSelectedExam());
        examsBottom.add(launchExamBtn);
        examsCard.add(examsBottom, BorderLayout.SOUTH);

        grid.add(examsCard);

        // Right: Recent Examination Activity Card
        AppCard resultsCard = new AppCard();
        resultsCard.setLayout(new BorderLayout(Spacing.MD, Spacing.MD));

        JPanel resultsCardHeader = new JPanel(new BorderLayout());
        resultsCardHeader.setOpaque(false);
        JLabel resultsTitle = new JLabel("Recent Examination Activity");
        resultsTitle.setFont(Typography.H2);
        resultsTitle.setForeground(Colors.TEXT_PRIMARY);
        resultsCardHeader.add(resultsTitle, BorderLayout.WEST);

        if (navigationController != null) {
            AppButton viewAllResultsBtn = new AppButton("View All Results ->", AppButton.ButtonStyle.GHOST);
            viewAllResultsBtn.setFont(Typography.CAPTION);
            viewAllResultsBtn.addActionListener(e -> navigationController.navigateTo("STUDENT_MY_RESULTS"));
            resultsCardHeader.add(viewAllResultsBtn, BorderLayout.EAST);
        }
        resultsCard.add(resultsCardHeader, BorderLayout.NORTH);

        recentResultsModel = new DefaultTableModel(new String[]{"Result ID", "Subject", "Score", "Result", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentResultsTable = new AppTable(recentResultsModel);
        recentResultsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof StatusBadge) return (StatusBadge) value;
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });

        JScrollPane resultsScroll = new JScrollPane(recentResultsTable);
        resultsScroll.setBorder(null);
        resultsScroll.getViewport().setBackground(Colors.SURFACE);
        resultsScroll.setPreferredSize(new Dimension(0, 280));
        resultsCard.add(resultsScroll, BorderLayout.CENTER);

        grid.add(resultsCard);

        return grid;
    }

    private JPanel createQuickActionsCard() {
        AppCard card = new AppCard();
        card.setLayout(new BorderLayout(Spacing.MD, Spacing.MD));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(Typography.H2);
        title.setForeground(Colors.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, 0));
        actionsRow.setOpaque(false);

        if (navigationController != null) {
            AppButton examsNavBtn = new AppButton("Go to My Examinations", AppButton.ButtonStyle.SECONDARY);
            examsNavBtn.addActionListener(e -> navigationController.navigateTo("STUDENT_MY_EXAMS"));
            actionsRow.add(examsNavBtn);

            AppButton resultsNavBtn = new AppButton("View Full Results History", AppButton.ButtonStyle.SECONDARY);
            resultsNavBtn.addActionListener(e -> navigationController.navigateTo("STUDENT_MY_RESULTS"));
            actionsRow.add(resultsNavBtn);

            AppButton profileNavBtn = new AppButton("Account Profile & Password", AppButton.ButtonStyle.SECONDARY);
            profileNavBtn.addActionListener(e -> navigationController.navigateTo("STUDENT_PROFILE"));
            actionsRow.add(profileNavBtn);
        } else {
            JLabel noNavLbl = new JLabel("Standalone Dashboard Mode Active");
            noNavLbl.setFont(Typography.CAPTION);
            noNavLbl.setForeground(Colors.TEXT_MUTED);
            actionsRow.add(noNavLbl);
        }

        card.add(actionsRow, BorderLayout.CENTER);
        return card;
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
            protected DashboardData doInBackground() throws Exception {
                // Exactly 5 batch queries
                Student student = studentService.getStudentById(session, session.getStudentId());
                List<Subject> subjects = subjectService.getActiveSubjects(session);
                List<ExamSchedule> schedules = examService.getSchedulesByStudentId(session, session.getStudentId());
                List<ExamAttempt> attempts = examService.getAttemptsByStudent(session, session.getStudentId());
                List<ExamResult> results = resultService.getResultsByStudent(session, session.getStudentId());

                // Build O(1) in-memory lookup map for subjects
                Map<Integer, String> sMap = new HashMap<>();
                for (Subject s : subjects) {
                    sMap.put(s.getId(), s.getName());
                }

                // Build O(1) in-memory lookup map for attempts by schedule ID
                Map<Integer, ExamAttempt> attemptBySchedule = new HashMap<>();
                for (ExamAttempt a : attempts) {
                    if (a.getExamScheduleId() != null) {
                        attemptBySchedule.put(a.getExamScheduleId(), a);
                    }
                }

                // Compute KPI: Available Exams
                List<StudentMyExamsView.ScheduleItem> activeItems = new ArrayList<>();
                int availableCount = 0;
                for (ExamSchedule es : schedules) {
                    String subName = sMap.getOrDefault(es.getSubjectId(), "Subject #" + es.getSubjectId());
                    ExamAttempt att = attemptBySchedule.get(es.getId());
                    StudentMyExamsView.ScheduleItem item = new StudentMyExamsView.ScheduleItem(es, att, subName);

                    // Count available only if canLaunch (Not Started or In Progress not expired)
                    if (item.canLaunch) {
                        availableCount++;
                        activeItems.add(item);
                    }
                }

                // Compute KPI: Exams Completed
                int completedCount = results.size();

                // Compute KPI: Average Score
                double totalScore = 0.0;
                boolean hasScore = !results.isEmpty();
                for (ExamResult er : results) {
                    totalScore += er.getPercentage();
                }
                double avgScore = hasScore ? (totalScore / results.size()) : 0.0;

                // Compute KPI: Passing Rate
                int passedCount = 0;
                int failedCount = 0;
                for (ExamResult er : results) {
                    if ("PASS".equalsIgnoreCase(er.getResult())) {
                        passedCount++;
                    } else if ("FAIL".equalsIgnoreCase(er.getResult())) {
                        failedCount++;
                    }
                }
                int gradedCount = passedCount + failedCount;
                boolean hasPassingRate = gradedCount > 0;
                double passingRate = hasPassingRate ? ((passedCount * 100.0) / gradedCount) : 0.0;

                // Recent results (max 5)
                List<ExamResult> recent = new ArrayList<>();
                for (int i = 0; i < Math.min(5, results.size()); i++) {
                    recent.add(results.get(i));
                }

                return new DashboardData(student, availableCount, completedCount, avgScore, hasScore,
                        passingRate, hasPassingRate, passedCount, failedCount, activeItems, recent);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return; // Drop stale response
                }
                try {
                    if (!isCancelled()) {
                        DashboardData data = get();

                        // 1. Update Header
                        if (data.student != null && data.student.getName() != null) {
                            greetingLabel.setText("Welcome back, " + data.student.getName());
                        }

                        // 2. Update KPI StatCards
                        availableExamsCard.setValue(data.availableCount + " Ready");
                        availableExamsCard.setSubtitle(data.availableCount == 1 ? "1 exam ready to take" : data.availableCount + " exams ready to take");

                        examsCompletedCard.setValue(String.valueOf(data.completedCount));
                        examsCompletedCard.setSubtitle(data.completedCount == 1 ? "1 assessment completed" : data.completedCount + " assessments completed");

                        if (data.hasScore) {
                            avgScoreCard.setValue(String.format("%.1f%%", data.avgScore));
                            avgScoreCard.setSubtitle("Across " + data.completedCount + " exams");
                        } else {
                            avgScoreCard.setValue("N/A");
                            avgScoreCard.setSubtitle("No exams completed");
                        }

                        if (data.hasPassingRate) {
                            passingRateCard.setValue(String.format("%.1f%%", data.passingRate));
                            passingRateCard.setSubtitle(data.passedCount + " Passed / " + data.failedCount + " Failed");
                        } else {
                            passingRateCard.setValue("N/A");
                            passingRateCard.setSubtitle("No graded exams");
                        }

                        // 3. Update Active Exams Table Preview (max 5 items)
                        currentAvailableItems = data.activeSchedules;
                        activeExamsModel.setRowCount(0);
                        int previewLimit = Math.min(5, data.activeSchedules.size());
                        for (int i = 0; i < previewLimit; i++) {
                            StudentMyExamsView.ScheduleItem item = data.activeSchedules.get(i);
                            StatusBadge badge = new StatusBadge(item.statusText, item.statusType);
                            activeExamsModel.addRow(new Object[]{
                                    item.schedule.getId(),
                                    item.subjectName,
                                    item.schedule.getDurationMinutes() + " mins",
                                    String.format("%.0f%%", item.schedule.getPassingPercentage()),
                                    badge
                            });
                        }
                        updateLaunchButtonState();

                        // 4. Update Recent Results Table Preview (max 5 items)
                        recentResultsModel.setRowCount(0);
                        for (ExamResult er : data.recentResults) {
                            boolean isPass = "PASS".equalsIgnoreCase(er.getResult());
                            StatusBadge badge = new StatusBadge(er.getResult(), isPass ? StatusBadge.StatusType.SUCCESS : StatusBadge.StatusType.ERROR);
                            String dateStr = er.getSubmittedAt() != null ? DATE_FORMATTER.format(er.getSubmittedAt()) : "—";
                            recentResultsModel.addRow(new Object[]{
                                    "#" + er.getId(),
                                    er.getSubjectName() != null ? er.getSubjectName() : "Subject #" + er.getSubjectId(),
                                    String.format("%.1f%%", er.getPercentage()),
                                    badge,
                                    dateStr
                            });
                        }

                        timestampLabel.setText("Last updated: " + LocalTime.now().format(TIME_FORMATTER));
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

    private void updateLaunchButtonState() {
        int selectedRow = activeExamsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentAvailableItems.size()) {
            launchExamBtn.setEnabled(false);
            launchExamBtn.setText("Start Exam");
            return;
        }

        StudentMyExamsView.ScheduleItem item = currentAvailableItems.get(selectedRow);
        if (item.canLaunch) {
            launchExamBtn.setEnabled(true);
            launchExamBtn.setText(item.actionLabel);
        } else {
            launchExamBtn.setEnabled(false);
            launchExamBtn.setText(item.statusText);
        }
    }

    private void launchSelectedExam() {
        int selectedRow = activeExamsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentAvailableItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an examination first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StudentMyExamsView.ScheduleItem item = currentAvailableItems.get(selectedRow);
        if (!item.canLaunch) {
            JOptionPane.showMessageDialog(this, "This examination cannot be launched.", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Launch examination for '" + item.subjectName + "'?\n\n"
                        + "Duration: " + item.schedule.getDurationMinutes() + " minutes\n"
                        + "Questions: " + item.schedule.getTotalQuestions() + "\n"
                        + "Passing Score: " + item.schedule.getPassingPercentage() + "%\n\n"
                        + "The exam timer starts immediately upon opening.",
                item.actionLabel + " Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        launchExamBtn.setEnabled(false);
        launchExamBtn.setText("Launching...");

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
                    java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(StudentDashboardView.this);
                    if (win != null) {
                        win.dispose();
                    }
                    exam_management_syatem.ui.views.exam.testtake.mainWithSessionAndSchedule(session, item.schedule);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StudentDashboardView.this, msg, "Exam Launch Failed", JOptionPane.ERROR_MESSAGE);
                    refreshData();
                }
            }
        };
        launcher.execute();
    }
}
