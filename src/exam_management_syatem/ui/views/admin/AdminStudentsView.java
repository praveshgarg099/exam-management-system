package exam_management_syatem.ui.views.admin;

import exam_management_syatem.model.Student;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.StudentService;
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
import javax.swing.SwingConstants;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AdminStudentsView extends JPanel {

    private final UserSession session;
    private final NavigationController navigationController;
    private final StudentService studentService;
    private final AuthenticationService authService;

    private final AtomicLong refreshGeneration = new AtomicLong(0);
    private SwingWorker<List<Student>, Void> activeWorker = null;

    private SearchField searchField;
    private AppButton refreshBtn;
    private AppButton registerBtn;
    private AppButton editBtn;
    private AppButton toggleStatusBtn;
    private AppButton resetPassBtn;
    private JLabel timestampLabel;

    private DefaultTableModel tableModel;
    private AppTable table;
    private List<Student> displayedStudents = new ArrayList<>();

    public AdminStudentsView(UserSession session) {
        this(session, null);
    }

    public AdminStudentsView(UserSession session, NavigationController navigationController) {
        this.session = session;
        this.navigationController = navigationController;
        this.studentService = new StudentService();
        this.authService = new AuthenticationService();

        initComponent();
        refreshData();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setBackground(Colors.BACKGROUND);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Colors.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // 1. Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Directory");
        titleLabel.setFont(Typography.H1);
        titleLabel.setForeground(Colors.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Enroll students, update profile records, manage active statuses, and reset credentials.");
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

        // 2. Action Toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout(Spacing.MD, 0));
        toolbarPanel.setOpaque(false);

        searchField = new SearchField("Search by Name, Email, Mobile, or Aadhar...");
        searchField.setPreferredSize(new Dimension(360, Dimensions.BUTTON_HEIGHT));
        searchField.addActionListener(e -> refreshData());
        toolbarPanel.add(searchField, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.SM, 0));
        btnGroup.setOpaque(false);

        refreshBtn = new AppButton("Refresh", AppButton.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        registerBtn = new AppButton("+ Register Student", AppButton.ButtonStyle.PRIMARY);
        registerBtn.addActionListener(e -> openRegisterStudentDialog());

        editBtn = new AppButton("Edit Details", AppButton.ButtonStyle.SECONDARY);
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> openEditStudentDialog());

        toggleStatusBtn = new AppButton("Toggle Status", AppButton.ButtonStyle.SECONDARY);
        toggleStatusBtn.setEnabled(false);
        toggleStatusBtn.addActionListener(e -> toggleStudentStatus());

        resetPassBtn = new AppButton("Reset Password", AppButton.ButtonStyle.SECONDARY);
        resetPassBtn.setEnabled(false);
        resetPassBtn.addActionListener(e -> resetStudentPassword());

        btnGroup.add(registerBtn);
        btnGroup.add(editBtn);
        btnGroup.add(toggleStatusBtn);
        btnGroup.add(resetPassBtn);
        btnGroup.add(refreshBtn);
        toolbarPanel.add(btnGroup, BorderLayout.EAST);

        contentPanel.add(toolbarPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.LG));

        // 3. Students Table Card
        AppCard tableCard = new AppCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Full Name", "Mobile Number", "Email Address", "Aadhar No", "DOB", "Status"};
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
                boolean hasSel = table.getSelectedRow() >= 0;
                editBtn.setEnabled(hasSel);
                toggleStatusBtn.setEnabled(hasSel);
                resetPassBtn.setEnabled(hasSel);
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
        final String keyword = searchField.getText().trim();

        refreshBtn.setEnabled(false);
        refreshBtn.setText("Loading...");
        timestampLabel.setText("Updating...");

        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        activeWorker = new SwingWorker<>() {
            @Override
            protected List<Student> doInBackground() throws Exception {
                if (keyword.isEmpty()) {
                    return studentService.getAllStudents(session);
                } else {
                    return studentService.searchStudents(session, keyword);
                }
            }

            @Override
            protected void done() {
                if (token != refreshGeneration.get()) {
                    return; // Stale request
                }
                try {
                    displayedStudents = get();
                    tableModel.setRowCount(0);

                    for (Student s : displayedStudents) {
                        StatusBadge badge = s.isActive()
                                ? new StatusBadge("Active", StatusBadge.StatusType.SUCCESS)
                                : new StatusBadge("Inactive", StatusBadge.StatusType.ERROR);

                        tableModel.addRow(new Object[]{
                                s.getId(),
                                s.getName(),
                                s.getMobile(),
                                s.getEmail(),
                                s.getAadharNo(),
                                s.getDateOfBirth(),
                                badge
                        });
                    }

                    timestampLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    timestampLabel.setText("Failed to load students");
                    timestampLabel.setForeground(Colors.STATUS_ERROR);
                } finally {
                    refreshBtn.setEnabled(true);
                    refreshBtn.setText("Refresh");
                    editBtn.setEnabled(false);
                    toggleStatusBtn.setEnabled(false);
                    resetPassBtn.setEnabled(false);
                }
            }
        };
        activeWorker.execute();
    }

    private Student getSelectedStudent() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedStudents.size()) {
            return displayedStudents.get(row);
        }
        return null;
    }

    private void openRegisterStudentDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Register New Student", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField nameField = new AppTextField();
        AppTextField mobileField = new AppTextField();
        AppTextField emailField = new AppTextField();
        AppTextField aadharField = new AppTextField();
        AppTextField dobField = new AppTextField();

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Mobile (10 digits):"));
        panel.add(mobileField);
        panel.add(new JLabel("Email Address:"));
        panel.add(emailField);
        panel.add(new JLabel("Aadhar (12 digits):"));
        panel.add(aadharField);
        panel.add(new JLabel("DOB (DDMMYYYY, 8 digits):"));
        panel.add(dobField);

        AppButton submitBtn = new AppButton("Register", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String mobile = mobileField.getText().trim();
            String email = emailField.getText().trim();
            String aadhar = aadharField.getText().trim();
            String dob = dobField.getText().trim();

            try {
                StudentService.RegistrationResult result =
                        studentService.registerStudent(session, name, mobile, email, aadhar, dob);
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Student registered successfully!\n\n" +
                                "Student ID: " + result.studentId + "\n" +
                                "Username: " + result.username + "\n" +
                                "Initial Password: " + result.password + "\n\n" +
                                "Please provide these credentials to the student.",
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(cancelBtn);
        panel.add(submitBtn);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void openEditStudentDialog() {
        Student s = getSelectedStudent();
        if (s == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Student — " + s.getName(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, Spacing.MD, Spacing.MD));
        panel.setBackground(Colors.SURFACE);
        panel.setBorder(new EmptyBorder(Spacing.LG, Spacing.LG, Spacing.LG, Spacing.LG));

        AppTextField nameField = new AppTextField();
        nameField.setText(s.getName());
        AppTextField mobileField = new AppTextField();
        mobileField.setText(s.getMobile());
        AppTextField emailField = new AppTextField();
        emailField.setText(s.getEmail());
        AppTextField aadharField = new AppTextField();
        aadharField.setText(s.getAadharNo());
        AppTextField dobField = new AppTextField();
        dobField.setText(s.getDateOfBirth());

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Mobile (10 digits):"));
        panel.add(mobileField);
        panel.add(new JLabel("Email Address:"));
        panel.add(emailField);
        panel.add(new JLabel("Aadhar (12 digits):"));
        panel.add(aadharField);
        panel.add(new JLabel("DOB (DDMMYYYY, 8 digits):"));
        panel.add(dobField);

        AppButton submitBtn = new AppButton("Save Changes", AppButton.ButtonStyle.PRIMARY);
        AppButton cancelBtn = new AppButton("Cancel", AppButton.ButtonStyle.SECONDARY);

        cancelBtn.addActionListener(e -> dialog.dispose());
        submitBtn.addActionListener(e -> {
            s.setName(nameField.getText().trim());
            s.setMobile(mobileField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAadharNo(aadharField.getText().trim());
            s.setDateOfBirth(dobField.getText().trim());

            try {
                studentService.updateStudent(session, s);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Student profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
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

    private void toggleStudentStatus() {
        Student s = getSelectedStudent();
        if (s == null) return;

        boolean newStatus = !s.isActive();
        String action = newStatus ? "activate" : "deactivate";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " student '" + s.getName() + "'?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.setStudentActiveStatus(session, s.getId(), newStatus);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetStudentPassword() {
        Student s = getSelectedStudent();
        if (s == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Generate a temporary password for student '" + s.getName() + "'?",
                "Reset Password Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String tempPass = authService.adminResetStudentPassword(session, s.getId());
                JOptionPane.showMessageDialog(this,
                        "Password reset successfully!\n\n" +
                                "Student: " + s.getName() + "\n" +
                                "Temporary Password: " + tempPass + "\n\n" +
                                "The student can now use this password to sign in.",
                        "Password Reset Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to reset password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
