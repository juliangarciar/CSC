import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;

public class LoginWindow {
	private java.io.File chosenFile;
	private JPanel loginPanel;	
	private JPanel signUpPanel;
	private JPanel userPanel;

	private JFrame frmFilenimbus;
	private JTextField userField;
	private JPasswordField passField;
	private JTextField userRegister;
	private JPasswordField passRegister1;
	private JPasswordField passRegister2;
	private JTable table;
	private DefaultTableModel model;

	private final int portNum = 8080;
	private final String ip = "localhost";
	private final Client mainClient = new Client(portNum, ip);
	
	private final String NO_FILE_SELECTED = "No file selected";
	
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
		frmFilenimbus.getContentPane().setLayout(new CardLayout(0, 0));
		
		
		loginPanel = new JPanel();
		FlowLayout fl_loginPanel = (FlowLayout) loginPanel.getLayout();
		fl_loginPanel.setVgap(50);
		fl_loginPanel.setHgap(50);
		loginPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(loginPanel, "name_418755019206011");
		
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
		
		signUpPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) signUpPanel.getLayout();
		flowLayout.setVgap(50);
		flowLayout.setHgap(50);
		signUpPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(signUpPanel, "name_418755085745115");
		
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
		
		userPanel = new JPanel();
		userPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(userPanel, "name_418755151851020");
		
		JPanel panelMyFiles = new JPanel();
		panelMyFiles.setBackground(SystemColor.inactiveCaptionBorder);
		
		JLabel downloadFileName = new JLabel("No file");
		
		JButton btnDownloadFile = new JButton("Download");
		btnDownloadFile.setForeground(Color.WHITE);
		btnDownloadFile.setBackground(new Color(100, 149, 237));
		
		// Download selected file
		btnDownloadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//downloadFileName.setText(fileList.getSelectedValue().toString());
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// Boton update declarado
		
		JButton btnUpdateFiles = new JButton("Update");
		btnUpdateFiles.setForeground(Color.WHITE);
		btnUpdateFiles.setBackground(new Color(100, 149, 237));
		
		
		
		JLabel lblMyFiles = new JLabel("My Files");
		lblMyFiles.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panelMyFiles = new GroupLayout(panelMyFiles);
		gl_panelMyFiles.setHorizontalGroup(
			gl_panelMyFiles.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panelMyFiles.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelMyFiles.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelMyFiles.createSequentialGroup()
							.addGap(10)
							.addComponent(downloadFileName))
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
						.addGroup(gl_panelMyFiles.createSequentialGroup()
							.addComponent(lblMyFiles)
							.addPreferredGap(ComponentPlacement.RELATED, 330, Short.MAX_VALUE)
							.addComponent(btnDownloadFile)
							.addGap(18)
							.addComponent(btnUpdateFiles)))
					.addContainerGap())
		);
		gl_panelMyFiles.setVerticalGroup(
			gl_panelMyFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMyFiles.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelMyFiles.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnUpdateFiles)
						.addComponent(btnDownloadFile)
						.addComponent(lblMyFiles))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(downloadFileName)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
					.addContainerGap())
		);
		panelMyFiles.setLayout(gl_panelMyFiles);
		
		
		// ********************************************* Tabla ***********************************************************
		table=new JTable();
		scrollPane.setViewportView(table);
		
		// Rellenamos la tabla con los datos de los archivos
    	model = new DefaultTableModel()
		{
    		Class[] columnTypes = new Class[] {
				Boolean.class, String.class, String.class, String.class, String.class
			};
    		
    		public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};
		model.addColumn("Select");
	    model.addColumn("Id");
	    model.addColumn("Name");
	    model.addColumn("Type");
	    model.addColumn("Propietary");
		table.setModel(model);
		asignarTamanyoColumnasTabla();
		
	    
	 // Update files button
 		btnUpdateFiles.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				// Rellenamos la tabla con los datos de los archivos
 			    try {
 					table.setModel(mainClient.check(model));
 					asignarTamanyoColumnasTabla();
 					
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 	// ********************************************* Fin Tabla ***********************************************************
	    
	    
	    
		JPanel panelUploadFiles = new JPanel();
		panelUploadFiles.setBackground(SystemColor.inactiveCaptionBorder);
		
		JPanel panelUsuario = new JPanel();
		panelUsuario.setBackground(new Color(30, 144, 255));
		
		JPanel panelMenu = new JPanel();
		panelMenu.setBackground(new Color(30, 144, 255));
		
		GroupLayout gl_userPanel = new GroupLayout(userPanel);
		gl_userPanel.setHorizontalGroup(
			gl_userPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_userPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_userPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_userPanel.createSequentialGroup()
							.addComponent(panelUploadFiles, GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
							.addGap(7))
						.addGroup(gl_userPanel.createSequentialGroup()
							.addComponent(panelMyFiles, GroupLayout.PREFERRED_SIZE, 567, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_userPanel.createSequentialGroup()
							.addComponent(panelUsuario, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panelMenu, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		gl_userPanel.setVerticalGroup(
			gl_userPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_userPanel.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_userPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(panelUsuario, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
						.addComponent(panelMenu, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
					.addGap(20)
					.addComponent(panelUploadFiles, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panelMyFiles, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(13, Short.MAX_VALUE))
		);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setBackground(new Color(30, 144, 255));
		panelMenu.add(menuBar);
		
		JMenu menu = new JMenu("");
		menu.setBackground(new Color(30, 144, 255));
		menuBar.add(menu);
		menu.setIcon(new ImageIcon(LoginWindow.class.getResource("/assets/menuIconMini.png")));
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.setBackground(new Color(135, 206, 235));
		menu.add(mntmSettings);
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mntmLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try{
					mainClient.logout();
				}
				catch(Exception o){
					// TODO Implement logout exception catch
				}
				loginPanel.setVisible(true);
				userPanel.setVisible(false);
			}
		});
		mntmLogout.setBackground(new Color(135, 206, 235));
		menu.add(mntmLogout);
		
		JLabel lblUser = new JLabel("User");
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JPanel ImgPanel = new JPanel();
		ImgPanel.setBackground(new Color(30, 144, 255));
		GroupLayout gl_panelUsuario = new GroupLayout(panelUsuario);
		gl_panelUsuario.setHorizontalGroup(
			gl_panelUsuario.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUsuario.createSequentialGroup()
					.addGap(8)
					.addComponent(ImgPanel, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblUser)
					.addContainerGap(74, Short.MAX_VALUE))
		);
		gl_panelUsuario.setVerticalGroup(
			gl_panelUsuario.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUsuario.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(ImgPanel, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_panelUsuario.createSequentialGroup()
					.addGap(24)
					.addComponent(lblUser)
					.addContainerGap(13, Short.MAX_VALUE))
		);
		ImgPanel.setLayout(null);
		
		Imagen img = new Imagen();
		img.setLocation(0, 0);
		img.setSize(40, 40);
		ImgPanel.add(img);
		panelUsuario.setLayout(gl_panelUsuario);
		
		JLabel fileName = new JLabel(NO_FILE_SELECTED);
		
		JButton btnSelectFile = new JButton("Select file");
		btnSelectFile.setForeground(Color.WHITE);
		btnSelectFile.setBackground(new Color(100, 149, 237));
		
		JButton btnUploadFile = new JButton("Upload file");
		btnUploadFile.setForeground(Color.WHITE);
		btnUploadFile.setBackground(new Color(100, 149, 237));
		
		JLabel lblNewFile = new JLabel("New File");
		lblNewFile.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panelUploadFiles = new GroupLayout(panelUploadFiles);
		gl_panelUploadFiles.setHorizontalGroup(
			gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUploadFiles.createSequentialGroup()
					.addGroup(gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelUploadFiles.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewFile)
							.addGap(77)
							.addComponent(btnSelectFile)
							.addGap(18)
							.addComponent(btnUploadFile))
						.addGroup(gl_panelUploadFiles.createSequentialGroup()
							.addGap(22)
							.addComponent(fileName)))
					.addContainerGap(264, Short.MAX_VALUE))
		);
		gl_panelUploadFiles.setVerticalGroup(
			gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUploadFiles.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_panelUploadFiles.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewFile)
						.addComponent(btnSelectFile)
						.addComponent(btnUploadFile))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(fileName)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panelUploadFiles.setLayout(gl_panelUploadFiles);
		
		// File upload button
		btnUploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chosenFile.exists()){
					try{
						mainClient.upload(chosenFile);
						fileName.setText(NO_FILE_SELECTED);
						
						// Actualizamos la tabla de archivos
						mainClient.check(model);
					}
					catch(Exception u){
						System.out.println("File could not be uploaded.");
					}
				}
				else{
					System.out.println("File not selected.");
				}
			}
		});
		
		// File select button
		btnSelectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					// Get the file
					chosenFile = fileChooser.getSelectedFile();
					fileName.setText(chosenFile.getAbsolutePath());
				}
			}
		});
		userPanel.setLayout(gl_userPanel);

		// Register button
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				signUpPanel.setVisible(true);
				loginPanel.setVisible(false);
				//frmFilenimbus.setContentPane(signUpPanel);
			}
		});

		// Login button
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
						loginPanel.setVisible(false);
						userPanel.setVisible(true);
						//frmFilenimbus.setContentPane(userPanel);
						
						// Cargar los archivos del usuario en la tabla
						table.setModel(mainClient.check(model));
						lblUser.setText(userName);
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

		// Back to login button
		backToLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signUpPanel.setVisible(false);
				loginPanel.setVisible(true);
				//frmFilenimbus.setContentPane(loginPanel);
			}
		});
		
		// Confirm new signup button
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
							signUpPanel.setVisible(false);
							loginPanel.setVisible(true);
							//frmFilenimbus.setContentPane(loginPanel);
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

	private void asignarTamanyoColumnasTabla() {
		
		table.getColumnModel().getColumn(0).setPreferredWidth(45);
		table.getColumnModel().getColumn(0).setMinWidth(45);
		table.getColumnModel().getColumn(0).setMaxWidth(45);
		table.getColumnModel().getColumn(1).setPreferredWidth(35);
		table.getColumnModel().getColumn(1).setMinWidth(35);
		table.getColumnModel().getColumn(1).setMaxWidth(35);
		table.getColumnModel().getColumn(3).setPreferredWidth(70);
		table.getColumnModel().getColumn(3).setMinWidth(70);
		table.getColumnModel().getColumn(3).setMaxWidth(70);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMaxWidth(100);
	}
}
