package exam_management_syatem;

import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentManagement extends JFrame {
    private static final long serialVersionUID = 1L;

    private final StudentService studentService;
    private final AuthenticationService authService;
    private final ResultService resultService;
    private final SubjectService subjectService;

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Student> displayedStudents;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    StudentManagement frame = new StudentManagement();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public StudentManagement() {
        this.studentService = new StudentService();
        this.authService = new AuthenticationService();
        this.resultService = new ResultService();
        this.subjectService = new SubjectService();

        initializeUI();
        loadStudents(null);
    }

    private void initializeUI() {
        setTitle("Exam Management System - Student Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1100, 680);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);

        // Header Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        titlePanel.add(titleLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);

        JButton backBtn = new JButton("Back to Admin Dashboard");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                adminpage1.main(null);
            }
        });
        topPanel.add(backBtn, BorderLayout.EAST);

        // Search Bar Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search (Name / Email / Mobile / Aadhar):"));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadStudents(searchField.getText().trim());
            }
        });
        searchPanel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadStudents(searchField.getText().trim());
            }
        });
        searchPanel.add(searchBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Tahoma", Font.PLAIN, 12));
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                loadStudents(null);
            }
        });
        searchPanel.add(clearBtn);

        topPanel.add(searchPanel, BorderLayout.SOUTH);
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Mobile", "Email", "Aadhar No", "DOB", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(26);
        studentTable.setFont(new Font("Tahoma", Font.PLAIN, 13));
        studentTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align status column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JButton addBtn = new JButton("Add Student");
        addBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAddStudentDialog();
            }
        });
        actionPanel.add(addBtn);


        JButton editBtn = new JButton("Edit Student");
        editBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Student s = getSelectedStudent();
                if (s != null) {
                    showEditDialog(s);
                }
            }
        });
        actionPanel.add(editBtn);

        JButton toggleStatusBtn = new JButton("Toggle Active/Inactive");
        toggleStatusBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        toggleStatusBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Student s = getSelectedStudent();
                if (s != null) {
                    boolean newStatus = !s.isActive();
                    String actionText = newStatus ? "activate" : "deactivate";
                    int confirm = JOptionPane.showConfirmDialog(
                            StudentManagement.this,
                            "Are you sure you want to " + actionText + " student '" + s.getName() + "'?\n" +
                            (newStatus ? "They will be allowed to log in and take exams." : "They will be blocked from logging in."),
                            "Confirm Status Change",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            studentService.setStudentActiveStatus(s.getId(), newStatus);
                            JOptionPane.showMessageDialog(StudentManagement.this, "Student status updated to " + (newStatus ? "Active" : "Inactive") + ".");
                            loadStudents(searchField.getText().trim());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(StudentManagement.this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        actionPanel.add(toggleStatusBtn);

        JButton resetPassBtn = new JButton("Reset Password");
        resetPassBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        resetPassBtn.setBackground(new Color(230, 126, 34));
        resetPassBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Student s = getSelectedStudent();
                if (s != null) {
                    int confirm = JOptionPane.showConfirmDialog(
                            StudentManagement.this,
                            "Generate a new temporary password for student '" + s.getName() + "' (ID: " + s.getId() + ")?",
                            "Confirm Password Reset",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            String tempPass = authService.adminResetStudentPassword(s.getId());
                            JTextField tempPassField = new JTextField(tempPass);
                            tempPassField.setEditable(false);
                            tempPassField.setFont(new Font("Monospaced", Font.BOLD, 16));
                            Object[] msg = {
                                    "Temporary password generated successfully!",
                                    "Please provide this temporary password to the student:",
                                    tempPassField
                            };
                            JOptionPane.showMessageDialog(StudentManagement.this, msg, "Password Reset Complete", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(StudentManagement.this, "Password reset failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        actionPanel.add(resetPassBtn);

        JButton detailsBtn = new JButton("View Details & History");
        detailsBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        detailsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Student s = getSelectedStudent();
                if (s != null) {
                    showDetailsDialog(s);
                }
            }
        });
        actionPanel.add(detailsBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma", Font.PLAIN, 13));
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                loadStudents(null);
            }
        });
        actionPanel.add(refreshBtn);

        contentPane.add(actionPanel, BorderLayout.SOUTH);
    }

    private Student getSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || displayedStudents == null || row >= displayedStudents.size()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedStudents.get(row);
    }

    private void loadStudents(String keyword) {
        tableModel.setRowCount(0);
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                displayedStudents = studentService.getAllStudents();
            } else {
                displayedStudents = studentService.searchStudents(keyword.trim());
            }

            for (Student s : displayedStudents) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getName(),
                        s.getMobile(),
                        s.getEmail(),
                        s.getAadharNo(),
                        s.getDateOfBirth(),
                        s.isActive() ? "Active" : "Inactive"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditDialog(final Student s) {
        JDialog dialog = new JDialog(this, "Edit Student - " + s.getName(), true);
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));

        final JTextField nameField = new JTextField(s.getName());
        final JTextField mobileField = new JTextField(s.getMobile());
        final JTextField emailField = new JTextField(s.getEmail());
        final JTextField aadharField = new JTextField(s.getAadharNo());
        final JTextField dobField = new JTextField(s.getDateOfBirth());
        final JCheckBox activeCheck = new JCheckBox("Active", s.isActive());

        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Mobile (10 digits):")); form.add(mobileField);
        form.add(new JLabel("Email Address:")); form.add(emailField);
        form.add(new JLabel("Aadhar (12 digits):")); form.add(aadharField);
        form.add(new JLabel("DOB (DDMMYYYY):")); form.add(dobField);
        form.add(new JLabel("Account Status:")); form.add(activeCheck);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        saveBtn.setBackground(new Color(41, 128, 185));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    s.setName(nameField.getText().trim());
                    s.setMobile(mobileField.getText().trim());
                    s.setEmail(emailField.getText().trim());
                    s.setAadharNo(aadharField.getText().trim());
                    s.setDateOfBirth(dobField.getText().trim());
                    s.setActive(activeCheck.isSelected());

                    studentService.updateStudent(s);
                    studentService.setStudentActiveStatus(s.getId(), activeCheck.isSelected());
                    JOptionPane.showMessageDialog(dialog, "Student updated successfully!");
                    dialog.dispose();
                    loadStudents(searchField.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showDetailsDialog(Student s) {
        JDialog dialog = new JDialog(this, "Student Details - " + s.getName(), true);
        dialog.setSize(750, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 8));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Student Profile"));
        infoPanel.add(new JLabel("Name: " + s.getName()));
        infoPanel.add(new JLabel("Student ID: " + s.getId()));
        infoPanel.add(new JLabel("Mobile: " + s.getMobile()));
        infoPanel.add(new JLabel("Email: " + s.getEmail()));
        infoPanel.add(new JLabel("Aadhar: " + s.getAadharNo()));
        infoPanel.add(new JLabel("Status: " + (s.isActive() ? "Active" : "Inactive")));
        dialog.add(infoPanel, BorderLayout.NORTH);

        // Results history table
        String[] cols = {"Result ID", "Subject", "Total", "Correct", "Score (%)", "Status", "Date"};
        DefaultTableModel resModel = new DefaultTableModel(cols, 0);
        JTable resTable = new JTable(resModel);
        resTable.setRowHeight(24);

        try {
            Map<Integer, Subject> subMap = new HashMap<>();
            for (Subject sub : subjectService.getAllSubjects()) subMap.put(sub.getId(), sub);

            List<ExamResult> results = resultService.getResultsByStudent(s.getId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (ExamResult er : results) {
                Subject sub = subMap.get(er.getSubjectId());
                String subName = sub != null ? sub.getName() : ("Subject #" + er.getSubjectId());
                String dateStr = er.getSubmittedAt() != null ? sdf.format(er.getSubmittedAt()) : "N/A";
                resModel.addRow(new Object[]{
                        er.getId(),
                        subName,
                        er.getTotalQuestions(),
                        er.getCorrectAnswers(),
                        String.format("%.2f%%", er.getPercentage()),
                        er.getResult(),
                        dateStr
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Exam History"));
        tablePanel.add(new JScrollPane(resTable), BorderLayout.CENTER);
        dialog.add(tablePanel, BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        closePanel.add(closeBtn);
        dialog.add(closePanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Add New Student", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));

        final JTextField nameField = new JTextField();
        final JTextField mobileField = new JTextField();
        final JTextField emailField = new JTextField();
        final JTextField aadharField = new JTextField();
        final JTextField dobField = new JTextField();

        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Mobile (10 digits):")); form.add(mobileField);
        form.add(new JLabel("Email Address:")); form.add(emailField);
        form.add(new JLabel("Aadhar (12 digits):")); form.add(aadharField);
        form.add(new JLabel("DOB (DDMMYYYY):")); form.add(dobField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = new JButton("Register Student");
        saveBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.BLACK);
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText().trim();
                    String mobile = mobileField.getText().trim();
                    String email = emailField.getText().trim();
                    String aadhar = aadharField.getText().trim();
                    String dob = dobField.getText().trim();

                    StudentService.RegistrationResult res = studentService.registerStudent(name, mobile, email, aadhar, dob);
                    
                    JTextField uField = new JTextField(res.username);
                    uField.setEditable(false);
                    JTextField pField = new JTextField(res.password);
                    pField.setEditable(false);

                    Object[] msg = {
                            "Student registered successfully!",
                            "Auto-generated credentials for the student:",
                            "Username:", uField,
                            "Password:", pField
                    };
                    JOptionPane.showMessageDialog(dialog, msg, "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudents(searchField.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

