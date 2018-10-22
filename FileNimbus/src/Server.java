import java.io.*;
import java.net.*;

public class Server {
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	private static int cliente = 1;
	

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        // Socket for server to listen at.
        ServerSocket listener = new ServerSocket(8080, 0, InetAddress.getByName("localhost"));
        //System.out.println("Server is now running at port: " + portNum);
        System.out.println(listener.getInetAddress());
        // Simply making Server run continously.
        Socket clientSocket = null;
        while (true) {
            try {
                // Accept a client connection once Server recieves one.
               clientSocket = listener.accept();
                System.out.println("Nuevo cliente!  --  Asignando variables");
                
                // Creating inout and output streams. Must creat out put stream first.
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                
                Thread t = new ControladorCliente(clientSocket, in, out, cliente);
                cliente++;
                t.start();

                
                
            } finally {
            } 
        }
    }
    
    public static class ControladorCliente extends Thread{
    	final Socket s;
    	final ObjectInputStream in;
    	final ObjectOutputStream out;
    	final int cliente;
    	
    	 public ControladorCliente(Socket s, ObjectInputStream in, ObjectOutputStream out, int cliente) {
    		 this.s = s;
    		 this.in = in;
    		 this.out = out;
    		 this.cliente = cliente;
    	 } 
    	 public void run() {
			 try {
				// Reading in Integer Object from input stream.
                String i = (String)in.readObject();
                while(!i.isEmpty()) {
                	if(i.equals("close")) {
                		out.writeObject("CLOSED");
                		System.out.println("Cliente " + this.cliente + ": CLOSED");
                		break;
                	}
                	System.out.println("Cliente " + this.cliente + ": " + i);
                	out.writeObject("TOK");
                	i = (String)in.readObject();
                }
                out.close();
                in.close();
                s.close();
			 }catch(Exception e) {
				 System.out.println(e.getMessage());
			 }
    	 }
    }
}