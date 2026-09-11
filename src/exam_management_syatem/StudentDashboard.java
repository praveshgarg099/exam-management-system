package exam_management_syatem;

import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDashboard extends JFrame {
    private static final long serialVersionUID = 1L;

    private final UserSession session;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ExamService examService;
    private final ResultService resultService;
    private final AuthenticationService authService;

    private Student currentStudent;
    private Map<Integer, Subject> subjectCache = new HashMap<>();

    private JTable scheduledExamsTable;
    private DefaultTableModel scheduledTableModel;
    private List<ExamSchedule> currentSchedules;

    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;

    public static void mainWithSession(final UserSession session) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    StudentDashboard frame = new StudentDashboard(session);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        mainWithSession(null);
    }

    public StudentDashboard(UserSession session) {
        this.session = session;
        this.studentService = new StudentService();
        this.subjectService = new SubjectService();
        this.examService = new ExamService();
        this.resultService = new ResultService();
        this.authService = new AuthenticationService();

        loadStudentProfile();
        initializeUI();
        loadSubjectCache();
        refreshScheduledExams();
        refreshResults();
    }

    private void loadStudentProfile() {
        if (session != null && session.getStudentId() != null) {
            try {
                this.currentStudent = studentService.getStudentById(session.getStudentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSubjectCache() {
        try {
            List<Subject> list = subjectService.getAllSubjects();
            for (Subject s : list) {
                subjectCache.put(s.getId(), s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSubjectName(int subjectId) {
        Subject s = subjectCache.get(subjectId);
        return s != null ? s.getName() : ("Subject #" + subjectId);
    }

    private void initializeUI() {
        setTitle("Exam Management System - Student Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1000, 680);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 42, 86));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Student Portal");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        String displayName = currentStudent != null ? currentStudent.getName() : "Student";
        String username = session != null ? session.getUsername() : "";
        JLabel userLabel = new JLabel("Welcome, " + displayName + " (" + username + ")");
        userLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
        userLabel.setForeground(Color.WHITE);
        userPanel.add(userLabel);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        logoutBtn.setBackground(new Color(232, 65, 24));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                index.main(null);
            }
        });
        userPanel.add(logoutBtn);

        headerPanel.add(userPanel, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // TAB 1: Available Exams
        JPanel examsTab = createAvailableExamsPanel();
        tabbedPane.addTab("Available Exams", examsTab);

        // TAB 2: Exam Results / History
        JPanel resultsTab = createResultsHistoryPanel();
        tabbedPane.addTab("My Exam History", resultsTab);

        // TAB 3: Profile & Security
        JPanel profileTab = createProfileAndSecurityPanel();
        tabbedPane.addTab("Profile & Password", profileTab);
    }

    private JPanel createAvailableExamsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Schedule ID", "Subject", "Duration", "Questions", "Passing %", "Status"};
        scheduledTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        scheduledExamsTable = new JTable(scheduledTableModel);
        scheduledExamsTable.setRowHeight(26);
        scheduledExamsTable.setFont(new Font("Tahoma", Font.PLAIN, 13));
        scheduledExamsTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        scheduledExamsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(scheduledExamsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma", Font.PLAIN, 13));
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshScheduledExams();
            }
        });
        actionPanel.add(refreshBtn);

        JButton startExamBtn = new JButton("Start Selected Exam");
        startExamBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        startExamBtn.setBackground(new Color(46, 204, 113));
        startExamBtn.setForeground(Color.BLACK);
        startExamBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = scheduledExamsTable.getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, "Please select an exam from the list to start.", "No Exam Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (currentSchedules == null || selectedRow >= currentSchedules.size()) {
                    return;
                }

                ExamSchedule schedule = currentSchedules.get(selectedRow);
                String subName = getSubjectName(schedule.getSubjectId());

                int confirm = JOptionPane.showConfirmDialog(
                        StudentDashboard.this,
                        "Start exam for '" + subName + "'?\n" +
                        "Duration: " + schedule.getDurationMinutes() + " minutes\n" +
                        "Questions: " + schedule.getTotalQuestions() + "\n" +
                        "Passing Score: " + schedule.getPassingPercentage() + "%\n\n" +
                        "The timer will start immediately once the exam window opens.",
                        "Confirm Start Exam",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        examService.startExam(schedule.getId());
                    } catch (Exception ex) {
                        // In case status update fails, still allow taking or inform user
                    }
                    dispose();
                    testtake.mainWithSessionAndSchedule(session, schedule);
                }
            }
        });
        actionPanel.add(startExamBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createResultsHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Result ID", "Subject", "Total Questions", "Correct", "Score (%)", "Status", "Date"};
        resultsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultsTable = new JTable(resultsTableModel);
        resultsTable.setRowHeight(26);
        resultsTable.setFont(new Font("Tahoma", Font.PLAIN, 13));
        resultsTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma", Font.PLAIN, 13));
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshResults();
            }
        });
        bottomPanel.add(refreshBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProfileAndSecurityPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile Details Card
        JPanel profileCard = new JPanel(new GridBagLayout());
        profileCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Personal Information",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addProfileRow(profileCard, gbc, row++, "Full Name:", currentStudent != null ? currentStudent.getName() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Username:", session != null ? session.getUsername() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Mobile No:", currentStudent != null ? currentStudent.getMobile() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Email Address:", currentStudent != null ? currentStudent.getEmail() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Aadhar Number:", currentStudent != null ? currentStudent.getAadharNo() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Date of Birth:", currentStudent != null ? currentStudent.getDateOfBirth() : "N/A");
        addProfileRow(profileCard, gbc, row++, "Account Status:", (currentStudent != null && currentStudent.isActive()) ? "Active" : "Inactive");

        panel.add(profileCard);

        // Security / Change Password Card
        JPanel passCard = new JPanel(new GridBagLayout());
        passCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Change Password",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 15)
        ));

        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(10, 12, 10, 12);
        pgbc.fill = GridBagConstraints.HORIZONTAL;

        int prow = 0;
        pgbc.gridx = 0; pgbc.gridy = prow; pgbc.gridwidth = 1;
        JLabel curLbl = new JLabel("Current Password:");
        curLbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        passCard.add(curLbl, pgbc);

        pgbc.gridx = 1;
        final JPasswordField curPassField = new JPasswordField(15);
        passCard.add(curPassField, pgbc);

        prow++;
        pgbc.gridx = 0; pgbc.gridy = prow;
        JLabel newLbl = new JLabel("New Password:");
        newLbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        passCard.add(newLbl, pgbc);

        pgbc.gridx = 1;
        final JPasswordField newPassField = new JPasswordField(15);
        passCard.add(newPassField, pgbc);

        prow++;
        pgbc.gridx = 0; pgbc.gridy = prow;
        JLabel confLbl = new JLabel("Confirm New Password:");
        confLbl.setFont(new Font("Tahoma", Font.PLAIN, 13));
        passCard.add(confLbl, pgbc);

        pgbc.gridx = 1;
        final JPasswordField confPassField = new JPasswordField(15);
        passCard.add(confPassField, pgbc);

        prow++;
        pgbc.gridx = 0; pgbc.gridy = prow; pgbc.gridwidth = 2;
        JButton changeBtn = new JButton("Update Password");
        changeBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        changeBtn.setBackground(new Color(41, 128, 185));
        changeBtn.setForeground(Color.WHITE);
        changeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cur = new String(curPassField.getPassword()).trim();
                String newP = new String(newPassField.getPassword()).trim();
                String confP = new String(confPassField.getPassword()).trim();

                if (cur.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, "Please fill in all password fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (newP.length() < 6) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, "New password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!newP.equals(confP)) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, "New password and confirmation do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    if (session == null || session.getStudentId() == null) {
                        JOptionPane.showMessageDialog(StudentDashboard.this, "Session invalid. Please re-login.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    authService.changeStudentPassword(session.getStudentId(), cur, newP);
                    JOptionPane.showMessageDialog(StudentDashboard.this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    curPassField.setText("");
                    newPassField.setText("");
                    confPassField.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this, ex.getMessage(), "Password Update Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        passCard.add(changeBtn, pgbc);

        panel.add(passCard);
        return panel;
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        panel.add(l, gbc);

        gbc.gridx = 1;
        JLabel v = new JLabel(value);
        v.setFont(new Font("Tahoma", Font.PLAIN, 13));
        panel.add(v, gbc);
    }

    private void refreshScheduledExams() {
        scheduledTableModel.setRowCount(0);
        if (session == null || session.getStudentId() == null) return;

        try {
            loadSubjectCache();
            List<ExamSchedule> all = examService.getSchedulesByStudentId(session.getStudentId());
            // Filter only SCHEDULED
            currentSchedules = new java.util.ArrayList<>();
            for (ExamSchedule es : all) {
                if ("SCHEDULED".equalsIgnoreCase(es.getStatus())) {
                    currentSchedules.add(es);
                    scheduledTableModel.addRow(new Object[]{
                            es.getId(),
                            getSubjectName(es.getSubjectId()),
                            es.getDurationMinutes() + " mins",
                            es.getTotalQuestions(),
                            es.getPassingPercentage() + "%",
                            es.getStatus()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshResults() {
        resultsTableModel.setRowCount(0);
        if (session == null || session.getStudentId() == null) return;

        try {
            loadSubjectCache();
            List<ExamResult> list = resultService.getResultsByStudent(session.getStudentId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (ExamResult er : list) {
                String dateStr = er.getCreatedAt() != null ? sdf.format(er.getCreatedAt()) : "N/A";
                resultsTableModel.addRow(new Object[]{
                        er.getId(),
                        getSubjectName(er.getSubjectId()),
                        er.getTotalQuestions(),
                        er.getCorrectAnswers(),
                        String.format("%.2f%%", er.getPercentage()),
                        er.getResult(),
                        dateStr
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
