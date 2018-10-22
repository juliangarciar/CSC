import java.io.*;
import java.net.*;

public class Server {
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        // Socket for server to listen at.
        ServerSocket listener = new ServerSocket(8080, 0, InetAddress.getByName("localhost"));
        //System.out.println("Server is now running at port: " + portNum);
        System.out.println(listener.getInetAddress());
        // Simply making Server run continously.
        while (true) {
            try {
                // Accept a client connection once Server recieves one.
            	Socket celientSocket = null;
                Socket clientSocket = listener.accept();
                System.out.println("Nuevo cliente!  --  Asignando variables");
                
                // Creating inout and output streams. Must creat out put stream first.
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                
                Thread t = new ControladorCliente(clientSocket, in, out);
                t.start();

                
                //Sending response back to client
                //String response = "Integer Object Received.";
                //out.writeObject(response);

                // Outputting recieved Integer Object.
                System.out.println("Received integer: " + i);
                out.close();
                in.close();
                clientSocket.close();
                break;
            } finally {
                      // Closing Server Socket now.
                      listener.close();
            } 
        }
    }
    
    private class ControladorCliente extends Thread {
    	final Socket s;
    	final ObjectInputStream in;
    	final ObjectOutputStream out;
    	
    	 public ControladorCliente(Socket s, ObjectInputStream in, ObjectOutputStream out) {
    		 this.s = s;
    		 this.in = in;
    		 this.out = out;
    	 }
    	 
    	 
    	 public void run() {
    		 while(true) {
    			 try {
    				// Reading in Integer Object from input stream.
    	                String i = (String)in.readObject();
    	                out.writeObject("QUE TE FOLLEN");
    	                while(!i.isEmpty()) {
    	                	i = (String)in.readObject();
    		                if(i.equals("SEND")) {
    		                	System.out.println("GOT IT");
    		                	out.writeObject("QUE TE FOLLEN");
    		                }
    		                else if(i.equals("QUIT")) {
    		                	break;
    		                }
    	                }
    			 }catch(Exception e) {
    				 System.out.println(e.getMessage());
    			 }
    		 }
    	 }
   
    }
}