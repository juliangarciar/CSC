import java.io.*;
import java.net.*;
import java.sql.*;

public class Server{
	private int port;
	private String ip;
	
	private String sqlIp;
	private String sqlPort;
	private String sqlDb;
	private Statement sqlSentence;
	private Connection sqlConnection;
	
	private ServerSocket listener;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	private int client;
	
	public Server(int server_port, String server_ip, String sql_ip, String sql_port, String sql_db) {
		this.port = server_port;
		this.ip = server_ip;
		
		this.sqlIp = sql_ip;
		this.sqlPort = sql_port;
		this.sqlDb = sql_db;
		this.sqlSentence = null;
		this.sqlConnection = null;
		
		this.listener = null;
		this.out = null;
		this.in = null;
		
		this.client = 0;
	}

	public void initServer() throws Exception {
		try {
			String temp = "jdbc:mysql://" + sqlIp + ":" + sqlPort + "/" + sqlDb;
    	    sqlConnection = DriverManager.getConnection(temp, "root", "");
    	    sqlSentence = sqlConnection.createStatement();
		}
		catch(SQLException e) {
    		System.out.println("Error conexion SQL.");
    		return;
    	}
		
    	// Overload the kill signal
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		public void run() {
    			System.out.println("Cerrando Servidor...");
    			try{
	    			in.close();
	    			out.close();
	    			listener.close();
    			}
    			catch(Exception e) {
    				// TODO
    			}
    		}
    	});
    	
        // Creates the socket
        listener = new ServerSocket(port, 0, InetAddress.getByName(ip));
        
        System.out.println("Servidor iniciado en: " + listener.getInetAddress() + ":" +port);
        
        // Client socket
        Socket clientSocket = null;
        
        while (true) {
            try {
                // Accept a client connection once Server receives one.
            	clientSocket = listener.accept();
                System.out.println(client + ": Nuevo cliente");
                
                // Creating in/out and output streams
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                
                // Starts threading
                Thread t = new ClientController(clientSocket, in, out, client, sqlSentence, sqlConnection); 
                t.start();

                client++;
            }
            finally {
            	// TODO
            } 
        }
	}
}