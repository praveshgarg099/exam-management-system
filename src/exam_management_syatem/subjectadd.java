package exam_management_syatem;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
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

import exam_management_syatem.model.Subject;
import exam_management_syatem.service.SubjectService;

public class subjectadd extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;

	private JTable subjectsTable;
	private DefaultTableModel tableModel;
	private List<Subject> allSubjects;
	private final SubjectService subjectService = new SubjectService();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					subjectadd frame = new subjectadd();
					frame.setVisible(true);
					frame.loadSubjects();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public subjectadd() {
		initialize();
		loadSubjects();
	}

	public void loadSubjects() {
		tableModel.setRowCount(0);
		try {
			allSubjects = subjectService.getAllSubjects();
			for (Subject s : allSubjects) {
				tableModel.addRow(new Object[]{
						s.getId(),
						s.getName(),
						s.isActive() ? "Active" : "Inactive"
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initialize() {
		setTitle("Exam Management System - Subject Management");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1000, 600);
		setLocationRelativeTo(null);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JLabel lblHeader = new JLabel("Subject Management");
		lblHeader.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblHeader.setBounds(20, 15, 300, 30);
		contentPane.add(lblHeader);

		JButton btnBack = new JButton("Back to Admin Dashboard");
		btnBack.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnBack.setBounds(760, 15, 200, 32);
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				adminpage1.main(null);
			}
		});
		contentPane.add(btnBack);

		// Left: Add Subject Panel
		JPanel addPanel = new JPanel();
		addPanel.setBounds(20, 65, 380, 470);
		addPanel.setBorder(new TitledBorder(null, "Add New Subject", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 14), Color.BLACK));
		addPanel.setLayout(null);
		contentPane.add(addPanel);

		JLabel lblName = new JLabel("Subject Name:");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblName.setBounds(20, 40, 150, 25);
		addPanel.add(lblName);

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField.setBounds(20, 75, 335, 35);
		addPanel.add(textField);
		textField.setColumns(10);

		JButton btnAdd = new JButton("Add Subject");
		btnAdd.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnAdd.setBackground(new Color(46, 204, 113));
		btnAdd.setBounds(20, 130, 160, 40);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = textField.getText().trim();
				if (!AppUtils.isValidIdentifier(s)) {
					JOptionPane.showMessageDialog(subjectadd.this, "Invalid subject name! Only letters, numbers, and underscores are allowed.", "Validation Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					subjectService.addSubject(s);
					JOptionPane.showMessageDialog(subjectadd.this, "Subject '" + s + "' created successfully!");
					textField.setText("");
					loadSubjects();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(subjectadd.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		addPanel.add(btnAdd);

		// Right: Subjects Table Panel
		JPanel listPanel = new JPanel();
		listPanel.setBounds(420, 65, 540, 470);
		listPanel.setBorder(new TitledBorder(null, "Existing Subjects", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 14), Color.BLACK));
		listPanel.setLayout(null);
		contentPane.add(listPanel);

		String[] cols = {"ID", "Subject Name", "Status"};
		tableModel = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) { return false; }
		};

		subjectsTable = new JTable(tableModel);
		subjectsTable.setRowHeight(26);
		subjectsTable.setFont(new Font("Tahoma", Font.PLAIN, 13));
		subjectsTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
		subjectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollPane = new JScrollPane(subjectsTable);
		scrollPane.setBounds(15, 30, 510, 360);
		listPanel.add(scrollPane);

		JButton btnRename = new JButton("Rename Subject");
		btnRename.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnRename.setBounds(15, 410, 150, 35);
		btnRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = subjectsTable.getSelectedRow();
				if (row < 0 || allSubjects == null || row >= allSubjects.size()) {
					JOptionPane.showMessageDialog(subjectadd.this, "Please select a subject from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
					return;
				}
				Subject sub = allSubjects.get(row);
				String newName = JOptionPane.showInputDialog(subjectadd.this, "Enter new name for subject:", sub.getName());
				if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(sub.getName())) {
					if (!AppUtils.isValidIdentifier(newName.trim())) {
						JOptionPane.showMessageDialog(subjectadd.this, "Invalid name! Only letters, numbers, and underscores are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						subjectService.updateSubject(sub.getId(), newName.trim());
						JOptionPane.showMessageDialog(subjectadd.this, "Subject renamed successfully!");
						loadSubjects();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(subjectadd.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		listPanel.add(btnRename);

		JButton btnToggle = new JButton("Toggle Status");
		btnToggle.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnToggle.setBounds(180, 410, 150, 35);
		btnToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = subjectsTable.getSelectedRow();
				if (row < 0 || allSubjects == null || row >= allSubjects.size()) {
					JOptionPane.showMessageDialog(subjectadd.this, "Please select a subject from the table.", "Notice", JOptionPane.WARNING_MESSAGE);
					return;
				}
				Subject sub = allSubjects.get(row);
				boolean targetStatus = !sub.isActive();
				try {
					subjectService.setSubjectActive(sub.getId(), targetStatus);
					JOptionPane.showMessageDialog(subjectadd.this, "Subject status updated to " + (targetStatus ? "Active" : "Inactive") + ".");
					loadSubjects();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(subjectadd.this, ex.getMessage(), "Status Update Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		listPanel.add(btnToggle);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnRefresh.setBounds(350, 410, 110, 35);
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadSubjects();
			}
		});
		listPanel.add(btnRefresh);
	}
}
