import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JPasswordField;
import javax.swing.JButton;

public class FileNimbus extends JFrame {

	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField pwdPassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileNimbus frame = new FileNimbus();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FileNimbus() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblFilenimbus = new JLabel("FileNimbus");
		lblFilenimbus.setHorizontalAlignment(SwingConstants.CENTER);
		lblFilenimbus.setFont(new Font("Yu Gothic UI Light", Font.PLAIN, 30));
		lblFilenimbus.setBounds(117, 11, 200, 48);
		contentPane.add(lblFilenimbus);
		
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
		txtUsername.setText("Username");
		txtUsername.setBounds(117, 83, 200, 30);
		contentPane.add(txtUsername);
		txtUsername.setColumns(10);
		
		pwdPassword = new JPasswordField();
		pwdPassword.setHorizontalAlignment(SwingConstants.CENTER);
		pwdPassword.setText("Password");
		pwdPassword.setBounds(117, 124, 200, 30);
		contentPane.add(pwdPassword);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(222, 165, 95, 23);
		contentPane.add(btnLogin);
		
		JButton btnRegistrarse = new JButton("Registrarse");
		btnRegistrarse.setBounds(117, 165, 95, 23);
		contentPane.add(btnRegistrarse);
		
		JLabel msg = new JLabel("");
		msg.setForeground(Color.RED);
		msg.setHorizontalAlignment(SwingConstants.CENTER);
		msg.setBounds(117, 199, 200, 51);
		contentPane.add(msg);
	}
}
