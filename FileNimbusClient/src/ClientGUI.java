import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JSplitPane;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;


public class ClientGUI extends JFrame {
	private Client mainClient;

	private JPanel mainPanel;
	// Inputs
	private JLabel clientState;
	private JTextField userName;
	private JLabel userNameLabel;
	private JTextField password;
	private JLabel passwordLabel;
	// ClientGUI class constructor
	public ClientGUI(int portNum, String ip){
		this.mainClient = new Client(portNum, ip);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		this.mainPanel = new JPanel();
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		BoxLayout verticalMenu = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(verticalMenu);

		this.clientState = new JLabel();
		mainPanel.add(clientState);

		this.userNameLabel = new JLabel("Username");
		mainPanel.add(userNameLabel);

		this.userName = new JTextField();
		mainPanel.add(userName);
		userName.setVisible(false);

		this.passwordLabel = new JLabel("Password");
		mainPanel.add(passwordLabel);

		this.password = new JTextField();
		mainPanel.add(password);
		password.setVisible(false);
		/*JButton btnConect = new JButton("conetcion");
		mainLayout.add(btnConect);
		btnConect.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					client.conection();
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});*/
	}

	// Init the client GUI
	public void initGUI() throws Exception{
		try {
			if(mainClient.initializeClient()){
				clientState.setText("Conectado");
				userName.setVisible(true);
				password.setVisible(true);
			}
			else{
				clientState.setText("Error de conexion");
				userName.setVisible(true);
				password.setVisible(true);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
