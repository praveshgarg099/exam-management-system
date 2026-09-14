package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.QuestionService;
import exam_management_syatem.service.StudentService;
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

public class AdminExamsView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final ExamService examService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final QuestionService questionService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<ExamsPayload, Void> activeWorker = null;

    private SearchField searchField;
    private JComboBox<String> statusFilterCombo;
    private AppButton refreshBtn;
    private AppButton scheduleBtn;
    private AppButton editBtn;
    private AppButton cancelBtn;
    private JLabel timestampLabel;

    private DefaultTableModel tableModel;
    private AppTable table;
    private List<ExamSchedule> displayedSchedules = new ArrayList<>();
    private Map<Integer, Student> studentMap = new HashMap<>();
    private Map<Integer, Subject> subjectMap = new HashMap<>();

    private static class ExamsPayload {
        final List<ExamSchedule> schedules;
        final List<Student> students;
        final List<Subject> subjects;

        ExamsPayload(List<ExamSchedule> schedules, List<Student> students, List<Subject> subjects) {
            this.schedules = schedules;
            this.students = students;
            this.subjects = subjects;
        }
    }

    public AdminExamsView(UserSession session) {
        this(session, null);
    }

    public AdminExamsView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.examService = new ExamService();
        this.studentService = new StudentService();
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

        JLabel titleLabel = new JLabel("Exam Schedules & Lifecycle");
        titleLabel.setFont(Typography.H1);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Plan examinations for registered students, set durations, question allotments, and pass thresholds.");
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

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.SM, 0));
        leftGroup.setOpaque(false);

        searchField = new SearchField("Search student or subject...");
        searchField.setPreferredSize(new Dimension(260, Dimensions.BUTTON_HEIGHT));
        searchField.addActionListener(e -> filterTableLocally());
        leftGroup.add(searchField);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"});
        statusFilterCombo.setPreferredSize(new Dimension(160, Dimensions.BUTTON_HEIGHT));
        statusFilterCombo.addActionListener(e -> filterTableLocally());
        leftGroup.add(statusFilterCombo);

        toolbarPanel.add(leftGroup, BorderLayout.WEST);

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.SM, 0));
        rightGroup.setOpaque(false);

        scheduleBtn = new AppButton("+ Schedule Exam", AppButton.ButtonStyle.PRIMARY);
        scheduleBtn.addActionListener(e -> openScheduleExamDialog());

        editBtn = new AppButton("Edit Schedule", AppButton.ButtonStyle.SECONDARY);
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> openEditScheduleDialog());

        cancelBtn = new AppButton("Cancel Exam", AppButton.ButtonStyle.DANGER);
        cancelBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> cancelSelectedExam());

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        rightGroup.add(scheduleBtn);
        rightGroup.add(editBtn);
        rightGroup.add(cancelBtn);
        rightGroup.add(refreshBtn);
        toolbarPanel.add(rightGroup, BorderLayout.EAST);

        contentPanel.add(toolbarPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.LG));

        // 3. Table Card
        AppCard tableCard = new AppCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Schedule ID", "Student Name", "Subject", "Duration", "Total Questions", "Passing %", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new AppTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Dimensions.TABLE_ROW_HEIGHT);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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
                ExamSchedule s = getSelectedSchedule();
                boolean isScheduled = s != null && "SCHEDULED".equalsIgnoreCase(s.getStatus());
                editBtn.setEnabled(isScheduled);
                cancelBtn.setEnabled(isScheduled);
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
            protected ExamsPayload doInBackground() throws Exception {
                List<ExamSchedule> schedules = examService.getAllSchedules(session);
                List<Student> students = studentService.getAllStudents(session);
                List<Subject> subjects = subjectService.getAllSubjects(session);
                return new ExamsPayload(schedules, students, subjects);
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return;
                }
                try {
                    ExamsPayload payload = get();
                    displayedSchedules = payload.schedules;

                    studentMap.clear();
                    for (Student st : payload.students) {
                        studentMap.put(st.getId(), st);
                    }

                    subjectMap.clear();
                    for (Subject sub : payload.subjects) {
                        subjectMap.put(sub.getId(), sub);
                    }

                    filterTableLocally();
                    timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    timestampLabel.setText("Failed to load schedules");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                    editBtn.setEnabled(false);
                    cancelBtn.setEnabled(false);
                }
            }
        };
        activeWorker.execute();
    }

    private void filterTableLocally() {
        String search = searchField.getText().trim().toLowerCase();
        String selStatus = (String) statusFilterCombo.getSelectedItem();

        tableModel.setRowCount(0);

        for (ExamSchedule es : displayedSchedules) {
            Student st = studentMap.get(es.getStudentId());
            Subject sub = subjectMap.get(es.getSubjectId());

            String studentName = st != null ? st.getName() : "Student #" + es.getStudentId();
            String subjectName = sub != null ? sub.getName() : "Subject #" + es.getSubjectId();

            if (selStatus != null && !selStatus.equals("All Statuses") && !es.getStatus().equalsIgnoreCase(selStatus)) {
                continue;
            }

            if (!search.isEmpty()) {
                boolean matchStudent = studentName.toLowerCase().contains(search);
                boolean matchSubject = subjectName.toLowerCase().contains(search);
                if (!matchStudent && !matchSubject) {
                    continue;
                }
            }

            StatusBadge.StatusType badgeType = StatusBadge.StatusType.INFO;
            if ("SCHEDULED".equalsIgnoreCase(es.getStatus())) badgeType = StatusBadge.StatusType.INFO;
            else if ("IN_PROGRESS".equalsIgnoreCase(es.getStatus())) badgeType = StatusBadge.StatusType.WARNING;
            else if ("COMPLETED".equalsIgnoreCase(es.getStatus())) badgeType = StatusBadge.StatusType.SUCCESS;
            else if ("CANCELLED".equalsIgnoreCase(es.getStatus())) badgeType = StatusBadge.StatusType.ERROR;

            StatusBadge badge = new StatusBadge(es.getStatus(), badgeType);

            tableModel.addRow(new Object[]{
                    es.getId(),
                    studentName,
                    subjectName,
                    es.getDurationMinutes() + " mins",
                    es.getTotalQuestions(),
                    String.format("%.0f%%", es.getPassingPercentage()),
                    badge
            });
        }
    }

    private ExamSchedule getSelectedSchedule() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            int schedId = (int) tableModel.getValueAt(row, 0);
            for (ExamSchedule es : displayedSchedules) {
                if (es.getId() == schedId) {
                    return es;
                }
            }
        }
        return null;
    }

    private void openScheduleExamDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Schedule New Examination", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        JComboBox<String> studentCombo = new JComboBox<>();
        List<Student> activeStudents = new ArrayList<>();
        for (Student st : studentMap.values()) {
            if (st.isActive()) {
                activeStudents.add(st);
                studentCombo.addItem(st.getName() + " (ID: " + st.getId() + ")");
            }
        }

        JComboBox<String> subjectCombo = new JComboBox<>();
        List<Subject> activeSubjects = new ArrayList<>();
        for (Subject sub : subjectMap.values()) {
            if (sub.isActive()) {
                activeSubjects.add(sub);
                subjectCombo.addItem(sub.getName());
            }
        }

        AppTextField durationField = new AppTextField();
        durationField.setText("30");
        AppTextField questionsField = new AppTextField();
        questionsField.setText("5");
        AppTextField passingField = new AppTextField();
        passingField.setText("40.0");

        panel.add(new JLabel("Target Student:"));
        panel.add(studentCombo);
        panel.add(new JLabel("Subject:"));
        panel.add(subjectCombo);
        panel.add(new JLabel("Duration (Minutes):"));
        panel.add(durationField);
        panel.add(new JLabel("Total Questions:"));
        panel.add(questionsField);
        panel.add(new JLabel("Passing Percentage (%):"));
        panel.add(passingField);

        AppButton submitBtn = new AppButton("Schedule Exam", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            int studentIdx = studentCombo.getSelectedIndex();
            int subjectIdx = subjectCombo.getSelectedIndex();

            if (studentIdx < 0 || subjectIdx < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select both an active student and subject.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student selStudent = activeStudents.get(studentIdx);
            Subject selSubject = activeSubjects.get(subjectIdx);

            int duration, totalQ;
            double passing;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                totalQ = Integer.parseInt(questionsField.getText().trim());
                passing = Double.parseDouble(passingField.getText().trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Duration and questions must be integers; passing percentage must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                examService.scheduleExam(session, selStudent.getId(), selSubject.getId(), duration, totalQ, passing);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Exam scheduled successfully for " + selStudent.getName() + " in " + selSubject.getName() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Scheduling Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(cancelBtn);
        panel.add(submitBtn);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void openEditScheduleDialog() {
        ExamSchedule s = getSelectedSchedule();
        if (s == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Exam Schedule #" + s.getId(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField durationField = new AppTextField();
        durationField.setText(String.valueOf(s.getDurationMinutes()));
        AppTextField questionsField = new AppTextField();
        questionsField.setText(String.valueOf(s.getTotalQuestions()));
        AppTextField passingField = new AppTextField();
        passingField.setText(String.valueOf(s.getPassingPercentage()));

        panel.add(new JLabel("Duration (Minutes):"));
        panel.add(durationField);
        panel.add(new JLabel("Total Questions:"));
        panel.add(questionsField);
        panel.add(new JLabel("Passing Percentage (%):"));
        panel.add(passingField);

        AppButton submitBtn = new AppButton("Save Changes", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                int totalQ = Integer.parseInt(questionsField.getText().trim());
                double passing = Double.parseDouble(passingField.getText().trim());

                examService.updateExamSchedule(session, s.getId(), duration, totalQ, passing);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Schedule #" + s.getId() + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(cancelBtn);
        panel.add(submitBtn);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void cancelSelectedExam() {
        ExamSchedule s = getSelectedSchedule();
        if (s == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel Exam Schedule #" + s.getId() + "?",
                "Confirm Exam Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                examService.cancelExam(session, s.getId());
                JOptionPane.showMessageDialog(this, "Exam Schedule #" + s.getId() + " cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
