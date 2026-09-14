package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.ExamResult;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.DashboardMetricsService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppTable;
import exam_management_syatem.ui.components.StatCard;
import exam_management_syatem.ui.components.StatusBadge;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;
import exam_management_syatem.ui.shell.NavigationController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import java.util.List;

public class AdminDashboardView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final DashboardMetricsService metricsService;
    private final ResultService resultService;

    // UI Components
    private JLabel lastUpdatedLabel;
    private AppButton refreshButton;
    private JPanel errorBanner;
    private JLabel errorBannerLabel;

    // 5 Primary KPI StatCards
    private StatCard studentsCard;
    private StatCard questionsCard;
    private StatCard schedulesCard;
    private StatCard passRateCard;
    private StatCard avgScoreCard;

    // Recent Activity Table
    private DefaultTableModel recentTableModel;
    private AppTable recentTable;
    private JScrollPane tableScrollPane;
    private JPanel emptyStatePanel;
    private JLabel recentCountLabel;

    public AdminDashboardView(UserSession session) {
        this(session, null);
    }

    public AdminDashboardView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.metricsService = new DashboardMetricsService();
        this.resultService = new ResultService();

        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        // Content panel inside scroll pane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // 1. Header Section
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, Spacing.MD)));

        // Error Banner (Hidden by default)
        errorBanner = new JPanel(new BorderLayout());
        errorBanner.setOpaque(true);
        errorBanner.setBackground(Colors.STATUS_ERROR_BG);
        errorBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.STATUS_ERROR, 1),
            new EmptyBorder(Spacing.SM, Spacing.MD, Spacing.SM, Spacing.MD)
        ));
        errorBannerLabel = new JLabel();
        errorBannerLabel.setFont(Typography.BODY_SMALL);
        errorBannerLabel.setForeground(Colors.STATUS_ERROR_TEXT);
        errorBanner.add(errorBannerLabel, BorderLayout.CENTER);
        errorBanner.setVisible(false);
        errorBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        contentPanel.add(errorBanner);
        contentPanel.add(Box.createRigidArea(new Dimension(0, Spacing.LG)));

        // 2. 5 Primary KPI Cards
        contentPanel.add(createKpiPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, Spacing.XL)));

        // 3. Middle Section: Recent Examination Activity
        contentPanel.add(createRecentActivityPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, Spacing.XL)));

        // 4. Quick Operations Hub
        contentPanel.add(createQuickActionsPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, Spacing.LG)));

        // 5. System Status Bar
        contentPanel.add(createSystemStatusBar());

        // Wrap in smooth JScrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Colors.BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Administrator Overview");
        title.setFont(Typography.H1);
        title.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time institutional metrics & operational controls");
        subtitle.setFont(Typography.BODY_SMALL);
        subtitle.setForeground(Colors.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, Spacing.XS)));
        titlePanel.add(subtitle);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.MD, 0));
        actionPanel.setOpaque(false);

        lastUpdatedLabel = new JLabel("Loading data...");
        lastUpdatedLabel.setFont(Typography.CAPTION);
        lastUpdatedLabel.setForeground(Colors.TEXT_MUTED);

        refreshButton = new AppButton("Refresh Data", AppButton.ButtonStyle.SECONDARY);
        refreshButton.addActionListener(e -> loadDashboardData());

        actionPanel.add(lastUpdatedLabel);
        actionPanel.add(refreshButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, Spacing.LG, Spacing.LG));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        studentsCard = new StatCard("Enrolled Students", "—", "Loading...");
        questionsCard = new StatCard("Question Repository", "—", "Loading...");
        schedulesCard = new StatCard("Exam Schedules", "—", "Loading...");
        passRateCard = new StatCard("Pass Rate", "—", "Loading...");
        avgScoreCard = new StatCard("Average Performance", "—", "Loading...");

        kpiGrid.add(studentsCard);
        kpiGrid.add(questionsCard);
        kpiGrid.add(schedulesCard);
        kpiGrid.add(passRateCard);
        kpiGrid.add(avgScoreCard);

        return kpiGrid;
    }

    private JPanel createRecentActivityPanel() {
        AppCard card = new AppCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Card Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, Spacing.MD, 0));

        JLabel title = new JLabel("Recent Examination Activity");
        title.setFont(Typography.H2);
        title.setForeground(Colors.TEXT_PRIMARY);

        recentCountLabel = new JLabel("Latest submissions");
        recentCountLabel.setFont(Typography.CAPTION);
        recentCountLabel.setForeground(Colors.TEXT_SECONDARY);

        cardHeader.add(title, BorderLayout.WEST);
        cardHeader.add(recentCountLabel, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        // Table Model with 8 exact audited columns
        String[] columns = {"Result ID", "Attempt ID", "Student", "Subject", "Score", "Percentage", "Status", "Submitted"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new AppTable(recentTableModel);
        recentTable.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        // Custom Renderer for Status column (index 6) to draw StatusBadge
        recentTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof StatusBadge) {
                    StatusBadge badge = (StatusBadge) value;
                    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                    wrapper.setOpaque(isSelected);
                    if (isSelected) wrapper.setBackground(table.getSelectionBackground());
                    wrapper.add(badge);
                    return wrapper;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        tableScrollPane = new JScrollPane(recentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 1));
        tableScrollPane.getViewport().setBackground(Colors.SURFACE);

        // Empty state panel
        emptyStatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, Spacing.XXXL));
        emptyStatePanel.setOpaque(false);
        JLabel emptyLabel = new JLabel("No examinations have been completed yet.");
        emptyLabel.setFont(Typography.BODY);
        emptyLabel.setForeground(Colors.TEXT_MUTED);
        emptyStatePanel.add(emptyLabel);
        emptyStatePanel.setVisible(false);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(tableScrollPane, BorderLayout.CENTER);
        tableContainer.add(emptyStatePanel, BorderLayout.SOUTH);

        card.add(tableContainer, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsPanel() {
        AppCard card = new AppCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, Spacing.MD, 0));

        JLabel title = new JLabel("Quick Operations");
        title.setFont(Typography.H3);
        title.setForeground(Colors.TEXT_PRIMARY);
        cardHeader.add(title);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new GridLayout(1, 5, Spacing.MD, Spacing.MD));
        actionsGrid.setOpaque(false);

        actionsGrid.add(createActionButton("Schedule Exam", "ADMIN_EXAMS"));
        actionsGrid.add(createActionButton("Student Directory", "ADMIN_STUDENTS"));
        actionsGrid.add(createActionButton("Question Bank", "ADMIN_QUESTIONS"));
        actionsGrid.add(createActionButton("Subject Management", "ADMIN_SUBJECTS"));
        actionsGrid.add(createActionButton("Exam Results", "ADMIN_RESULTS"));

        card.add(actionsGrid, BorderLayout.CENTER);
        return card;
    }

    private AppButton createActionButton(String label, String routeId) {
        AppButton btn = new AppButton(label, AppButton.ButtonStyle.SECONDARY);
        btn.setFont(Typography.LABEL);
        btn.addActionListener(e -> {
            if (navigationController != null) {
                navigationController.navigateTo(routeId);
            }
        });
        return btn;
    }

    private JPanel createSystemStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.MD, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel statusLabel = new JLabel("● Database Connected");
        statusLabel.setFont(Typography.CAPTION);
        statusLabel.setForeground(Colors.STATUS_SUCCESS);

        bar.add(statusLabel);
        return bar;
    }

    public void refreshData() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Prevent concurrent refresh
        refreshButton.setEnabled(false);
        refreshButton.setText("Refreshing...");
        errorBanner.setVisible(false);

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardMetricsService.Metrics metrics = metricsService.getMetrics(session);
                List<ExamResult> recent = resultService.getRecentResults(session, 10);
                return new DashboardData(metrics, recent);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();

                    // 1. Update 5 KPI Cards
                    DashboardMetricsService.Metrics m = data.metrics;
                    studentsCard.setValue(String.valueOf(m.totalStudents));
                    studentsCard.setSubtitle(m.activeStudents + " active");

                    questionsCard.setValue(String.valueOf(m.totalQuestions));
                    questionsCard.setSubtitle(m.activeQuestions + " active");

                    schedulesCard.setValue(String.valueOf(m.totalSchedules));
                    schedulesCard.setSubtitle(m.totalSubjects + " subjects");

                    passRateCard.setValue(m.passRate + "%");
                    passRateCard.setSubtitle(m.passedCount + " passed / " + m.failedCount + " failed");

                    avgScoreCard.setValue(m.averageScorePercentage + "%");
                    avgScoreCard.setSubtitle(m.totalResults + " total attempts");

                    // 2. Update Recent Activity Table
                    recentTableModel.setRowCount(0);
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

                    if (data.recentResults.isEmpty()) {
                        tableScrollPane.setVisible(false);
                        emptyStatePanel.setVisible(true);
                        recentCountLabel.setText("0 records found");
                    } else {
                        tableScrollPane.setVisible(true);
                        emptyStatePanel.setVisible(false);
                        recentCountLabel.setText("Showing latest " + data.recentResults.size() + " submissions");

                        for (ExamResult r : data.recentResults) {
                            String attemptDisplay = r.getAttemptId() != null ? "#" + r.getAttemptId() : "— (Legacy)";
                            String studentDisplay = r.getStudentName() != null ? r.getStudentName() : "Student #" + r.getStudentId();
                            String subjectDisplay = r.getSubjectName() != null ? r.getSubjectName() : "Subject #" + r.getSubjectId();
                            String scoreDisplay = r.getMarks() + " / " + r.getTotalQuestions();
                            String pctDisplay = String.format("%.1f%%", r.getPercentage());
                            String resText = r.getResult() != null ? r.getResult() : "—";
                            StatusBadge statusBadge = new StatusBadge(resText, "Pass".equalsIgnoreCase(resText) ? StatusBadge.StatusType.SUCCESS : StatusBadge.StatusType.ERROR);
                            String dateDisplay = r.getSubmittedAt() != null ? dtf.format(r.getSubmittedAt()) : "—";

                            recentTableModel.addRow(new Object[]{
                                "#" + r.getId(),
                                attemptDisplay,
                                studentDisplay,
                                subjectDisplay,
                                scoreDisplay,
                                pctDisplay,
                                statusBadge,
                                dateDisplay
                            });
                        }
                    }

                    // 3. Update timestamp
                    String timeStr = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now());
                    lastUpdatedLabel.setText("Last updated: " + timeStr);

                    revalidate();
                    repaint();
                } catch (Exception ex) {
                    errorBannerLabel.setText("Unable to refresh dashboard data. Please check database connection and try again.");
                    errorBanner.setVisible(true);
                } finally {
                    refreshButton.setText("Refresh Data");
                    refreshButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private static class DashboardData {
        final DashboardMetricsService.Metrics metrics;
        final List<ExamResult> recentResults;

        DashboardData(DashboardMetricsService.Metrics metrics, List<ExamResult> recentResults) {
            this.metrics = metrics;
            this.recentResults = recentResults;
        }
    }
}
