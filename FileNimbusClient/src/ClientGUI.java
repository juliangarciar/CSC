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

	// ClientGUI class constructor
	public ClientGUI(int portNum, String ip){
		this.mainClient = new Client(portNum, ip);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		this.mainPanel = new JPanel();
		getContentPane().add(mainPanel);
		
		this.clientState = new JLabel();
		mainPanel.add(clientState);
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
			}
			else{
				clientState.setText("Error de conexion");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
