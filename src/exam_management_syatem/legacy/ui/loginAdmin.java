package exam_management_syatem.legacy.ui;

import exam_management_syatem.AppUtils;
import exam_management_syatem.index;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;

public class loginAdmin {

	public JFrame frame;
	public JTextField t1;
	public JLabel l1;
public 	JLabel l2;
private JPasswordField t2;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					exam_management_syatem.ui.auth.LoginFrame.launchAdminMode();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public loginAdmin() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1750, 1100);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		
		panel.setBorder(UIManager.getBorder("Button.border"));
		panel.setBackground(new Color(247, 247, 247));
		panel.setBounds(0,0, 1540, 802);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
	    panel.setOpaque(false);
	       
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(UIManager.getBorder("Button.border"));
		panel_1.setBackground(new Color(255, 255, 255));
		panel_1.setBounds(167, 95, 1199, 573);
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		ImageIcon icon1 = AppUtils.loadImage("/WhatsApp Image 2024-10-26 at 13.51.37_61d7902e.jpg");
		if (icon1 != null) {
			lblNewLabel.setIcon(icon1);
		}
		lblNewLabel.setBounds(53, 42, 541, 510);
		panel_1.add(lblNewLabel);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panel_2.setBackground(new Color(255, 255, 255));
		panel_2.setBounds(611, 61, 523, 451);
		panel_1.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("Log In");
		lblNewLabel_1.setBounds(66, 24, 104, 40);
		panel_2.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 32));
		
		JLabel lblNewLabel_2 = new JLabel("UserName");
		lblNewLabel_2.setForeground(new Color(192, 192, 192));
		lblNewLabel_2.setBounds(90, 121, 141, 23);
		panel_2.add(lblNewLabel_2);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 21));
		
		t1 = new JTextField();
		t1.setBounds(66, 151, 239, 30);
		panel_2.add(t1);
		t1.setColumns(10);
		
		JButton btnNewButton = new JButton("Log in ");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = t1.getText().trim();
				String pass = new String(t2.getPassword()).trim();
				if (user.isEmpty() || pass.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please enter both username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					exam_management_syatem.service.AuthenticationService authService = new exam_management_syatem.service.AuthenticationService();
					exam_management_syatem.security.UserSession session = authService.loginAdmin(user, pass);
					frame.dispose();
					java.awt.EventQueue.invokeLater(() -> {
						exam_management_syatem.ui.shell.MainApplicationFrame mainFrame = new exam_management_syatem.ui.shell.MainApplicationFrame(session);
						mainFrame.setVisible(true);
					});
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, "Incorrect Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewButton.setBorder(new CompoundBorder());
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setBackground(new Color(148, 190, 254));
		btnNewButton.setBounds(66, 373, 105, 41);
		panel_2.add(btnNewButton);
		
		JLabel lblNewLabel_2_2 = new JLabel("Password");
		lblNewLabel_2_2.setForeground(Color.LIGHT_GRAY);
		lblNewLabel_2_2.setFont(new Font("Tahoma", Font.BOLD, 21));
		lblNewLabel_2_2.setBounds(90, 238, 141, 23);
		panel_2.add(lblNewLabel_2_2);
		
		JLabel lblNewLabel_3 = new JLabel("");
		ImageIcon icon2 = AppUtils.loadImage("/WhatsApp Image 2024-10-26 at 14.02.04_bc7c914a.jpg");
		if (icon2 != null) {
			lblNewLabel_3.setIcon(icon2);
		}
		lblNewLabel_3.setBounds(66, 110, 22, 40);
		panel_2.add(lblNewLabel_3);
		
		JLabel lblNewLabel_3_1 = new JLabel("");
		ImageIcon icon3 = AppUtils.loadImage("/WhatsApp Image 2024-10-26 at 14.02.24_04306b56.jpg");
		if (icon3 != null) {
			lblNewLabel_3_1.setIcon(icon3);
		}
		lblNewLabel_3_1.setBounds(66, 227, 32, 40);
		panel_2.add(lblNewLabel_3_1);
		
		l1 = new JLabel("Username Is Incorrect");
		l1.setVisible(false);
		l1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		l1.setForeground(new Color(255, 0, 0));
		l1.setBounds(66, 191, 165, 17);
		panel_2.add(l1);
		
		l2 = new JLabel("Password Is Incorrect");
		l2.setVisible(false);
		l2.setForeground(Color.RED);
		l2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		l2.setBounds(66, 304, 165, 17);
		panel_2.add(l2);
		
		t2 = new JPasswordField();
		t2.setBounds(66, 271, 239, 30);
		panel_2.add(t2);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Show Password");
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxNewCheckBox.isSelected())
				{
					t2.setEchoChar((char)0);
				}
				else
				{
					t2.setEchoChar('*');
				}
			}
		});
		chckbxNewCheckBox.setBackground(new Color(255, 255, 255));
		chckbxNewCheckBox.setBounds(66, 327, 152, 23);
		panel_2.add(chckbxNewCheckBox);
	}
}