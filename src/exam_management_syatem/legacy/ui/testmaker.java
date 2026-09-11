package exam_management_syatem.legacy.ui;

import exam_management_syatem.AppUtils;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

public class testmaker {

	private JFrame frame;
	private JTextField textField; // duration
	private JTextField textField_1; // num questions
	private static JComboBox<String> comboBox; // subject
	private static JComboBox<String> comboBox_1; // username
	private JTextField textField_2; // passing %
	
	private JTable schedulesTable;
	private DefaultTableModel scheduleTableModel;
	private List<ExamSchedule> allSchedules;
	private Map<Integer, Student> studentMap = new HashMap<>();
	private Map<Integer, Subject> subjectMap = new HashMap<>();

	private final ExamService examService = new ExamService();
	private final StudentService studentService = new StudentService();
	private final SubjectService subjectService = new SubjectService();
	private final exam_management_syatem.security.UserSession session;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testmaker window = new testmaker();
					window.frame.setVisible(true);
					window.loadDropdowns();
					window.loadSchedules();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public testmaker() {
		this(adminpage1.getAdminSession());
	}

	public testmaker(exam_management_syatem.security.UserSession session) {
		this.session = session;
		initialize();
	}

	public void loadDropdowns() {
		try {
			comboBox_1.removeAllItems();
			List<Student> students = studentService.getAllStudents(session);
			for (Student st : students) {
				studentMap.put(st.getId(), st);
				if (st.isActive()) {
					String cleanName = st.getName().trim().replaceAll("\\s+", "");
					if (cleanName.length() < 4) cleanName = (cleanName + "aaaa").substring(0, 4);
					else cleanName = cleanName.substring(0, 4);
					String last4 = st.getAadharNo().length() >= 4 ? st.getAadharNo().substring(st.getAadharNo().length() - 4) : "0000";
					String uName = (cleanName + last4).toLowerCase();
					comboBox_1.addItem(uName);
				}
			}

			comboBox.removeAllItems();
			List<Subject> subjects = subjectService.getActiveSubjects(session);
			for (Subject sub : subjects) {
				subjectMap.put(sub.getId(), sub);
				comboBox.addItem(sub.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadSchedules() {
		scheduleTableModel.setRowCount(0);
		try {
			// Refresh cache
			for (Student st : studentService.getAllStudents(session)) studentMap.put(st.getId(), st);
			for (Subject sub : subjectService.getAllSubjects(session)) subjectMap.put(sub.getId(), sub);

			allSchedules = examService.getAllSchedules(session);
			for (ExamSchedule es : allSchedules) {
				Student st = studentMap.get(es.getStudentId());
				Subject sub = subjectMap.get(es.getSubjectId());
				String stName = st != null ? st.getName() : ("Student #" + es.getStudentId());
				String subName = sub != null ? sub.getName() : ("Subject #" + es.getSubjectId());

				scheduleTableModel.addRow(new Object[]{
						es.getId(),
						stName,
						subName,
						es.getDurationMinutes() + " mins",
						es.getTotalQuestions(),
						es.getPassingPercentage() + "%",
						es.getStatus()
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(102, 116, 204));
		frame.setBounds(0, 0, 1850, 1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("      Test Maker & Schedule Management");
		lblNewLabel.setIcon(AppUtils.loadImage("checklist (1).png"));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblNewLabel.setBounds(10, 10, 550, 55);
		frame.getContentPane().add(lblNewLabel);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				adminpage1 ob = new adminpage1();
				ob.main(null);
			}
		});
		btnClose.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnClose.setBounds(1379, 10, 120, 50);
		frame.getContentPane().add(btnClose);
		
		// Left Side: Schedule New Exam Form
		JPanel formPanel = new JPanel();
		formPanel.setBounds(30, 90, 500, 550);
		formPanel.setBackground(new Color(102, 116, 204));
		formPanel.setBorder(new TitledBorder(null, "Create New Exam Schedule", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 16), Color.BLACK));
		formPanel.setLayout(null);
		frame.getContentPane().add(formPanel);

		JLabel lblUsername = new JLabel("Student Username:");
		lblUsername.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblUsername.setBounds(20, 40, 200, 30);
		formPanel.add(lblUsername);

		comboBox_1 = new JComboBox<>();
		comboBox_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		comboBox_1.setBounds(230, 40, 240, 30);
		formPanel.add(comboBox_1);

		JLabel lblSubject = new JLabel("Subject:");
		lblSubject.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblSubject.setBounds(20, 100, 200, 30);
		formPanel.add(lblSubject);

		comboBox = new JComboBox<>();
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 14));
		comboBox.setBounds(230, 100, 240, 30);
		formPanel.add(comboBox);

		JLabel lblPassing = new JLabel("Passing Score (%):");
		lblPassing.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblPassing.setBounds(20, 160, 200, 30);
		formPanel.add(lblPassing);

		textField_2 = new JTextField("40.0");
		textField_2.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField_2.setBounds(230, 160, 240, 30);
		formPanel.add(textField_2);

		JLabel lblTime = new JLabel("Duration (mins):");
		lblTime.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblTime.setBounds(20, 220, 200, 30);
		formPanel.add(lblTime);

		textField = new JTextField("10");
		textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField.setBounds(230, 220, 240, 30);
		formPanel.add(textField);

		JLabel lblQues = new JLabel("Question Count:");
		lblQues.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblQues.setBounds(20, 280, 200, 30);
		formPanel.add(lblQues);

		textField_1 = new JTextField("5");
		textField_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField_1.setBounds(230, 280, 240, 30);
		formPanel.add(textField_1);

		JButton btnSubmit = new JButton("Schedule Exam");
		btnSubmit.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnSubmit.setBackground(new Color(46, 204, 113));
		btnSubmit.setBounds(140, 360, 200, 45);
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() == null || comboBox_1.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(frame, "Please select both a Subject and a Student Username.", "Validation Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				String subName = comboBox.getSelectedItem().toString().trim();
				String uName = comboBox_1.getSelectedItem().toString().trim();
				String timeStr = textField.getText().trim();
				String quesStr = textField_1.getText().trim();
				String passStr = textField_2.getText().trim();

				if (subName.isEmpty() || uName.isEmpty() || timeStr.isEmpty() || quesStr.isEmpty() || passStr.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "All schedule fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
					return;
				}

				try {
					int timeVal = Integer.parseInt(timeStr);
					int quesVal = Integer.parseInt(quesStr);
					double passVal = Double.parseDouble(passStr);

					examService.scheduleExam(session, uName, subName, timeVal, quesVal, passVal);

					JOptionPane.showMessageDialog(frame, "Test Scheduled Successfully for " + uName + "!");
					textField.setText("10");
					textField_1.setText("5");
					textField_2.setText("40.0");
					loadSchedules();
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(frame, "Duration, Question Count, and Passing Percentage must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
				} catch (Exception ew) {
					JOptionPane.showMessageDialog(frame, "Error saving test schedule: " + ew.getMessage(), "Schedule Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		formPanel.add(btnSubmit);

		// Right Side: Scheduled Exams Table & Lifecycle Management
		JPanel tablePanel = new JPanel();
		tablePanel.setBounds(560, 90, 940, 550);
		tablePanel.setBackground(new Color(102, 116, 204));
		tablePanel.setBorder(new TitledBorder(null, "Existing Exam Schedules", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 16), Color.BLACK));
		tablePanel.setLayout(null);
		frame.getContentPane().add(tablePanel);

		String[] cols = {"ID", "Student", "Subject", "Duration", "Questions", "Passing %", "Status"};
		scheduleTableModel = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) { return false; }
		};

		schedulesTable = new JTable(scheduleTableModel);
		schedulesTable.setRowHeight(26);
		schedulesTable.setFont(new Font("Tahoma", Font.PLAIN, 13));
		schedulesTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
		schedulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane tableScroll = new JScrollPane(schedulesTable);
		tableScroll.setBounds(15, 30, 910, 430);
		tablePanel.add(tableScroll);

		JButton btnCancelExam = new JButton("Cancel Selected Exam");
		btnCancelExam.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnCancelExam.setBackground(new Color(231, 76, 60));
		btnCancelExam.setBounds(15, 480, 200, 38);
		btnCancelExam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = schedulesTable.getSelectedRow();
				if (row < 0 || allSchedules == null || row >= allSchedules.size()) {
					JOptionPane.showMessageDialog(frame, "Please select an exam schedule from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
					return;
				}
				ExamSchedule es = allSchedules.get(row);
				if (!"SCHEDULED".equalsIgnoreCase(es.getStatus())) {
					JOptionPane.showMessageDialog(frame, "Only exams with status 'SCHEDULED' can be cancelled.\nCurrent status: " + es.getStatus(), "Invalid Operation", JOptionPane.WARNING_MESSAGE);
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to cancel Schedule #" + es.getId() + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					try {
						examService.cancelExam(session, es.getId());
						JOptionPane.showMessageDialog(frame, "Exam schedule #" + es.getId() + " cancelled successfully.");
						loadSchedules();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "Error cancelling exam: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		tablePanel.add(btnCancelExam);

		JButton btnEditSchedule = new JButton("Edit Selected Exam");
		btnEditSchedule.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnEditSchedule.setBounds(230, 480, 180, 38);
		btnEditSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = schedulesTable.getSelectedRow();
				if (row < 0 || allSchedules == null || row >= allSchedules.size()) {
					JOptionPane.showMessageDialog(frame, "Please select an exam schedule from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
					return;
				}
				ExamSchedule es = allSchedules.get(row);
				if (!"SCHEDULED".equalsIgnoreCase(es.getStatus())) {
					JOptionPane.showMessageDialog(frame, "Only exams with status 'SCHEDULED' can be edited.\nCurrent status: " + es.getStatus(), "Invalid Operation", JOptionPane.WARNING_MESSAGE);
					return;
				}
				showEditScheduleDialog(es);
			}
		});
		tablePanel.add(btnEditSchedule);

		JButton btnRefresh = new JButton("Refresh List");
		btnRefresh.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnRefresh.setBounds(430, 480, 130, 38);
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadSchedules();
			}
		});
		tablePanel.add(btnRefresh);
	}

