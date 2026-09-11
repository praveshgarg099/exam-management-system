package exam_management_syatem;

import java.awt.EventQueue;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class studentlogin {

	private JFrame frame;
	private JTextField textField;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					studentlogin window = new studentlogin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @param s2 
	 */
	public studentlogin() {
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setAutoRequestFocus(false);
		frame.setBounds(0,0,1850,1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		lblNewLabel_1.setBackground(new Color(240, 240, 240));
		lblNewLabel_1.setForeground(new Color(255, 128, 64));
		lblNewLabel_1.setBounds(75, 178, 330, 379);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setBounds(0,0,1850,1200);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(102, 116, 204));
		panel_1.setBounds(623, 211, 332, 338);
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel_2 = new JLabel("Username");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel_2.setBounds(50, 40, 119, 24);
		panel_1.add(lblNewLabel_2);
		
		textField = new JTextField();
		textField.setBounds(22, 71, 279, 31);
		panel_1.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Password");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel.setBounds(50, 114, 103, 24);
		panel_1.add(lblNewLabel);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(22, 144, 279, 31);
		panel_1.add(passwordField);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Show Password");
		chckbxNewCheckBox.setBackground(new Color(102, 116, 204));
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxNewCheckBox.isSelected())
				{
					passwordField.setEchoChar((char)0);
				}
				else
				{
					passwordField.setEchoChar('*');
				}
			}
		});
		chckbxNewCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		chckbxNewCheckBox.setBounds(22, 192, 119, 21);
		panel_1.add(chckbxNewCheckBox);
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = textField.getText().trim();
				String pass = new String(passwordField.getPassword()).trim();
				
				if (user.isEmpty() || pass.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please enter both username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					exam_management_syatem.service.AuthenticationService authService = new exam_management_syatem.service.AuthenticationService();
					exam_management_syatem.security.UserSession session = authService.loginStudent(user, pass);
					frame.dispose();
					StudentDashboard.mainWithSession(session);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Incorrect Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton.setIcon(AppUtils.loadImage("login.png"));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnNewButton.setBounds(111, 265, 111, 31);
		panel_1.add(btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("");
		ImageIcon icon1 = AppUtils.loadImage("/WhatsApp Image 2024-10-26 at 14.02.04_bc7c914a.jpg");
		if (icon1 != null) {
			lblNewLabel_3.setIcon(icon1);
		}
		lblNewLabel_3.setBounds(22, 37, 45, 31);
		panel_1.add(lblNewLabel_3);
		
		JLabel lblNewLabel_3_1 = new JLabel("");
		ImageIcon icon2 = AppUtils.loadImage("/WhatsApp Image 2024-10-26 at 14.02.24_04306b56.jpg");
		if (icon2 != null) {
			lblNewLabel_3_1.setIcon(icon2);
		}
		lblNewLabel_3_1.setBounds(22, 112, 45, 31);
		panel_1.add(lblNewLabel_3_1);
		
		JButton btnNewButton_2 = new JButton("");
		btnNewButton_2.setBackground(new Color(0, 0, 0));
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				index ob = new index();
				ob.main(null);
			}
		});
		btnNewButton_2.setIcon(AppUtils.loadImage("Close.png"));
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton_2.setBounds(1478, 10, 38, 28);
		panel.add(btnNewButton_2);
	}
}
