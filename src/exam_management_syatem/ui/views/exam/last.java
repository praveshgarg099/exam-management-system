package exam_management_syatem.ui.views.exam;

import exam_management_syatem.AppUtils;
import exam_management_syatem.index;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

public class last {

	private static JFrame frame;
	static String pass12="";
	static String pass1234="";
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (args != null && args.length >= 2) {
						pass12 = args[0];
						pass1234 = args[1];
					}
					last window = new last();
					window.frame.setVisible(true);
					JOptionPane.showMessageDialog(window.frame, "Congratulations!\nYour percentage is: " + pass12 + "%\nYour result is: " + pass1234.toUpperCase(), "Exam Result", JOptionPane.INFORMATION_MESSAGE);
					window.frame.dispose();
					index ob = new index();
					ob.main(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public last() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1366, 766);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 10, 1342, 709);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		ImageIcon icon = AppUtils.loadImage("congrats-gif-11.gif");
		if (icon != null) {
			lblNewLabel.setIcon(icon);
		}
		lblNewLabel.setBounds(0, 0, 1342, 731);
		panel.add(lblNewLabel);
	}
}
