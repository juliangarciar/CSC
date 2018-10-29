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
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JSplitPane;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;
import java.awt.GridLayout;

public class FileNimbus extends JFrame {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		GUIClient client = new GUIClient();


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
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
	}
}
