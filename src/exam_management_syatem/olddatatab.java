package exam_management_syatem;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.AncestorEvent;
import javax.swing.JComboBox;
import javax.swing.SpringLayout;
import javax.swing.JSeparator;
import javax.swing.JTable;
import java.awt.Choice;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JScrollBar;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class olddatatab {

	private JFrame frame;
	public static  JComboBox comboBox;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private static JComboBox<String> diffChoice;
	private static JTable tblData;
	private JTextField textField_6;
	 public static Object data1 ;
	 private  JLabel id1;
	 private  JLabel q2;
	 private  JLabel o1;
	 private  JLabel o2;
	 private  JLabel o3;
	 private  JLabel o4;
	 private  JLabel a1;
	 public static DefaultTableModel model;
	 private JTextField textField_7;
	 private static JLabel lblNewLabel_2;

	public static void fgh() {
		if (comboBox == null || comboBox.getSelectedItem() == null) return;
		String s1 = comboBox.getSelectedItem().toString().trim();
		
		try {
			exam_management_syatem.service.SubjectService subjectService = new exam_management_syatem.service.SubjectService();
			exam_management_syatem.model.Subject subject = subjectService.getSubjectByName(s1);
			if (subject == null) return;

			exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
			java.util.List<exam_management_syatem.model.Question> questions = questionService.getQuestionsBySubjectId(subject.getId());

			model = (DefaultTableModel) tblData.getModel();
			model.setRowCount(0);
			
			for (exam_management_syatem.model.Question q : questions) {
				String[] row = {
					Integer.toString(q.getId()),
					q.getQuestionText(),
					q.getOption1(),
					q.getOption2(),
					q.getOption3(),
					q.getOption4(),
					q.getCorrectAnswer(),
					q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM",
					q.isActive() ? "Active" : "Inactive"
				};
				model.addRow(row);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					olddatatab window = new olddatatab();
					window.frame.setVisible(true);
					exam_management_syatem.service.SubjectService subjectService = new exam_management_syatem.service.SubjectService();
					java.util.List<exam_management_syatem.model.Subject> subjects = subjectService.getActiveSubjects();
					for (exam_management_syatem.model.Subject s : subjects) {
						comboBox.addItem(s.getName());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void search(String str)
	{
		model= (DefaultTableModel) tblData.getModel();
		TableRowSorter<DefaultTableModel> trs=new TableRowSorter<>(model);
		tblData.setRowSorter(trs);
		trs.setRowFilter(RowFilter.regexFilter(str));
	}

	/**
	 * Create the application.
	 */
	public olddatatab() {
		initialize();
		
		
	}
	
	
	
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(102, 116, 204));
		frame.setBackground(new Color(255, 255, 255));
		frame.setBounds(0, 0, 1850, 1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("     Old Data");
		lblNewLabel.setBounds(20, 26, 254, 60);
		lblNewLabel.setIcon(AppUtils.loadImage("301-3019631_historical-data-icon (2) (3).png"));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
		frame.getContentPane().add(lblNewLabel);
		
		JButton btnNewButton = new JButton("");
		btnNewButton.setBounds(1475, 10, 55, 48);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				adminpage1 ob = new adminpage1();
				ob.main(null);
			}
		});
		btnNewButton.setIcon(AppUtils.loadImage("Close.png"));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Subject");
		lblNewLabel_1.setBounds(578, 26, 91, 32);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 19));
		frame.getContentPane().add(lblNewLabel_1);
		
		JButton n = new JButton("Search");
		n.setBounds(1036, 32, 113, 25);
		n.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fgh();
			}
		});
		n.setFont(new Font("Tahoma", Font.BOLD, 14));
		frame.getContentPane().add(n);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(713, 33, 282, 25);
		frame.getContentPane().add(scrollPane_1);
		
		comboBox = new JComboBox();
		scrollPane_1.setViewportView(comboBox);
		comboBox.setEditable(true);
		
		q2 = new JLabel("QUESTION");
		q2.setForeground(new Color(64, 0, 64));
		q2.setFont(new Font("Times New Roman", Font.BOLD, 20));
		q2.setBounds(10, 178, 131, 31);
		frame.getContentPane().add(q2);
		
		textField = new JTextField();
		textField.setColumns(10);
		textField.setBounds(151, 180, 239, 31);
		frame.getContentPane().add(textField);
		
		o1 = new JLabel("OPTION 1");
		o1.setForeground(new Color(64, 0, 64));
		o1.setFont(new Font("Times New Roman", Font.BOLD, 20));
		o1.setBounds(10, 236, 131, 32);
		frame.getContentPane().add(o1);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(151, 239, 239, 31);
		frame.getContentPane().add(textField_1);
		
		o2 = new JLabel("OPTION 2");
		o2.setForeground(new Color(64, 0, 64));
		o2.setFont(new Font("Times New Roman", Font.BOLD, 20));
		o2.setBounds(10, 293, 131, 32);
		frame.getContentPane().add(o2);
		
		o3 = new JLabel("OPTION 3");
		o3.setForeground(new Color(64, 0, 64));
		o3.setFont(new Font("Times New Roman", Font.BOLD, 20));
		o3.setBounds(10, 349, 131, 32);
		frame.getContentPane().add(o3);
		
		o4 = new JLabel("OPTION 4");
		o4.setForeground(new Color(64, 0, 64));
		o4.setFont(new Font("Times New Roman", Font.BOLD, 20));
		o4.setBounds(10, 406, 131, 32);
		frame.getContentPane().add(o4);
		
		a1 = new JLabel("ANSWER");
		a1.setForeground(new Color(64, 0, 64));
		a1.setFont(new Font("Times New Roman", Font.BOLD, 20));
		a1.setBounds(10, 473, 102, 32);
		frame.getContentPane().add(a1);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(151, 296, 239, 31);
		frame.getContentPane().add(textField_2);
		
		textField_3 = new JTextField();
		textField_3.setColumns(10);
		textField_3.setBounds(151, 352, 239, 31);
		frame.getContentPane().add(textField_3);
		
		textField_4 = new JTextField();
		textField_4.setColumns(10);
		textField_4.setBounds(151, 409, 239, 31);
		frame.getContentPane().add(textField_4);
		
		textField_5 = new JTextField();
		textField_5.setColumns(10);
		textField_5.setBounds(151, 476, 239, 31);
		frame.getContentPane().add(textField_5);

		JLabel lblDiff = new JLabel("DIFFICULTY");
		lblDiff.setForeground(new Color(64, 0, 64));
		lblDiff.setFont(new Font("Times New Roman", Font.BOLD, 18));
		lblDiff.setBounds(10, 525, 130, 31);
		frame.getContentPane().add(lblDiff);

		diffChoice = new JComboBox<>(new String[]{"MEDIUM", "EASY", "HARD"});
		diffChoice.setFont(new Font("Tahoma", Font.BOLD, 14));
		diffChoice.setBounds(151, 525, 239, 31);
		frame.getContentPane().add(diffChoice);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(541, 125, 904, 566);
		frame.getContentPane().add(scrollPane);
		
		tblData = new JTable();
		tblData.setModel(new DefaultTableModel(
			new Object[][] {},
			new String[] {
				"ID", "QUESTION", "OPTION 1", "OPTION 2", "OPTION 3", "OPTION 4", "ANSWER", "DIFFICULTY", "STATUS"
			}
		));
		ListSelectionModel selectionModel = tblData.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selectedRow = tblData.getSelectedRow();
					if (selectedRow >= 0) {
						data1 = tblData.getValueAt(selectedRow, 0);
						Object data2 = tblData.getValueAt(selectedRow, 1);
						Object data3 = tblData.getValueAt(selectedRow, 2);
						Object data4 = tblData.getValueAt(selectedRow, 3);
						Object data5 = tblData.getValueAt(selectedRow, 4);
						Object data6 = tblData.getValueAt(selectedRow, 5);
						Object data7 = tblData.getValueAt(selectedRow, 6);
						Object data8 = tblData.getValueAt(selectedRow, 7);
						
						textField_6.setText(data1 != null ? data1.toString() : "");
						textField.setText(data2 != null ? data2.toString() : "");
						textField_1.setText(data3 != null ? data3.toString() : "");
						textField_2.setText(data4 != null ? data4.toString() : "");
						textField_3.setText(data5 != null ? data5.toString() : "");
						textField_4.setText(data6 != null ? data6.toString() : "");
						textField_5.setText(data7 != null ? data7.toString() : "");
						if (data8 != null && diffChoice != null) {
							diffChoice.setSelectedItem(data8.toString());
						}
					}
				}
			}
		});
		tblData.getColumnModel().getColumn(0).setPreferredWidth(26);
		tblData.getColumnModel().getColumn(1).setPreferredWidth(87);
		tblData.getColumnModel().getColumn(6).setPreferredWidth(85);
		scrollPane.setViewportView(tblData);
		
		JButton btnNewButton_1 = new JButton("UPDATE");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(frame, "Please select a subject.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				String s1 = comboBox.getSelectedItem().toString().trim();
				if (!AppUtils.isValidIdentifier(s1)) {
					JOptionPane.showMessageDialog(frame, "Invalid subject table selected.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (data1 == null || textField_6.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please select a question from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					int questionId = Integer.parseInt(textField_6.getText().trim());
					String diff = diffChoice.getSelectedItem() != null ? diffChoice.getSelectedItem().toString() : "MEDIUM";
					exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
					questionService.updateQuestion(
						questionId,
						textField.getText().trim(),
						textField_1.getText().trim(),
						textField_2.getText().trim(),
						textField_3.getText().trim(),
						textField_4.getText().trim(),
						textField_5.getText().trim(),
						diff
					);
					JOptionPane.showMessageDialog(frame, "Question Updated Successfully!");
					fgh();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error updating question: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 19));
		btnNewButton_1.setBounds(43, 603, 121, 38);
		frame.getContentPane().add(btnNewButton_1);
		
		JButton btnToggleActive = new JButton("TOGGLE STATUS");
		btnToggleActive.setBounds(390, 603, 140, 38);
		btnToggleActive.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnToggleActive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String targetId = textField_6.getText().trim();
				if (targetId.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please select a question from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					int qId = Integer.parseInt(targetId);
					int selectedRow = tblData.getSelectedRow();
					boolean isCurrentlyActive = true;
					if (selectedRow >= 0) {
						Object statusVal = tblData.getValueAt(selectedRow, 8);
						isCurrentlyActive = "Active".equalsIgnoreCase(statusVal != null ? statusVal.toString() : "");
					}
					exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
					questionService.setQuestionActive(qId, !isCurrentlyActive);
					JOptionPane.showMessageDialog(frame, "Question status updated to " + (!isCurrentlyActive ? "Active" : "Inactive") + ".");
					fgh();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Error toggling question status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		frame.getContentPane().add(btnToggleActive);

		
		JButton btnNewButton_2 = new JButton("DELETE");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(frame, "Please select a subject.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				String s1 = comboBox.getSelectedItem().toString().trim();
				String targetId = textField_6.getText().trim();
				if (targetId.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please select a question from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Question ID " + targetId + " from subject " + s1 + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					try {
						int questionId = Integer.parseInt(targetId);
						exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
						questionService.deleteQuestion(questionId);
						
						JOptionPane.showMessageDialog(frame, "Question Deleted Successfully!");
						textField_6.setText("");
						textField.setText("");
						textField_1.setText("");
						textField_2.setText("");
						textField_3.setText("");
						textField_4.setText("");
						textField_5.setText("");
						data1 = null;
						fgh();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame, "Error deleting question: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 19));
		btnNewButton_2.setBounds(238, 603, 131, 38);
		frame.getContentPane().add(btnNewButton_2);
		
		id1 = new JLabel("ID");
		id1.setForeground(new Color(64, 0, 64));
		id1.setFont(new Font("Times New Roman", Font.BOLD, 20));
		id1.setBounds(10, 106, 72, 31);
		frame.getContentPane().add(id1);
		
		textField_6 = new JTextField();
		textField_6.setColumns(10);
		textField_6.setBounds(151, 108, 239, 31);
		frame.getContentPane().add(textField_6);
		
		textField_7 = new JTextField();
		textField_7.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String searchString = textField_7.getText();
				search(searchString);
			}
		});
		textField_7.setBounds(713, 73, 296, 19);
		frame.getContentPane().add(textField_7);
		textField_7.setColumns(10);
		
		JLabel lblNewLabel_1_1 = new JLabel("Search");
		lblNewLabel_1_1.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel_1_1.setBounds(578, 68, 91, 32);
		frame.getContentPane().add(lblNewLabel_1_1);
		
		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setBounds(0, 703, 17, 13);
		frame.getContentPane().add(lblNewLabel_2);
	}
}




