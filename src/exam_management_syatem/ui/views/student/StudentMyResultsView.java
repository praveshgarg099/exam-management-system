package exam_management_syatem.ui.views.student;

import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class StudentMyResultsView extends JPanel {

    private final UserSession session;
    private final ResultService resultService;
    private final ExamService examService;
    private final SubjectService subjectService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<ResultsData, Void> activeWorker = null;

    private AppButton refreshBtn;
    private JLabel timestampLabel;
    private DefaultTableModel tableModel;
    private AppTable table;
    private JLabel emptyLabel;
    private JScrollPane tableScroll;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    public static class HistoryRow {
        public final String idDisplay;
        public final String subjectName;
        public final String totalQuestions;
        public final String correctAnswers;
        public final String scorePercentage;
        public final String statusText;
        public final StatusBadge.StatusType statusType;
        public final String dateDisplay;

        public HistoryRow(String idDisplay, String subjectName, String totalQuestions, String correctAnswers,
                          String scorePercentage, String statusText, StatusBadge.StatusType statusType, String dateDisplay) {
            this.idDisplay = idDisplay;
            this.subjectName = subjectName;
            this.totalQuestions = totalQuestions;
            this.correctAnswers = correctAnswers;
            this.scorePercentage = scorePercentage;
            this.statusText = statusText;
            this.statusType = statusType;
            this.dateDisplay = dateDisplay;
        }
    }

    private static class ResultsData {
        final List<HistoryRow> rows;

        ResultsData(List<HistoryRow> rows) {
            this.rows = rows;
        }
    }

    public StudentMyResultsView(UserSession session) {
        this.session = session;
        this.resultService = new ResultService();
        this.examService = new ExamService();
        this.subjectService = new SubjectService();

        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel pageTitle = new JLabel("Examination Results");
        pageTitle.setFont(Typography.H1);
        pageTitle.setForeground(Colors.TEXT_PRIMARY);

        JLabel pageSubtitle = new JLabel("Review your graded examination submissions and performance records.");
        pageSubtitle.setFont(Typography.BODY);
        pageSubtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(pageTitle);
        titleBlock.add(Box.createVerticalStrut(Spacing.XS));
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

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

        // Results Card
        AppCard card = new AppCard();
        card.setLayout(new BorderLayout(Spacing.LG, Spacing.LG));

        String[] cols = {"Result ID", "Subject", "Questions", "Correct", "Score", "Status", "Date Completed"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new AppTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if (value instanceof StatusBadge) {
                    return (StatusBadge) value;
                }
                return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            }
        });

        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(Colors.SURFACE);
        tableScroll.setPreferredSize(new Dimension(0, 420));
        card.add(tableScroll, BorderLayout.CENTER);

        emptyLabel = new JLabel("No examination records found.");
        emptyLabel.setFont(Typography.BODY);
        emptyLabel.setForeground(Colors.TEXT_MUTED);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setBorder(new EmptyBorder(Spacing.XXL, 0, Spacing.XXL, 0));

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
            protected ResultsData doInBackground() throws Exception {
                CompletableFuture<List<ExamResult>> resultsFut = CompletableFuture.supplyAsync(() -> {
                    try { return resultService.getResultsByStudent(session, session.getStudentId()); } catch (Exception e) { return Collections.emptyList(); }
                });
                CompletableFuture<List<ExamAttempt>> attemptsFut = CompletableFuture.supplyAsync(() -> {
                    try { return examService.getAttemptsByStudent(session, session.getStudentId()); } catch (Exception e) { return Collections.emptyList(); }
                });
                CompletableFuture<List<Subject>> subjectsFut = CompletableFuture.supplyAsync(() -> {
                    try { return subjectService.getActiveSubjects(session); } catch (Exception e) { return Collections.emptyList(); }
                });

                CompletableFuture.allOf(resultsFut, attemptsFut, subjectsFut).join();

                List<ExamResult> results = resultsFut.join();
                List<ExamAttempt> attempts = attemptsFut.join();
                List<Subject> subjects = subjectsFut.join();

                Map<Integer, String> sMap = new HashMap<>();
                for (Subject s : subjects) {
                    sMap.put(s.getId(), s.getName());
                }

                Set<Integer> coveredAttemptIds = new HashSet<>();
                List<HistoryRow> rows = new ArrayList<>();

                // Add official graded exam results
                for (ExamResult er : results) {
                    if (er.getAttemptId() != null) {
                        coveredAttemptIds.add(er.getAttemptId());
                    }
                    String subName = er.getSubjectName() != null ? er.getSubjectName() : sMap.getOrDefault(er.getSubjectId(), "Subject #" + er.getSubjectId());
                    String dateStr = er.getSubmittedAt() != null ? DATE_FORMATTER.format(er.getSubmittedAt()) : "—";
                    boolean isPass = "PASS".equalsIgnoreCase(er.getResult());
                    StatusBadge.StatusType badgeType = isPass ? StatusBadge.StatusType.SUCCESS : StatusBadge.StatusType.ERROR;

                    rows.add(new HistoryRow(
                            "#" + er.getId(),
                            subName,
                            String.valueOf(er.getTotalQuestions()),
                            String.valueOf(er.getCorrectAnswers()),
                            String.format("%.2f%%", er.getPercentage()),
                            er.getResult() != null ? er.getResult() : "Graded",
                            badgeType,
                            dateStr
                    ));
                }

                // Add expired attempts that have no exam_results row (without fabricating results)
                for (ExamAttempt att : attempts) {
                    if (att.getId() > 0 && !coveredAttemptIds.contains(att.getId())) {
                        if ("EXPIRED".equalsIgnoreCase(att.getStatus()) || (att.isExpired() && !"COMPLETED".equalsIgnoreCase(att.getStatus()))) {
                            String subName = sMap.getOrDefault(att.getSubjectId(), "Subject #" + att.getSubjectId());
                            String dateStr = att.getStartedAt() != null ? DATE_FORMATTER.format(att.getStartedAt()) : "—";

                            rows.add(new HistoryRow(
                                    "Att #" + att.getId(),
                                    subName,
                                    String.valueOf(att.getTotalQuestions()),
                                    "—",
                                    "—",
                                    "Expired",
                                    StatusBadge.StatusType.ERROR,
                                    dateStr
                            ));
                        }
                    }
                }

                return new ResultsData(rows);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return; // drop stale result
                }
                try {
                    if (!isCancelled()) {
                        ResultsData data = get();
                        tableModel.setRowCount(0);

                        for (HistoryRow r : data.rows) {
                            StatusBadge badge = new StatusBadge(r.statusText, r.statusType);
                            tableModel.addRow(new Object[]{
                                    r.idDisplay,
                                    r.subjectName,
                                    r.totalQuestions,
                                    r.correctAnswers,
                                    r.scorePercentage,
                                    badge,
                                    r.dateDisplay
                            });
                        }

                        if (data.rows.isEmpty()) {
                            tableScroll.setVisible(false);
                            tableScroll.getParent().add(emptyLabel, BorderLayout.CENTER);
                        } else {
                            emptyLabel.getParent().remove(emptyLabel);
                            tableScroll.setVisible(true);
                        }

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
}
