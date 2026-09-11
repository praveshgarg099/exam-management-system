package exam_management_syatem.legacy.ui;

import exam_management_syatem.AppUtils;

import java.awt.EventQueue;



import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Point;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Studentadder {

	private JFrame frame;
	private JTextField n;
	private JTextField m;
	private JTextField em;
	private JTextField a;
	private JTextField d;
	public static int open=0;
	private JLabel lblNewLabel_6;
	private JLabel lblNewLabel_6_1;
	private JLabel lblNewLabel_6_2;
	private JLabel lblNewLabel_6_3;
	private JLabel lblNewLabel_6_4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Studentadder window = new Studentadder();
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
	public Studentadder() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setLocation(new Point(150, 183));
		frame.setBounds(0, 0, 1850, 1200);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(102, 116, 204));
		panel.setBounds(-68, 0, 1850, 1200);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Add  Student ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 19));
		lblNewLabel.setIcon(AppUtils.loadImage("graduated (3).png"));
		lblNewLabel.setBounds(749, 128, 439, 61);
		panel.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Name");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblNewLabel_1.setBounds(658, 227, 112, 42);
		panel.add(lblNewLabel_1);
		
		n = new JTextField();
		n.setFont(new Font("Tahoma", Font.BOLD, 14));
		n.setBounds(867, 242, 277, 19);
		panel.add(n);
		n.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Mobile number");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel_2.setBounds(658, 280, 159, 26);
		panel.add(lblNewLabel_2);
		
		m = new JTextField();
		m.setFont(new Font("Tahoma", Font.BOLD, 14));
		m.setBounds(867, 286, 277, 19);
		panel.add(m);
		m.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Email Id");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel_3.setBounds(658, 326, 104, 26);
		panel.add(lblNewLabel_3);
		
		em = new JTextField();
		em.setFont(new Font("Tahoma", Font.BOLD, 14));
		em.setBounds(867, 332, 277, 19);
		panel.add(em);
		em.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Aadhar no");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel_4.setBounds(658, 379, 124, 26);
		panel.add(lblNewLabel_4);
		
		a = new JTextField();
		a.setFont(new Font("Tahoma", Font.BOLD, 14));
		a.setBounds(867, 385, 275, 19);
		panel.add(a);
		a.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Date Birth");
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblNewLabel_5.setBounds(658, 435, 124, 26);
		panel.add(lblNewLabel_5);
		
		d = new JTextField();
		d.setFont(new Font("Tahoma", Font.BOLD, 14));
		d.setBounds(867, 441, 275, 19);
		panel.add(d);
		d.setColumns(10);
		
		JButton btnNewButton = new JButton("Close");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				adminpage1 ob = new adminpage1();
				ob.main(null);
			}
		});
		btnNewButton.setIcon(AppUtils.loadImage("delete-button.png"));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton.setBounds(1465, 21, 132, 46);
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Submit");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String Name = n.getText().trim();
				String Mobile_number = m.getText().trim();
				String Email_Id = em.getText().trim();
				String Aadhar_no = a.getText().trim();
				String Date_Birth = d.getText().trim();
				
				boolean f1 = true, f2 = true, f3 = true, f4 = true, f5 = true;
				
				if (Name.isEmpty()) {
					lblNewLabel_6.setText("Name cannot be empty");
					f1 = false;
				} else if (!Name.matches("^[a-zA-Z\\s]+$")) {
					lblNewLabel_6.setText("Name should contain letters only");
					f1 = false;
				} else {
					lblNewLabel_6.setText(null);
				}
				
				if (Mobile_number.isEmpty()) {
					lblNewLabel_6_1.setText("Mobile cannot be empty");
					f2 = false;
				} else if (!Mobile_number.matches("^\\d{10}$")) {
					lblNewLabel_6_1.setText("Enter 10-digit mobile number");
					f2 = false;
				} else {
					lblNewLabel_6_1.setText(null);
				}
				
				if (Email_Id.isEmpty()) {
					lblNewLabel_6_2.setText("Email cannot be empty");
					f3 = false;
				} else if (!Email_Id.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
					lblNewLabel_6_2.setText("Enter a valid email address");
					f3 = false;
				} else {
					lblNewLabel_6_2.setText(null);
				}
				
				if (Aadhar_no.isEmpty()) {
					lblNewLabel_6_3.setText("Aadhar cannot be empty");
					f4 = false;
				} else if (!Aadhar_no.matches("^\\d{12}$")) {
					lblNewLabel_6_3.setText("Enter 12-digit Aadhar number");
					f4 = false;
				} else {
					lblNewLabel_6_3.setText(null);
				}
				
				if (Date_Birth.isEmpty()) {
					lblNewLabel_6_4.setText("DOB cannot be empty");
					f5 = false;
				} else if (!Date_Birth.matches("^\\d{8}$")) {
					lblNewLabel_6_4.setText("Enter 8-digit DOB (DDMMYYYY)");
					f5 = false;
				} else {
					lblNewLabel_6_4.setText(null);
				}
				
				if (f1 && f2 && f3 && f4 && f5) {
					// Safe Credential Generation
					String cleanName = Name.replaceAll("\\s+", "");
					String namePrefix;
					if (cleanName.length() >= 4) {
						namePrefix = cleanName.substring(0, 4).toLowerCase();
					} else {
						StringBuilder sb = new StringBuilder(cleanName.toLowerCase());
						while (sb.length() < 4) {
							sb.append("x");
						}
						namePrefix = sb.toString();
					}
					String aadharSuffix = Aadhar_no.substring(Aadhar_no.length() - 4);
					String username = namePrefix + aadharSuffix;
					String password = Date_Birth + aadharSuffix;
					
					try {
						exam_management_syatem.service.StudentService studentService = new exam_management_syatem.service.StudentService();
						exam_management_syatem.service.StudentService.RegistrationResult res = studentService.registerStudent(adminpage1.getAdminSession(), Name, Mobile_number, Email_Id, Aadhar_no, Date_Birth);
						
						JOptionPane.showMessageDialog(frame, "Student Details Saved Successfully!\nUsername: " + res.username + "\nInitial Password: " + res.password, "Success", JOptionPane.INFORMATION_MESSAGE);
						
						n.setText("");
						m.setText("");
						em.setText("");
						a.setText("");
						d.setText("");
					} catch (Exception ew) {
						JOptionPane.showMessageDialog(frame, "Error saving student details: " + ew.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnNewButton_1.setBounds(749, 530, 159, 38);
		panel.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("Clear");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				n.setText(null);
				m.setText(null);
				em.setText(null);
				a.setText(null);
				d.setText(null);
				lblNewLabel_6.setText(null);
				lblNewLabel_6_1.setText(null);
				lblNewLabel_6_2.setText(null);
				lblNewLabel_6_3.setText(null);
				lblNewLabel_6_4.setText(null);
			}
		});
		btnNewButton_2.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnNewButton_2.setBounds(957, 531, 124, 38);
		panel.add(btnNewButton_2);
		
		lblNewLabel_6 = new JLabel("");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_6.setForeground(Color.RED);
		lblNewLabel_6.setBounds(231, 173, 350, 25);
		panel.add(lblNewLabel_6);
		
		lblNewLabel_6_1 = new JLabel("");
		lblNewLabel_6_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_6_1.setForeground(Color.RED);
		lblNewLabel_6_1.setBounds(231, 227, 350, 25);
		panel.add(lblNewLabel_6_1);
		
		lblNewLabel_6_2 = new JLabel("");
		lblNewLabel_6_2.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_6_2.setForeground(Color.RED);
		lblNewLabel_6_2.setBounds(231, 290, 350, 25);
		panel.add(lblNewLabel_6_2);
		
		lblNewLabel_6_3 = new JLabel("");
		lblNewLabel_6_3.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_6_3.setForeground(Color.RED);
		lblNewLabel_6_3.setBounds(233, 355, 350, 25);
		panel.add(lblNewLabel_6_3);
		
		lblNewLabel_6_4 = new JLabel("");
		lblNewLabel_6_4.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel_6_4.setForeground(Color.RED);
		lblNewLabel_6_4.setBounds(233, 412, 350, 25);
		panel.add(lblNewLabel_6_4);
	}
}
