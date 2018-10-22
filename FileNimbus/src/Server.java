import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SealedObject;

public class Server {
	private static int port = 8080;
	private static String ip = "localhost";
	
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	private static int cliente = 1;
	private final static KeyPair KP = buildKeyPair();
	

    public static void main(String[] args) throws IOException, ClassNotFoundException {
		


        // Socket for server to listen at.
        ServerSocket listener = new ServerSocket(port, 0, InetAddress.getByName(ip));
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
    
    public static KeyPair buildKeyPair(){
    	try {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);      
        return keyPairGenerator.genKeyPair();
    	}catch(Exception e) {
    		System.out.println(e);
    		
    	}
    	return null;
    }
    
    public static class ControladorCliente extends Thread{
    	final Socket s;
    	final ObjectInputStream in;
    	final ObjectOutputStream out;
    	final int client;
    	private static Key conectionKey = null;
    	
    	 public ControladorCliente(Socket s, ObjectInputStream in, ObjectOutputStream out, int client) {
    		 this.s = s;
    		 this.in = in;
    		 this.out = out;
    		 this.client = client;
    	 } 
    	 public void run() {
			 try {
				// Reading in Integer Object from input stream.
                String i = (String)in.readObject();
                while(!i.isEmpty()) {
                	if(i.equals("000")) {
                		conection();
                	}else if(i.equals("999")) {
                		out.writeObject("CLOSED");
                		System.out.println("Cliente " + this.client + ": CLOSED");
                		break;
                	}else{
	                	System.out.println("Cliente " + this.client + ": " + i);
	                	out.writeObject("000");
                	}
                	i = (String)SRecive();
                }
                out.close();
                in.close();
                s.close();
			 }catch(Exception e) {
				 System.out.println(e.getMessage());
			 }
    	 }
   
    	 public void conection() {
    		 System.out.println(client + " Conectando...");
    		 try {
    			 out.writeObject(KP.getPublic());
    			 System.out.println("Emitida clave pública...");
    			 
    			 
    			 SealedObject i = (SealedObject) in.readObject();
    			 System.out.println("Recibida clave secreta...");
    			 
    			 System.out.println("Desencriptando clave secreta...");
    			 Cipher c = Cipher.getInstance("RSA");
    			 c.init(Cipher.DECRYPT_MODE, KP.getPrivate());
    			 conectionKey = (Key) i.getObject(c);
    			 
    			 
    			 System.out.println("Clave secreta obtenida con exito!");
    			 System.out.println(conectionKey);
    			 
    			 System.out.println("Encriptando socket..");
    			 c = Cipher.getInstance("AES");
    			 c.init(Cipher.ENCRYPT_MODE, conectionKey);
    			 SealedObject socketEncrypted = new SealedObject("010", c);
    			 System.out.println(socketEncrypted);
    			 
    			 
    			 System.out.println("Enviando...");
    			 out.writeObject(socketEncrypted);
    			 
    		 }catch(Exception e) {
    			 System.out.println(e);
    		 }
    		
    	 }
    
    	 public void SSend(Object o) throws Exception{
    	    	Cipher c = Cipher.getInstance("AES");
    			c.init(Cipher.ENCRYPT_MODE, conectionKey);
    			SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
    	    	out.writeObject(socketEncrypted);
    	    }
    	 public Object SRecive() throws Exception {
	    	SealedObject socket = (SealedObject) in.readObject();
	    	Cipher c = Cipher.getInstance("AES");
	    	c.init(Cipher.DECRYPT_MODE, conectionKey);
	    	return socket.getObject(c);
	    }
    }
}