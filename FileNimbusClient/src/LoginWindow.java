import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Window.Type;
import java.awt.SystemColor;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import javax.swing.JLayeredPane;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JPasswordField;
import java.awt.Dimension;
import javax.swing.border.BevelBorder;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;

public class LoginWindow {

	private JFrame frmFilenimbus;
	private JTextField userField;
	private JPasswordField passField;
	private JTextField userRegister;
	private JPasswordField passRegister1;
	private JPasswordField passRegister2;

	private final int portNum = 8080;
	private final String ip = "localhost";
	private final Client mainClient = new Client(portNum, ip);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow window = new LoginWindow();
					window.frmFilenimbus.setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFilenimbus = new JFrame();
		frmFilenimbus.setMaximumSize(new Dimension(600, 400));
		frmFilenimbus.setMinimumSize(new Dimension(600, 400));
		frmFilenimbus.getContentPane().setFont(new Font("Arial", Font.PLAIN, 17));
		frmFilenimbus.setForeground(SystemColor.desktop);
		frmFilenimbus.setTitle("FileNimbus");
		frmFilenimbus.setBounds(100, 100, 450, 300);
		frmFilenimbus.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFilenimbus.getContentPane().setLayout(new CardLayout(0, 0));
		
		frmFilenimbus.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				JOptionPane.showMessageDialog(null, "We will miss you.");
				try{
					mainClient.logout();
					mainClient.close();
				}
				catch(Exception i){
					System.out.println("Something went wrong while closing the program.");
				}
			}
		});
		
		
		JPanel loginPanel = new JPanel();
		FlowLayout fl_loginPanel = (FlowLayout) loginPanel.getLayout();
		fl_loginPanel.setVgap(50);
		fl_loginPanel.setHgap(50);
		loginPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(loginPanel, "name_409793438578449");
		
		Box loginBox = Box.createVerticalBox();
		loginBox.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.setEnabled(false);
		loginBox.setBorder(null);
		loginPanel.add(loginBox);
		
		JLabel statLabel = new JLabel("Welcome");
		statLabel.setFont(new Font("Arial", Font.BOLD, 16));
		statLabel.setAlignmentX(0.5f);
		loginBox.add(statLabel);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		loginBox.add(verticalStrut);
		
		JLabel userLabel = new JLabel("User");
		userLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(userLabel);
		userLabel.setHorizontalAlignment(SwingConstants.LEFT);
		userLabel.setAlignmentX(0.5f);
		
		userField = new JTextField();
		userField.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(userField);
		userField.setColumns(20);
		
		Component verticalStrut_2 = Box.createVerticalStrut(10);
		loginBox.add(verticalStrut_2);
		
		JLabel passLabel = new JLabel("Password");
		passLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(passLabel);
		passLabel.setHorizontalAlignment(SwingConstants.LEFT);
		passLabel.setAlignmentX(0.5f);
		
		passField = new JPasswordField();
		passField.setFont(new Font("Arial", Font.PLAIN, 15));
		passField.setColumns(20);
		loginBox.add(passField);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		loginBox.add(verticalStrut_1);
		
		Box horizontalBox = Box.createHorizontalBox();
		loginBox.add(horizontalBox);
		
		JButton btnRegister = new JButton("Sign up");
		btnRegister.setFont(new Font("Arial", Font.PLAIN, 15));
		btnRegister.setMaximumSize(new Dimension(100, 25));
		btnRegister.setMinimumSize(new Dimension(100, 25));
		horizontalBox.add(btnRegister);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);
		
		JButton btnLogin = new JButton("Login");
		
		btnLogin.setBorder(UIManager.getBorder("Button.border"));
		btnLogin.setMaximumSize(new Dimension(100, 25));
		btnLogin.setMinimumSize(new Dimension(100, 25));
		horizontalBox.add(btnLogin);
		btnLogin.setFont(new Font("Arial", Font.PLAIN, 15));
		btnLogin.setForeground(new Color(0, 0, 0));
		btnLogin.setBackground(UIManager.getColor("Button.background"));
		btnLogin.setAlignmentX(0.5f);
		
		JPanel signUpPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) signUpPanel.getLayout();
		flowLayout.setVgap(50);
		flowLayout.setHgap(50);
		signUpPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(signUpPanel, "name_409793459187686");
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setFont(new Font("Arial", Font.PLAIN, 15));
		verticalBox.setEnabled(false);
		verticalBox.setBorder(null);
		signUpPanel.add(verticalBox);
		
		JLabel label = new JLabel("Register new account");
		label.setFont(new Font("Arial", Font.BOLD, 16));
		label.setAlignmentX(0.5f);
		verticalBox.add(label);
		
		Component verticalStrut_3 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_3);
		
		JLabel label_1 = new JLabel("User");
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setFont(new Font("Arial", Font.PLAIN, 15));
		label_1.setAlignmentX(0.5f);
		verticalBox.add(label_1);
		
		userRegister = new JTextField();
		userRegister.setFont(new Font("Arial", Font.PLAIN, 15));
		userRegister.setColumns(20);
		verticalBox.add(userRegister);
		
		Component verticalStrut_4 = Box.createVerticalStrut(10);
		verticalBox.add(verticalStrut_4);
		
		JLabel label_2 = new JLabel("Password");
		label_2.setHorizontalAlignment(SwingConstants.LEFT);
		label_2.setFont(new Font("Arial", Font.PLAIN, 15));
		label_2.setAlignmentX(0.5f);
		verticalBox.add(label_2);
		
		passRegister1 = new JPasswordField();
		passRegister1.setFont(new Font("Arial", Font.PLAIN, 15));
		passRegister1.setColumns(20);
		verticalBox.add(passRegister1);
		
		JLabel label_3 = new JLabel("Repeat password");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setFont(new Font("Arial", Font.PLAIN, 15));
		label_3.setAlignmentX(0.5f);
		verticalBox.add(label_3);
		
		passRegister2 = new JPasswordField();
		passRegister2.setFont(new Font("Arial", Font.PLAIN, 15));
		passRegister2.setColumns(20);
		verticalBox.add(passRegister2);
		
		Component verticalStrut_5 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_5);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		JButton backToLogin = new JButton("Back");
		
		backToLogin.setMinimumSize(new Dimension(100, 25));
		backToLogin.setMaximumSize(new Dimension(100, 25));
		backToLogin.setFont(new Font("Arial", Font.PLAIN, 15));
		horizontalBox_1.add(backToLogin);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut_1);
		
		JButton btnRegisterConfirm = new JButton("Confirm");
		btnRegisterConfirm.setMinimumSize(new Dimension(100, 25));
		btnRegisterConfirm.setMaximumSize(new Dimension(100, 25));
		btnRegisterConfirm.setForeground(Color.BLACK);
		btnRegisterConfirm.setFont(new Font("Arial", Font.PLAIN, 15));
		btnRegisterConfirm.setBorder(UIManager.getBorder("Button.border"));
		btnRegisterConfirm.setBackground(SystemColor.menu);
		btnRegisterConfirm.setAlignmentX(0.5f);
		horizontalBox_1.add(btnRegisterConfirm);
		
		JPanel userPanel = new JPanel();
		userPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(userPanel, "name_409807723464641");
		
		// Button actions
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				signUpPanel.setVisible(true);
				frmFilenimbus.setContentPane(signUpPanel);
			}
		});
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String userName = userField.getText();
				char[] pwd = passField.getPassword();
				String tmp = "";
				for(int i = 0; i < pwd.length; i++){
					tmp += pwd[i];
				}
				// Call connect method
				try{
					if(mainClient.login(userName, tmp)){
						statLabel.setText("Conectado.");
						userPanel.setVisible(true);
						frmFilenimbus.setContentPane(userPanel);
					}
				}
				catch(Exception e){
					statLabel.setText(e.getMessage());
				}
				finally{
					//statLabel.setText(tmp);
				}
			}
		});
		backToLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginPanel.setVisible(true);
				frmFilenimbus.setContentPane(loginPanel);
			}
		});
		
		btnRegisterConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userName = userRegister.getText();
				char[] pwd1 = passRegister1.getPassword();
				char[] pwd2 = passRegister2.getPassword();
				String tmp1 = "";
				String tmp2 = "";
				for(int i = 0; i < pwd1.length; i++){
					tmp1 += pwd1[i];
					tmp2 += pwd2[i];
				}
				if(tmp1.equals(tmp2)){
					// Call the register method
					try{
						if(mainClient.signUp(userName, tmp1)){
							statLabel.setText("Account created succesfully.");
							loginPanel.setVisible(true);
							frmFilenimbus.setContentPane(loginPanel);
						}
					}
					catch(Exception a){
						statLabel.setText("Error when trying to sign up.");
					}
				}
				else{
					statLabel.setText("Passwords doesn't match.");
					//statLabel.setText(tmp1);
				}
			}
		});

		// Checks the server connection
		// TODO Bucle?
		try{
			mainClient.initializeClient();
			statLabel.setText("Server connected.");
		}
		catch(Exception e){
			statLabel.setText("Server disconnected.");
			btnLogin.setEnabled(false);
			btnRegister.setEnabled(false);
		}
	}
}
