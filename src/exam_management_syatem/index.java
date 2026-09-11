package exam_management_syatem;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class index {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Initialize SQLite database schema & trigger migration if Access files present
		try {
			boolean isNewDb = !new java.io.File(exam_management_syatem.config.AppConfig.getDbPath()).exists();
			exam_management_syatem.db.DatabaseManager.initializeDatabase();
			if (isNewDb && new java.io.File("studentdata.accdb").exists()) {
				System.out.println("New SQLite DB created. Triggering auto-migration from Access DBs...");
				exam_management_syatem.migration.DataMigrator.migrateAll(".");
			}
		} catch (Exception e) {
			System.err.println("Database initialization warning: " + e.getMessage());
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					index window = new index();
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
	public index() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		//frame.setAutoRequestFocus(false);
		frame.setBounds(0, 0, 1850, 1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		//frame.setForeground(new Color(255, 255, 255));
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setBounds(0, 0, 1850, 1200);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		

		JButton btnNewButton = new JButton("Student");
		btnNewButton.setBackground(new Color(255, 255, 255));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				studentlogin ob = new studentlogin();
				ob.main(null);
			}
		});
		btnNewButton.setBounds(702, 240, 162, 69);
		btnNewButton.setIcon(AppUtils.loadImage("index student.png"));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton.setForeground(new Color(0, 0, 0));
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Admin");
		btnNewButton_1.setBackground(new Color(255, 255, 255));
		btnNewButton_1.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				frame.dispose();
				loginAdmin ob = new loginAdmin();
				ob.main(null);
			}
		});
		btnNewButton_1.setBounds(702, 364, 162, 69);
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 17));
		btnNewButton_1.setIcon(AppUtils.loadImage("index admin.png"));
		panel.add(btnNewButton_1);
		

		JButton btnNewButton_2 = new JButton("Exit");
		btnNewButton_2.setBackground(new Color(255, 255, 255));
		btnNewButton_2.setBounds(702, 484, 162, 69);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int a=JOptionPane.showConfirmDialog(frame,"Do you really want to Exit Application?","Select",JOptionPane.YES_NO_OPTION);
				if(a==JOptionPane.YES_OPTION)
				{
					System.exit(0);
				}
				
			}
		});
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 17));
		btnNewButton_2.setIcon(AppUtils.loadImage("Close.png"));
		panel.add(btnNewButton_2);
	}
}
