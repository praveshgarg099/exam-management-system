package exam_management_syatem;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import java.awt.Color;

public class adminpage1 {

	private JFrame frame;
	public static int open=0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		mainWithSession(null);
	}

	public static void mainWithSession(final exam_management_syatem.security.UserSession session) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					adminpage1 window = new adminpage1();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the application.
	 */
	public adminpage1() {
		
		initialize();
		
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		
		frame.getContentPane().setBackground(new Color(0, 0, 0));
		frame.setBackground(new Color(0, 0, 0));
		
		frame.setBounds(0, 0, 1470, 956);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnNewButton = new JButton("New Data");
		btnNewButton.setBounds(382, 177, 178, 55);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
				newdata ob = new newdata();
				ob.main(null);
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		frame.getContentPane().setLayout(null);
		btnNewButton.setIcon(AppUtils.loadImage("new.png"));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Old Data");
		btnNewButton_1.setBounds(671, 177, 178, 55);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				olddatatab ob = new olddatatab();
				ob.main(null);
			}
		});
		btnNewButton_1.setIcon(AppUtils.loadImage("301-3019631_historical-data-icon (2) (1).png"));
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Students");
		btnNewButton_2.setBounds(943, 175, 193, 55);
		btnNewButton_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
				StudentManagement ob = new StudentManagement();
				ob.setVisible(true);
			}
		});
		btnNewButton_2.setIcon(AppUtils.loadImage("graduated (2).png"));
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 19));
		frame.getContentPane().add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("Test Maker");
		btnNewButton_3.setBounds(671, 279, 178, 55);
		btnNewButton_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
				testmaker ob=new testmaker();
				ob.main(null);
			}
		});
		btnNewButton_3.setIcon(AppUtils.loadImage("test.png"));
		btnNewButton_3.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_3);
		
		JButton btnNewButton_4 = new JButton("  Logout");
		btnNewButton_4.setBounds(671, 647, 178, 69);
		btnNewButton_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int a=JOptionPane.showConfirmDialog(frame,"Do you really want to logout?","Logout",JOptionPane.YES_NO_OPTION);
				if(a==JOptionPane.YES_OPTION)
				{
					frame.dispose();
					loginAdmin ob = new loginAdmin();
					ob.main(null);
				}
			}
		});
		btnNewButton_4.setIcon(AppUtils.loadImage("Logout.png"));
		btnNewButton_4.setFont(new Font("Tahoma", Font.BOLD, 16));
		frame.getContentPane().add(btnNewButton_4);
		
		JButton btnNewButton_5 = new JButton("");
		btnNewButton_5.setBounds(1484, 10, 46, 34);
		btnNewButton_5.setBackground(new Color(0, 0, 0));
		btnNewButton_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int a=JOptionPane.showConfirmDialog(frame,"Do you really want to Exit to Index?","Select",JOptionPane.YES_NO_OPTION);
				if(a==JOptionPane.YES_OPTION)
				{
					frame.dispose();
					index ob = new index();
					ob.main(null);
				}
			}
		});
		btnNewButton_5.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton_5.setIcon(AppUtils.loadImage("Close.png"));
		frame.getContentPane().add(btnNewButton_5);
		
		JButton btnNewButton_6 = new JButton("Result");
		btnNewButton_6.setBounds(943, 278, 193, 55);
		btnNewButton_6.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnNewButton_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				resultpage ob=new resultpage();
				ob.main(null);
			}
		});
		frame.getContentPane().add(btnNewButton_6);
		
		JButton btnNewButton_7 = new JButton("Add Subject");
		btnNewButton_7.setBounds(382, 278, 178, 55);
		btnNewButton_7.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnNewButton_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				subjectadd ob = new subjectadd();
				ob.setVisible(true);
			}
		});
		frame.getContentPane().add(btnNewButton_7);

		// Real-time KPI Metrics Panel
		JPanel metricsPanel = new JPanel();
		metricsPanel.setBounds(382, 380, 754, 200);
		metricsPanel.setBackground(new Color(20, 25, 40, 220));
		metricsPanel.setLayout(new java.awt.GridLayout(2, 2, 15, 15));
		metricsPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
				javax.swing.BorderFactory.createEmptyBorder(12, 15, 12, 15)
		));

		try {
			exam_management_syatem.service.DashboardMetricsService dms = new exam_management_syatem.service.DashboardMetricsService();
			exam_management_syatem.service.DashboardMetricsService.Metrics m = dms.getMetrics();

			metricsPanel.add(createMetricCard("Students", m.totalStudents + " Total (" + m.activeStudents + " Active)", new Color(46, 204, 113)));
			metricsPanel.add(createMetricCard("Question Bank", m.totalQuestions + " Questions (" + m.activeQuestions + " Active)", new Color(52, 152, 219)));
			metricsPanel.add(createMetricCard("Exam Pass Rate", m.passRate + "% (" + m.passedCount + " Pass / " + m.failedCount + " Fail)", new Color(241, 196, 15)));
			metricsPanel.add(createMetricCard("Average Score", m.averageScorePercentage + "% (" + m.totalResults + " Submissions)", new Color(155, 89, 182)));
		} catch (Exception ex) {
			metricsPanel.add(new JLabel("Metrics currently unavailable"));
		}
		frame.getContentPane().add(metricsPanel);

		
		JLabel lblNewLabel = new JLabel("");
		ImageIcon bgIcon = AppUtils.loadImage("Gemini_Generated_Image_c5ohv2c5ohv2c5oh.jpg");
		if (bgIcon != null) {
			lblNewLabel.setIcon(bgIcon);
		}
		lblNewLabel.setBounds(6, 0, 1470, 956);
		frame.getContentPane().add(lblNewLabel);
	}

	private static JPanel createMetricCard(String title, String value, Color accentColor) {
		JPanel card = new JPanel(new java.awt.BorderLayout(5, 5));
		card.setBackground(new Color(30, 39, 46));
		card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
				javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
		));

		JLabel tLabel = new JLabel(title);
		tLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		tLabel.setForeground(accentColor);
		card.add(tLabel, java.awt.BorderLayout.NORTH);

		JLabel vLabel = new JLabel(value);
		vLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		vLabel.setForeground(Color.WHITE);
		card.add(vLabel, java.awt.BorderLayout.CENTER);

		return card;
	}
}