	private void showEditScheduleDialog(final ExamSchedule es) {
		JDialog dialog = new JDialog(frame, "Edit Exam Schedule #" + es.getId(), true);
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(frame);
		dialog.setLayout(new java.awt.BorderLayout(10, 10));

		JPanel form = new JPanel(new java.awt.GridLayout(4, 2, 10, 12));
		form.setBorder(new EmptyBorder(20, 20, 10, 20));

		final JTextField durField = new JTextField(String.valueOf(es.getDurationMinutes()));
		final JTextField qField = new JTextField(String.valueOf(es.getTotalQuestions()));
		final JTextField passField = new JTextField(String.valueOf(es.getPassingPercentage()));

		form.add(new JLabel("Schedule ID:"));
		form.add(new JLabel(String.valueOf(es.getId())));
		form.add(new JLabel("Duration (mins):")); form.add(durField);
		form.add(new JLabel("Total Questions:")); form.add(qField);
		form.add(new JLabel("Passing Score (%):")); form.add(passField);

		dialog.add(form, java.awt.BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 10));
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(e -> dialog.dispose());
		btnPanel.add(cancelBtn);

		JButton saveBtn = new JButton("Save Changes");
		saveBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
		saveBtn.setBackground(new Color(41, 128, 185));
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int duration = Integer.parseInt(durField.getText().trim());
					int questions = Integer.parseInt(qField.getText().trim());
					double passing = Double.parseDouble(passField.getText().trim());

					examService.updateExamSchedule(session, es.getId(), duration, questions, passing);
					JOptionPane.showMessageDialog(dialog, "Exam schedule updated successfully!");
					dialog.dispose();
					loadSchedules();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.", "Error", JOptionPane.WARNING_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnPanel.add(saveBtn);
		dialog.add(btnPanel, java.awt.BorderLayout.SOUTH);

		dialog.setVisible(true);
	}
}
