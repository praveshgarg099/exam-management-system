package exam_management_syatem.ui.views.student;

import exam_management_syatem.model.Student;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.AuthenticationService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.ui.components.AppButton;
import exam_management_syatem.ui.components.AppCard;
import exam_management_syatem.ui.components.AppPasswordField;
import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;

public class StudentProfileView extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private final AuthenticationService authService;

    private JLabel nameValueLabel;
    private JLabel usernameValueLabel;
    private JLabel mobileValueLabel;
    private JLabel emailValueLabel;
    private JLabel aadharValueLabel;
    private JLabel dobValueLabel;
    private JLabel statusValueLabel;

    private AppPasswordField currentPassField;
    private AppPasswordField newPassField;
    private AppPasswordField confirmPassField;
    private AppButton updatePasswordBtn;

    public StudentProfileView(UserSession session) {
        this.session = session;
        this.studentService = new StudentService();
        this.authService = new AuthenticationService();

        initComponent();
        loadProfileData();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(Spacing.XXL, Spacing.XXL, Spacing.XXL, Spacing.XXL));

        // Page Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel pageTitle = new JLabel("Profile & Security");
        pageTitle.setFont(Typography.H1);
        pageTitle.setForeground(Colors.TEXT_PRIMARY);

        JLabel pageSubtitle = new JLabel("View your registered student credentials and update your portal password.");
        pageSubtitle.setFont(Typography.BODY);
        pageSubtitle.setForeground(Colors.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(pageTitle);
        titleBlock.add(Box.createVerticalStrut(Spacing.XS));
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(Spacing.XL));

        // Two Cards Side-by-Side: Profile Details and Password Security
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, Spacing.XL, Spacing.XL));
        cardsGrid.setOpaque(false);

        // Card 1: Verified Personal Information
        AppCard infoCard = new AppCard();
        infoCard.setLayout(new BorderLayout(Spacing.LG, Spacing.LG));

        JLabel infoTitle = new JLabel("Personal Information");
        infoTitle.setFont(Typography.H2);
        infoTitle.setForeground(Colors.TEXT_PRIMARY);
        infoCard.add(infoTitle, BorderLayout.NORTH);

        JPanel infoFieldsPanel = new JPanel(new GridBagLayout());
        infoFieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Spacing.SM, Spacing.SM, Spacing.SM, Spacing.SM);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        nameValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Full Name", nameValueLabel);

        usernameValueLabel = new JLabel(session != null ? session.getUsername() : "—");
        addProfileRow(infoFieldsPanel, gbc, row++, "Username", usernameValueLabel);

        mobileValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Mobile Number", mobileValueLabel);

        emailValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Email Address", emailValueLabel);

        aadharValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Aadhar Number", aadharValueLabel);

        dobValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Date of Birth", dobValueLabel);

        statusValueLabel = new JLabel("Loading...");
        addProfileRow(infoFieldsPanel, gbc, row++, "Account Status", statusValueLabel);

        infoCard.add(infoFieldsPanel, BorderLayout.CENTER);
        cardsGrid.add(infoCard);

        // Card 2: Self-Service Password Security
        AppCard securityCard = new AppCard();
        securityCard.setLayout(new BorderLayout(Spacing.LG, Spacing.LG));

        JLabel secTitle = new JLabel("Change Password");
        secTitle.setFont(Typography.H2);
        secTitle.setForeground(Colors.TEXT_PRIMARY);
        securityCard.add(secTitle, BorderLayout.NORTH);

        JPanel passForm = new JPanel();
        passForm.setLayout(new BoxLayout(passForm, BoxLayout.Y_AXIS));
        passForm.setOpaque(false);

        JLabel curLbl = new JLabel("Current Password");
        curLbl.setFont(Typography.CAPTION);
        curLbl.setForeground(Colors.TEXT_MUTED);
        currentPassField = new AppPasswordField();

        JLabel newLbl = new JLabel("New Password (min 6 characters)");
        newLbl.setFont(Typography.CAPTION);
        newLbl.setForeground(Colors.TEXT_MUTED);
        newPassField = new AppPasswordField();

        JLabel confLbl = new JLabel("Confirm New Password");
        confLbl.setFont(Typography.CAPTION);
        confLbl.setForeground(Colors.TEXT_MUTED);
        confirmPassField = new AppPasswordField();

        passForm.add(curLbl);
        passForm.add(Box.createVerticalStrut(Spacing.XS));
        passForm.add(currentPassField);
        passForm.add(Box.createVerticalStrut(Spacing.MD));

        passForm.add(newLbl);
        passForm.add(Box.createVerticalStrut(Spacing.XS));
        passForm.add(newPassField);
        passForm.add(Box.createVerticalStrut(Spacing.MD));

        passForm.add(confLbl);
        passForm.add(Box.createVerticalStrut(Spacing.XS));
        passForm.add(confirmPassField);
        passForm.add(Box.createVerticalStrut(Spacing.LG));

        updatePasswordBtn = new AppButton("Update Password", AppButton.ButtonStyle.PRIMARY);
        updatePasswordBtn.addActionListener(e -> handleChangePassword());
        passForm.add(updatePasswordBtn);

        securityCard.add(passForm, BorderLayout.CENTER);
        cardsGrid.add(securityCard);

        contentPanel.add(cardsGrid);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel title = new JLabel(label);
        title.setFont(Typography.LABEL);
        title.setForeground(Colors.TEXT_PRIMARY);
        panel.add(title, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.65;
        valueLabel.setFont(Typography.BODY);
        valueLabel.setForeground(Colors.TEXT_MUTED);
        panel.add(valueLabel, gbc);
    }

    public void loadProfileData() {
        if (session == null || session.getStudentId() == null) {
            return;
        }

        SwingWorker<Student, Void> worker = new SwingWorker<>() {
            @Override
            protected Student doInBackground() throws Exception {
                return studentService.getStudentById(session, session.getStudentId());
            }

            @Override
            protected void done() {
                try {
                    Student student = get();
                    if (student != null) {
                        nameValueLabel.setText(student.getName() != null ? student.getName() : "—");
                        mobileValueLabel.setText(student.getMobile() != null ? student.getMobile() : "—");
                        emailValueLabel.setText(student.getEmail() != null ? student.getEmail() : "—");
                        aadharValueLabel.setText(student.getAadharNo() != null ? student.getAadharNo() : "—");
                        dobValueLabel.setText(student.getDateOfBirth() != null ? student.getDateOfBirth() : "—");
                        statusValueLabel.setText(student.isActive() ? "Active (Verified)" : "Inactive");
                        statusValueLabel.setForeground(student.isActive() ? Colors.STATUS_SUCCESS : Colors.STATUS_ERROR);
                    }
                } catch (Exception e) {
                    nameValueLabel.setText("Failed to load");
                    nameValueLabel.setForeground(Colors.STATUS_ERROR);
                }
            }
        };
        worker.execute();
    }

    private void handleChangePassword() {
        char[] curChars = currentPassField.getPassword();
        char[] newChars = newPassField.getPassword();
        char[] confChars = confirmPassField.getPassword();

        String cur = new String(curChars).trim();
        String newP = new String(newChars).trim();
        String confP = new String(confChars).trim();

        // Immediate memory hygiene
        Arrays.fill(curChars, '\0');
        Arrays.fill(newChars, '\0');
        Arrays.fill(confChars, '\0');

        if (cur.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newP.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newP.equals(confP)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newP.equals(cur)) {
            JOptionPane.showMessageDialog(this, "New password cannot be identical to your current password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updatePasswordBtn.setEnabled(false);
        updatePasswordBtn.setText("Updating...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.changeOwnPassword(session, cur, newP);
                return null;
            }

            @Override
            protected void done() {
                updatePasswordBtn.setEnabled(true);
                updatePasswordBtn.setText("Update Password");
                try {
                    get();
                    JOptionPane.showMessageDialog(StudentProfileView.this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    currentPassField.setText("");
                    newPassField.setText("");
                    confirmPassField.setText("");
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(StudentProfileView.this, msg, "Password Update Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
