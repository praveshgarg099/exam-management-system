package exam_management_syatem.ui.views.exam;

import exam_management_syatem.AppUtils;
import exam_management_syatem.legacy.ui.StudentDashboard;
import exam_management_syatem.index;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import exam_management_syatem.model.ExamAttempt;
import exam_management_syatem.model.ExamAttemptQuestion;
import exam_management_syatem.model.ExamResult;
import exam_management_syatem.model.ExamSchedule;
import exam_management_syatem.model.Question;
import exam_management_syatem.model.Student;
import exam_management_syatem.model.Subject;
import java.util.ArrayList;
import exam_management_syatem.security.UserSession;
import exam_management_syatem.service.ExamService;
import exam_management_syatem.service.QuestionService;
import exam_management_syatem.service.ResultService;
import exam_management_syatem.service.StudentService;
import exam_management_syatem.service.SubjectService;

public class testtake extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel_1;
	private JLabel l1;
	private JLabel lblNewLabel_1;
	
	private JLabel lblNewLabel_9;
	private JLabel lblNewLabel_6;
	private JLabel lblNewLabel_7;
	private JLabel lblNewLabel_11;
	private JLabel lblNewLabel_3;
	private JLabel lblNewLabel_8;
	public int question1 = 1;
	private JLabel p1;
	
	private JLabel p2;
	private JLabel pr;
	private JButton btnNewButton;
	private JButton btnNewButton_3;
	private JButton btnNewButton_1;
	
	public String answer;
	private Timer timer;
	public int marks = 0;

	private JLabel lblNewLabel_2;
	private JLabel lblProgress;
	
	private JRadioButton c1;
	private JRadioButton c2;
	private JRadioButton c3;
	private JRadioButton c4;
	private ButtonGroup optionGroup = new ButtonGroup();
	
	private Map<Integer, String> userAnswers = new HashMap<>();
	private boolean isSubmitted = false;

	static String pass = "";
	
	private JLabel lblNewLabel_10;
	private JLabel lblNewLabel_12;
	public int second = 0, minute = 0;
	public String ddSecond, ddMinute;	
	public DecimalFormat dFormat = new DecimalFormat("00");
	
	private JPanel panel;
	private JTextField textField_2;

	private static UserSession currentSession;
	private static ExamSchedule currentSchedule;
	private List<Question> sessionQuestions;
	private ExamSchedule activeSchedule;
	private ExamAttempt activeAttempt;
	private List<ExamAttemptQuestion> attemptQuestions;

	public static void main(String[] args) {
		mainWithSession(null);
	}

	public static void mainWithSession(final UserSession session) {
		currentSession = session;
		currentSchedule = null;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testtake frameWindow = new testtake();
					frameWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void mainWithSessionAndSchedule(final UserSession session, final ExamSchedule schedule) {
		currentSession = session;
		currentSchedule = schedule;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testtake frameWindow = new testtake();
					frameWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public testtake(String password) {
		initialize();
	}
	
	public testtake() {
		initialize();
	}
	
	private void initialize() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(0, 0, 1850, 1200);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panel = new JPanel();
		panel.setBounds(0, 0, 1850, 1200);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Name");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel.setBounds(32, 25, 107, 38);
		panel.add(lblNewLabel);
		
		lblNewLabel_1 = new JLabel("Subject");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel_1.setBounds(357, 31, 113, 26);
		panel.add(lblNewLabel_1);
		
		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel_2.setBounds(149, 28, 200, 29);
		panel.add(lblNewLabel_2);
		
		lblNewLabel_3 = new JLabel("");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel_3.setBounds(480, 31, 178, 26);
		panel.add(lblNewLabel_3);
		
		panel_1 = new JPanel();
		panel_1.setBounds(1278, 31, 220, 240);
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel_4 = new JLabel("TIME");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel_4.setBounds(10, 25, 65, 23);
		panel_1.add(lblNewLabel_4);
		
		lblNewLabel_6 = new JLabel("00:00");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_6.setForeground(new Color(192, 57, 43));
		lblNewLabel_6.setBounds(90, 25, 110, 23);
		panel_1.add(lblNewLabel_6);
		
		lblNewLabel_7 = new JLabel("All question");
		lblNewLabel_7.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_7.setBounds(10, 72, 81, 28);
		panel_1.add(lblNewLabel_7);
		
		lblNewLabel_8 = new JLabel("0");
		lblNewLabel_8.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel_8.setBounds(120, 72, 45, 23);
		panel_1.add(lblNewLabel_8);
		
		lblNewLabel_9 = new JLabel("Question attempt :");
		lblNewLabel_9.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_9.setBounds(10, 120, 110, 25);
		panel_1.add(lblNewLabel_9);
		
		lblNewLabel_11 = new JLabel("1");
		lblNewLabel_11.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_11.setBounds(125, 120, 54, 25);
		panel_1.add(lblNewLabel_11);

		lblProgress = new JLabel("Answered: 0 / 0");
		lblProgress.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblProgress.setForeground(new Color(41, 128, 185));
		lblProgress.setBounds(10, 160, 180, 25);
		panel_1.add(lblProgress);
		
		btnNewButton = new JButton("Previous");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prives();
			}
		});
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton.setBounds(1225, 725, 142, 53);
		panel.add(btnNewButton);
		
		btnNewButton_1 = new JButton("Next");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				numberofquestion();
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 17));
		btnNewButton_1.setBounds(1370, 725, 124, 53);
		panel.add(btnNewButton_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(32, 154, 1149, 518);
		panel.add(panel_2);
		panel_2.setLayout(null);
		
		l1 = new JLabel("");
		l1.setFont(new Font("Tahoma", Font.BOLD, 16));
		l1.setBounds(38, 41, 1076, 47);
		panel_2.add(l1);
		
		c1 = new JRadioButton("");
		c1.setFont(new Font("Tahoma", Font.BOLD, 18));
		c1.setBounds(38, 131, 1021, 38);
		panel_2.add(c1);
		
		c2 = new JRadioButton("");
		c2.setFont(new Font("Tahoma", Font.BOLD, 18));
		c2.setBounds(38, 196, 1021, 47);
		panel_2.add(c2);
		
		c3 = new JRadioButton("");
		c3.setFont(new Font("Tahoma", Font.BOLD, 18));
		c3.setBounds(38, 268, 1021, 38);
		panel_2.add(c3);
		
		c4 = new JRadioButton("");
		c4.setFont(new Font("Tahoma", Font.BOLD, 18));
		c4.setBounds(38, 348, 1021, 47);
		panel_2.add(c4);
		
		optionGroup.add(c1);
		optionGroup.add(c2);
		optionGroup.add(c3);
		optionGroup.add(c4);
		
		textField_2 = new JTextField();
		textField_2.setBounds(50, 435, 1010, 47);
		panel_2.add(textField_2);
		textField_2.setColumns(10);
		
		lblNewLabel_10 = new JLabel("");
		lblNewLabel_10.setBounds(10, 725, 6, 13);
		panel.add(lblNewLabel_10);
		
		lblNewLabel_12 = new JLabel("");
		lblNewLabel_12.setBounds(333, 725, 0, 13);
		panel.add(lblNewLabel_12);
		
		p1 = new JLabel("");
		p1.setBounds(454, 795, 0, 13);
		panel.add(p1);
		
		p2 = new JLabel("");
		p2.setBounds(940, 808, 0, 13);
		panel.add(p2);
		
		pr = new JLabel("");
		pr.setBounds(1309, 412, 0, 13);
		panel.add(pr);
		
		btnNewButton_3 = new JButton("Submit");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrentAnswer();
				int total = sessionQuestions != null ? sessionQuestions.size() : 0;
				int answered = countAnswered();
				int unanswered = Math.max(0, total - answered);
				String timeRemaining = (dFormat.format(minute) + ":" + dFormat.format(second));

				String message = String.format(
						"Exam Summary:\n\n" +
						"Total Questions: %d\n" +
						"Answered: %d\n" +
						"Unanswered: %d\n" +
						"Time Remaining: %s\n\n" +
						"Are you sure you want to submit your exam?",
						total, answered, unanswered, timeRemaining
				);

				int a = JOptionPane.showConfirmDialog(testtake.this, message, "Confirm Exam Submission", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (a == JOptionPane.YES_OPTION) {
					submit();
				}
			}
		});
		btnNewButton_3.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton_3.setBounds(983, 25, 113, 47);
		panel.add(btnNewButton_3);

		// Initialize exam data and timer
		initExamSession();
	}

	private void initExamSession() {
		try {
			StudentService studentService = new StudentService();
			SubjectService subjectService = new SubjectService();
			ExamService examService = new ExamService();

			if (currentSession == null || !currentSession.isStudent()) {
				JOptionPane.showMessageDialog(this, "Access Denied: Please log in as a student to take this exam.", "Authentication Required", JOptionPane.ERROR_MESSAGE);
				dispose();
				return;
			}

			// 1. Resolve Student
			Student s = studentService.getStudentById(currentSession, currentSession.getStudentId());
			if (s != null) {
				lblNewLabel_2.setText(s.getName());
				lblNewLabel_10.setText(currentSession.getUsername());
			}

			// 2. Resolve Schedule
			if (currentSchedule != null) {
				activeSchedule = currentSchedule;
			} else {
				List<ExamSchedule> list = examService.getSchedulesByStudentId(currentSession, currentSession.getStudentId());
				for (ExamSchedule es : list) {
					if ("SCHEDULED".equalsIgnoreCase(es.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(es.getStatus())) {
						activeSchedule = es;
						break;
					}
				}
			}

			if (activeSchedule != null) {
				Subject sub = subjectService.getSubjectById(currentSession, activeSchedule.getSubjectId());
				if (sub != null) {
					lblNewLabel_3.setText(sub.getName());
				}
				lblNewLabel_8.setText(Integer.toString(activeSchedule.getTotalQuestions()));
				lblNewLabel_12.setText(Double.toString(activeSchedule.getPassingPercentage()));

				// Start or resume persistent attempt in PostgreSQL
				activeAttempt = examService.startOrResumeExam(currentSession, activeSchedule.getId());
				attemptQuestions = examService.getAttemptQuestions(currentSession, activeAttempt.getId());
				sessionQuestions = new ArrayList<>();
				userAnswers.clear();

				for (int idx = 0; idx < attemptQuestions.size(); idx++) {
					ExamAttemptQuestion eq = attemptQuestions.get(idx);
					sessionQuestions.add(eq.getQuestion());
					if (eq.getSelectedOption() != null && !eq.getSelectedOption().trim().isEmpty()) {
						userAnswers.put(idx + 1, eq.getSelectedOption().trim());
					}
				}

				// Authoritative remaining time
				long remainingSec = activeAttempt.getAuthoritativeRemainingSeconds();
				if (remainingSec <= 0) {
					JOptionPane.showMessageDialog(this, "The exam window has expired.", "Exam Expired", JOptionPane.WARNING_MESSAGE);
					submit();
					return;
				}
				minute = (int) (remainingSec / 60);
				second = (int) (remainingSec % 60);
			} else {
				// Fallback if launched standalone without schedule
				Subject defaultSub = subjectService.getActiveSubjects(currentSession).stream().findFirst().orElse(null);
				if (defaultSub != null) {
					lblNewLabel_3.setText(defaultSub.getName());
					QuestionService qs = new QuestionService();
					sessionQuestions = qs.getActiveQuestionsBySubjectId(currentSession, defaultSub.getId());
					lblNewLabel_8.setText(Integer.toString(sessionQuestions.size()));
					lblNewLabel_12.setText("40.0");
				}
				minute = 10;
				second = 0;
			}

			// Update question label count
			if (sessionQuestions != null && !sessionQuestions.isEmpty()) {
				lblNewLabel_8.setText(Integer.toString(sessionQuestions.size()));
			}

			// Start timer safely
			startTimerSafely();

			// Render first question
			question1 = 1;
			lblNewLabel_11.setText("1");
			renderCurrentQuestion();
			loadSavedAnswerForCurrentQuestion();
			updateProgressIndicator();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to load exam data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void startTimerSafely() {
		if (timer != null && timer.isRunning()) {
			return; // Avoid multiple timers
		}

		ddSecond = dFormat.format(second);
		ddMinute = dFormat.format(minute);
		lblNewLabel_6.setText(ddMinute + ":" + ddSecond);

		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (second > 0) {
					second--;
				} else {
					if (minute > 0) {
						minute--;
						second = 59;
					} else {
						second = 0;
						minute = 0;
						if (timer != null) timer.stop();
						JOptionPane.showMessageDialog(testtake.this, "Time expired! Submitting test automatically.", "Time Up", JOptionPane.WARNING_MESSAGE);
						submit();
						return;
					}
				}

				ddSecond = dFormat.format(second);
				ddMinute = dFormat.format(minute);
				lblNewLabel_6.setText(ddMinute + ":" + ddSecond);

				if (activeAttempt != null && (second % 5 == 0) && currentSession != null) {
					final int remSec = (minute * 60) + second;
					final int attId = activeAttempt.getId();
					final UserSession sess = currentSession;
					java.util.concurrent.CompletableFuture.runAsync(() -> {
						try {
							new ExamService().updateRemainingTime(sess, attId, remSec);
						} catch (Exception ignored) {}
					});
				}
			}
		});
		timer.start();
	}
	
	private void saveCurrentAnswer() {
		String selected = "";
		if (c1.isSelected()) selected = c1.getText();
		else if (c2.isSelected()) selected = c2.getText();
		else if (c3.isSelected()) selected = c3.getText();
		else if (c4.isSelected()) selected = c4.getText();
		else if (textField_2 != null && !textField_2.getText().trim().isEmpty()) selected = textField_2.getText().trim();
		
		if (!selected.isEmpty()) {
			userAnswers.put(question1, selected);
		} else {
			userAnswers.remove(question1);
		}
		if (activeAttempt != null && sessionQuestions != null && question1 > 0 && question1 <= sessionQuestions.size() && currentSession != null) {
			Question q = sessionQuestions.get(question1 - 1);
			final int attId = activeAttempt.getId();
			final int qId = q.getId();
			final String sel = selected;
			final UserSession sess = currentSession;
			java.util.concurrent.CompletableFuture.runAsync(() -> {
				try {
					new ExamService().saveAnswer(sess, attId, qId, sel);
				} catch (Exception ignored) {}
			});
		}
		updateProgressIndicator();
	}

	private int countAnswered() {
		int count = 0;
		if (sessionQuestions == null) return 0;
		for (int i = 1; i <= sessionQuestions.size(); i++) {
			String ans = userAnswers.get(i);
			if (ans != null && !ans.trim().isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private void updateProgressIndicator() {
		int total = sessionQuestions != null ? sessionQuestions.size() : 0;
		int answered = countAnswered();
		lblProgress.setText("Answered: " + answered + " / " + total);
	}
	
	private void loadSavedAnswerForCurrentQuestion() {
		optionGroup.clearSelection();
		if (textField_2 != null) textField_2.setText("");
		
		String saved = userAnswers.get(question1);
		if (saved != null && !saved.isEmpty()) {
			if (c1.getText().equalsIgnoreCase(saved)) c1.setSelected(true);
			else if (c2.getText().equalsIgnoreCase(saved)) c2.setSelected(true);
			else if (c3.getText().equalsIgnoreCase(saved)) c3.setSelected(true);
			else if (c4.getText().equalsIgnoreCase(saved)) c4.setSelected(true);
			else if (textField_2 != null) textField_2.setText(saved);
		}
	}
	
	public void prives() {
		saveCurrentAnswer();
		if (question1 > 1) {
			question1--;
			lblNewLabel_11.setText(String.valueOf(question1));
			renderCurrentQuestion();
			loadSavedAnswerForCurrentQuestion();
		}
	}

	public void question() {
		renderCurrentQuestion();
	}
	
	public void renderCurrentQuestion() {
		try {
			optionGroup.clearSelection();
			if (textField_2 != null) textField_2.setText("");

			if (sessionQuestions != null && question1 > 0 && question1 <= sessionQuestions.size()) {
				Question q = sessionQuestions.get(question1 - 1);
				l1.setText(q.getQuestionText());
				c1.setText(q.getOption1());
				c2.setText(q.getOption2());
				c3.setText(q.getOption3());
				c4.setText(q.getOption4());
				answer = q.getCorrectAnswer();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void numberofquestion() {
		saveCurrentAnswer();
		int maxQues = sessionQuestions != null ? sessionQuestions.size() : 1;
		
		if (question1 < maxQues) {
			question1++;
			lblNewLabel_11.setText(String.valueOf(question1));
			renderCurrentQuestion();
			loadSavedAnswerForCurrentQuestion();
		} else {
			JOptionPane.showMessageDialog(this, "This is the last question.", "Notice", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void countdownTimer() {
		startTimerSafely();
	}	
	
	public void submit() {
		if (isSubmitted) return;
		isSubmitted = true;
		
		if (timer != null) timer.stop();
		saveCurrentAnswer();
		
		btnNewButton.setEnabled(false);
		btnNewButton_1.setEnabled(false);
		btnNewButton_3.setEnabled(false);
		c1.setEnabled(false);
		c2.setEnabled(false);
		c3.setEnabled(false);
		c4.setEnabled(false);
		if (textField_2 != null) textField_2.setEnabled(false);
		
		int totalQuestions = sessionQuestions != null ? sessionQuestions.size() : 0;
		double passThreshold = 40.0;
		try {
			passThreshold = Double.parseDouble(lblNewLabel_12.getText().trim());
		} catch (Exception e) {}
		
		int correctCount = 0;
		int studentId = currentSession != null && currentSession.getStudentId() != null ? currentSession.getStudentId() : 1;
		int subjectId = activeSchedule != null ? activeSchedule.getSubjectId() : 1;
		Integer scheduleId = activeSchedule != null ? activeSchedule.getId() : null;

		try {
			ResultService resultService = new ResultService();
			ExamResult result;
			if (activeAttempt != null && currentSession != null) {
				result = resultService.submitAttempt(currentSession, activeAttempt.getId());
				correctCount = result.getCorrectAnswers();
			} else {
				throw new IllegalStateException("Cannot submit exam without an active authenticated attempt.");
			}

			marks = correctCount;
			double scorePercent = result.getPercentage();
			String passStatus = result.getResult();
			String scorePercentStr = String.format("%.2f", scorePercent);

			if ("Pass".equalsIgnoreCase(passStatus)) {
				JOptionPane.showMessageDialog(this,
						"Congratulations!\nYour score: " + scorePercentStr + "%\nResult: PASS",
						"Exam Result", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this,
						"Examination Completed.\nYour score: " + scorePercentStr + "%\nResult: FAIL",
						"Exam Result", JOptionPane.INFORMATION_MESSAGE);
			}

			dispose();

			if (currentSession != null) {
				final UserSession retSession = currentSession;
				EventQueue.invokeLater(() -> {
					try {
						exam_management_syatem.ui.shell.MainApplicationFrame appFrame =
								new exam_management_syatem.ui.shell.MainApplicationFrame(retSession);
						appFrame.setVisible(true);
						appFrame.navigateTo("STUDENT_MY_RESULTS");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			} else {
				exam_management_syatem.app.Main.main(null);
			}
		} catch (Exception ew) {
			JOptionPane.showMessageDialog(this, "Error submitting exam: " + ew.getMessage(), "Submission Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
