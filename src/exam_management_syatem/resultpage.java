package exam_management_syatem;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
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
import javax.swing.table.DefaultTableModel;

import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

public class resultpage {

	private JFrame frame;
	private JTable table;
	public static DefaultTableModel model;
	private JTextField textField;
	private JTextField textField_1;
	public static Object data1;

	private JComboBox<String> subjectFilterCombo;
	private JComboBox<String> statusFilterCombo;

	private final ResultService resultService = new ResultService();
	private final StudentService studentService = new StudentService();
	private final SubjectService subjectService = new SubjectService();

	private List<ExamResult> currentResultsList = new java.util.ArrayList<>();
	private Map<Integer, Student> studentMap = new HashMap<>();
	private Map<Integer, Subject> subjectMap = new HashMap<>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					resultpage window = new resultpage();
					window.frame.setVisible(true);
					window.initFilters();
					window.loadResults();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public resultpage() {
		initialize();
	}

	public void initFilters() {
		try {
			subjectFilterCombo.removeAllItems();
			subjectFilterCombo.addItem("All Subjects");
			List<Subject> subjects = subjectService.getAllSubjects();
			for (Subject s : subjects) {
				subjectMap.put(s.getId(), s);
				subjectFilterCombo.addItem(s.getName());
			}

			List<Student> students = studentService.getAllStudents();
			for (Student st : students) {
				studentMap.put(st.getId(), st);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadResults() {
		try {
			// Refresh cache
			for (Subject s : subjectService.getAllSubjects()) subjectMap.put(s.getId(), s);
			for (Student st : studentService.getAllStudents()) studentMap.put(st.getId(), st);

			String searchTxt = textField.getText().trim();
			String selSubject = subjectFilterCombo.getSelectedItem() != null ? subjectFilterCombo.getSelectedItem().toString() : "All Subjects";
			String selStatus = statusFilterCombo.getSelectedItem() != null ? statusFilterCombo.getSelectedItem().toString() : "All Results";

			currentResultsList = resultService.searchAndFilter(searchTxt, selSubject, selStatus);

			model = (DefaultTableModel) table.getModel();
			model.setRowCount(0);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			for (ExamResult r : currentResultsList) {
				Student st = studentMap.get(r.getStudentId());
				Subject sub = subjectMap.get(r.getSubjectId());
				String stName = st != null ? st.getName() : ("Student #" + r.getStudentId());
				String subName = sub != null ? sub.getName() : ("Subject #" + r.getSubjectId());
				String dateStr = r.getSubmittedAt() != null ? sdf.format(r.getSubmittedAt()) : "N/A";

				String[] row = {
						Integer.toString(r.getId()),
						stName,
						subName,
						r.getCorrectAnswers() + " / " + r.getTotalQuestions(),
						String.format("%.2f%%", r.getPercentage()),
						r.getResult(),
						dateStr
				};
				model.addRow(row);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Exam Management System - Results & Analytics");
		frame.setBounds(100, 100, 1366, 763);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblTitle = new JLabel("Exam Results Management");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblTitle.setBounds(10, 10, 350, 30);
		frame.getContentPane().add(lblTitle);

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				adminpage1 ob = new adminpage1();
				ob.main(null);
			}
		});
		btnClose.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnClose.setBounds(1220, 10, 110, 35);
		frame.getContentPane().add(btnClose);

		// Filter Controls Panel
		JLabel lblSearch = new JLabel("Search Student:");
		lblSearch.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblSearch.setBounds(10, 55, 120, 25);
		frame.getContentPane().add(lblSearch);

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textField.setBounds(130, 55, 180, 25);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				loadResults();
			}
		});
		frame.getContentPane().add(textField);

		JLabel lblSubject = new JLabel("Subject:");
		lblSubject.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblSubject.setBounds(330, 55, 65, 25);
		frame.getContentPane().add(lblSubject);

