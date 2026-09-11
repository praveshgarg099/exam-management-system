package exam_management_syatem;

import java.awt.Color;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.JScrollPane;
import javax.swing.JComboBox;

public class newdata {
	private JFrame frame;
	private JTextField q;
	private JTextField o1;
	private JTextField o2;
	private JTextField o3;
	private JTextField o4;
	private JTextField a;
	private JTextField i;
	private static JComboBox comboBox;
	private JComboBox<String> diffComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					newdata window = new newdata();
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


	/**
	 * Create the application.
	 */
	public newdata() {
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("Tahoma", Font.BOLD, 14));
		frame.getContentPane().setBackground(new Color(102, 116, 204));
		frame.setBounds(0, 0, 1850, 1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("      New Data");
		lblNewLabel.setBounds(10, 10, 376, 71);
		lblNewLabel.setIcon(AppUtils.loadImage("new (1).png"));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
		frame.getContentPane().add(lblNewLabel);

		JButton btnNewButton = new JButton("Close");
		btnNewButton.setBounds(1382, 10, 148, 64);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				adminpage1 ob = new adminpage1();
				ob.main(null);
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnNewButton.setIcon(AppUtils.loadImage("Close.png"));
		frame.getContentPane().add(btnNewButton);

		JLabel lblNewLabel_1 = new JLabel("Enter the Subject");
		lblNewLabel_1.setBounds(10, 131, 166, 30);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 17));
		frame.getContentPane().add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Question");
		lblNewLabel_2.setBounds(10, 211, 174, 30);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2);

		q = new JTextField();
		q.setBounds(208, 216, 733, 28);
		frame.getContentPane().add(q);
		q.setColumns(10);

		JLabel lblNewLabel_2_1 = new JLabel("Option 1 :");
		lblNewLabel_2_1.setBounds(10, 269, 174, 30);
		lblNewLabel_2_1.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2_1);

		o1 = new JTextField();
		o1.setBounds(208, 278, 733, 19);
		o1.setColumns(10);
		frame.getContentPane().add(o1);

		JLabel lblNewLabel_2_1_1 = new JLabel("Option 2 :");
		lblNewLabel_2_1_1.setBounds(10, 324, 174, 30);
		lblNewLabel_2_1_1.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2_1_1);

		JLabel lblNewLabel_2_1_2 = new JLabel("Option 3 :");
		lblNewLabel_2_1_2.setBounds(10, 383, 174, 30);
		lblNewLabel_2_1_2.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2_1_2);

		JLabel lblNewLabel_2_1_3 = new JLabel("Option 4 :");
		lblNewLabel_2_1_3.setBounds(10, 440, 174, 30);
		lblNewLabel_2_1_3.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2_1_3);

		JLabel lblNewLabel_2_1_4 = new JLabel("Answer");
		lblNewLabel_2_1_4.setBounds(10, 496, 174, 30);
		lblNewLabel_2_1_4.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblNewLabel_2_1_4);

		o2 = new JTextField();
		o2.setBounds(208, 333, 733, 19);
		o2.setColumns(10);
		frame.getContentPane().add(o2);

		o3 = new JTextField();
		o3.setBounds(208, 392, 733, 19);
		o3.setColumns(10);
		frame.getContentPane().add(o3);

		o4 = new JTextField();
		o4.setBounds(208, 449, 733, 19);
		o4.setColumns(10);
		frame.getContentPane().add(o4);

		a = new JTextField();
		a.setBounds(208, 498, 733, 30);
		a.setFont(new Font("Tahoma", Font.BOLD, 14));
		a.setColumns(10);
		frame.getContentPane().add(a);

		JLabel lblDiff = new JLabel("Difficulty :");
		lblDiff.setBounds(10, 545, 174, 30);
		lblDiff.setFont(new Font("Tahoma", Font.BOLD, 18));
		frame.getContentPane().add(lblDiff);

		diffComboBox = new JComboBox<>(new String[]{"MEDIUM", "EASY", "HARD"});
		diffComboBox.setBounds(208, 545, 200, 30);
		diffComboBox.setFont(new Font("Tahoma", Font.BOLD, 14));
		frame.getContentPane().add(diffComboBox);

		JButton btnNewButton_2 = new JButton("Clear");
		btnNewButton_2.setBounds(644, 623, 166, 48);
		btnNewButton_2.setIcon(AppUtils.loadImage("clear.png"));
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				i.setText(null);
				q.setText(null);
				o1.setText(null);
				o2.setText(null);
				o3.setText(null);
				o4.setText(null);
				a.setText(null);
			}
		});
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_2);

		JLabel lblNewLabel_3 = new JLabel("Id");
		lblNewLabel_3.setBounds(681, 142, 56, 19);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(lblNewLabel_3);

		i = new JTextField();
		i.setBounds(724, 131, 148, 30);
		frame.getContentPane().add(i);
		i.setColumns(10);

		JButton btnNewButton_1_1 = new JButton("Submit");
		btnNewButton_1_1.setBounds(322, 623, 164, 48);
		btnNewButton_1_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(frame, "Please select a subject.", "Validation Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				String p = comboBox.getSelectedItem().toString().trim();
				if (!AppUtils.isValidIdentifier(p)) {
					JOptionPane.showMessageDialog(frame, "Invalid subject table selected.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String id = i.getText().trim();
				String question = q.getText().trim();
				String option1 = o1.getText().trim();
				String option2 = o2.getText().trim();
				String option3 = o3.getText().trim();
				String option4 = o4.getText().trim();
				String answer = a.getText().trim();
				String diff = diffComboBox.getSelectedItem() != null ? diffComboBox.getSelectedItem().toString() : "MEDIUM";

				if (id.isEmpty() || question.isEmpty() || option1.isEmpty() || option2.isEmpty() ||
						option3.isEmpty() || option4.isEmpty() || answer.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "All fields (ID, Question, 4 Options, Answer) are required.",
							"Validation Error", JOptionPane.WARNING_MESSAGE);
					return;
				}

				try {
					exam_management_syatem.service.SubjectService subjectService = new exam_management_syatem.service.SubjectService();
					exam_management_syatem.model.Subject subject = subjectService.getSubjectByName(p);
					if (subject == null) {
						JOptionPane.showMessageDialog(frame, "Subject not found.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
					questionService.addQuestion(subject.getId(), question, option1, option2, option3, option4, answer, diff);

					JOptionPane.showMessageDialog(frame, "Question Saved Successfully!");
					i.setText(null);
					q.setText(null);
					o1.setText(null);
					o2.setText(null);
					o3.setText(null);
					o4.setText(null);
					a.setText(null);
				} catch (Exception ew) {
					JOptionPane.showMessageDialog(frame, "Error saving question: " + ew.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		btnNewButton_1_1.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_1_1);

		JButton btnNewButton_3 = new JButton("Start");
		btnNewButton_3.setBounds(471, 132, 137, 30);
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem() == null)
					return;
				String ss = comboBox.getSelectedItem().toString().trim();
				try {
					exam_management_syatem.service.SubjectService subjectService = new exam_management_syatem.service.SubjectService();
					exam_management_syatem.model.Subject subject = subjectService.getSubjectByName(ss);
					if (subject != null) {
						exam_management_syatem.service.QuestionService questionService = new exam_management_syatem.service.QuestionService();
						int count = questionService.getQuestionCountBySubjectId(subject.getId());
						i.setText(Integer.toString(count + 1));
					} else {
						i.setText("1");
					}
				} catch (Exception ee) {
					i.setText("1");
				}
			}
		});
		btnNewButton_3.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_3);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(208, 136, 159, 25);
		frame.getContentPane().add(scrollPane_1);

		comboBox = new JComboBox();
		comboBox.setEditable(true);
		scrollPane_1.setViewportView(comboBox);
	}
}