		subjectFilterCombo = new JComboBox<>();
		subjectFilterCombo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		subjectFilterCombo.setBounds(400, 55, 160, 25);
		subjectFilterCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadResults();
			}
		});
		frame.getContentPane().add(subjectFilterCombo);

		JLabel lblStatus = new JLabel("Result:");
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblStatus.setBounds(580, 55, 60, 25);
		frame.getContentPane().add(lblStatus);

		statusFilterCombo = new JComboBox<>(new String[]{"All Results", "Pass", "Fail"});
		statusFilterCombo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		statusFilterCombo.setBounds(645, 55, 120, 25);
		statusFilterCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadResults();
			}
		});
		frame.getContentPane().add(statusFilterCombo);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnRefresh.setBounds(785, 53, 100, 28);
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText("");
				loadResults();
			}
		});
		frame.getContentPane().add(btnRefresh);

		JButton btnDetails = new JButton("View Details");
		btnDetails.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDetails.setBackground(new Color(41, 128, 185));
		btnDetails.setForeground(Color.BLACK);
		btnDetails.setBounds(900, 53, 130, 28);
		btnDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedResultDetails();
			}
		});
		frame.getContentPane().add(btnDetails);

		JButton btnDelete = new JButton("Delete Result");
		btnDelete.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnDelete.setBackground(new Color(231, 76, 60));
		btnDelete.setForeground(Color.BLACK);
		btnDelete.setBounds(1045, 53, 130, 28);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String targetId = textField_1.getText().trim();
				if (targetId.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please select a result row from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}

				int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Result Attempt ID: " + targetId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					try {
						int resultId = Integer.parseInt(targetId);
						resultService.deleteResultById(resultId);

						JOptionPane.showMessageDialog(frame, "Result Attempt Deleted Successfully!");
						textField_1.setText("");
						loadResults();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Error deleting result: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		frame.getContentPane().add(btnDelete);

		textField_1 = new JTextField();
		textField_1.setVisible(false);
		frame.getContentPane().add(textField_1);

		// Results Table
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 95, 1330, 615);
		frame.getContentPane().add(scrollPane);

		table = new JTable();
		table.setRowHeight(26);
		table.setFont(new Font("Tahoma", Font.PLAIN, 13));
		table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
		table.setModel(new DefaultTableModel(
				new Object[][] {},
				new String[] {
						"Result ID", "Student Name", "Subject", "Score (Correct/Total)", "Percentage", "Result", "Submitted At"
				}
		) {
			@Override
			public boolean isCellEditable(int r, int c) { return false; }
		});

		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow >= 0) {
					data1 = table.getValueAt(selectedRow, 0);
					if (data1 != null) {
						textField_1.setText(data1.toString());
					}
				}
			}
		});
		scrollPane.setViewportView(table);
	}

	private void showSelectedResultDetails() {
		int row = table.getSelectedRow();
		if (row < 0 || currentResultsList == null || row >= currentResultsList.size()) {
			JOptionPane.showMessageDialog(frame, "Please select a result row from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
			return;
		}

		ExamResult r = currentResultsList.get(row);
		Student st = studentMap.get(r.getStudentId());
		Subject sub = subjectMap.get(r.getSubjectId());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		JDialog dialog = new JDialog(frame, "Exam Result Details - Attempt #" + r.getId(), true);
		dialog.setSize(480, 420);
		dialog.setLocationRelativeTo(frame);
		dialog.setLayout(new java.awt.BorderLayout(10, 10));

		JPanel infoCard = new JPanel(new GridLayout(9, 2, 10, 8));
		infoCard.setBorder(BorderFactory.createCompoundBorder(
				new EmptyBorder(15, 20, 15, 20),
				BorderFactory.createTitledBorder("Attempt Information")
		));

		addDetailRow(infoCard, "Result ID:", String.valueOf(r.getId()));
		addDetailRow(infoCard, "Student Name:", st != null ? st.getName() : ("Student #" + r.getStudentId()));
		addDetailRow(infoCard, "Student Mobile:", st != null ? st.getMobile() : "N/A");
		addDetailRow(infoCard, "Subject:", sub != null ? sub.getName() : ("Subject #" + r.getSubjectId()));
		addDetailRow(infoCard, "Total Questions:", String.valueOf(r.getTotalQuestions()));
		addDetailRow(infoCard, "Correct Answers:", String.valueOf(r.getCorrectAnswers()));
		addDetailRow(infoCard, "Final Percentage:", String.format("%.2f%%", r.getPercentage()));
		addDetailRow(infoCard, "Overall Result:", r.getResult());
		addDetailRow(infoCard, "Submitted At:", r.getSubmittedAt() != null ? sdf.format(r.getSubmittedAt()) : "N/A");

		dialog.add(infoCard, java.awt.BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 10));
		JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(e -> dialog.dispose());
		bottomPanel.add(closeBtn);
		dialog.add(bottomPanel, java.awt.BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	private void addDetailRow(JPanel p, String label, String val) {
		JLabel l = new JLabel(label);
		l.setFont(new Font("Tahoma", Font.BOLD, 13));
		p.add(l);
		JLabel v = new JLabel(val);
		v.setFont(new Font("Tahoma", Font.PLAIN, 13));
		p.add(v);
	}
}
