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
	
	private static ServerSocket listener = null;
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	private static int cliente = 1;
	private final static KeyPair KP = buildKeyPair();
	

    public static void main(String[] args) throws Exception, ClassNotFoundException {
    	
    	//Cierra el puerto cuando se hace Ctr+C
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		public void run() {
    			System.out.println("Cerrando Servidor...");
    			try{
    			in.close();
    			out.close();
    			listener.close();
    			}catch(Exception e) {}
    		}
    	});


        // Se crea el socket
        listener = new ServerSocket(port, 0, InetAddress.getByName(ip));
        
        System.out.println("Servidor correindo en la ip: " + listener.getInetAddress() + ":" +port);
        
        //Socket de cliente
        Socket clientSocket = null;
        
        while (true) {
            try {
                // Accept a client connection once Server recieves one.
            	clientSocket = listener.accept();
                System.out.println(cliente + ": Nuevo cliente");
                
                // Creating inout and output streams. Must creat out put stream first.
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                
                //Nuevo hilo Controlador de cliente con el socket el numero de cliente y la entrada y salida
                Thread t = new ControladorCliente(clientSocket, in, out, cliente); 
                t.start();

                cliente++;
                
            } finally {
            } 
        }
    }
    
    //Genera un par de claves RSA
    public static KeyPair buildKeyPair(){
    	try {
        final int keySize = 2048; //TODO Tamaño de par de claves
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);      
        return keyPairGenerator.genKeyPair();
    	}catch(Exception e) {
    		System.out.println(e);
    		
    	}
    	return null;
    }

    //Controlador de cliente
    public static class ControladorCliente extends Thread{
    	private boolean sc = false; //Conexion segura
    	final Socket s;
    	final ObjectInputStream in;
    	final ObjectOutputStream out;
    	final int client;//Numero de cliente
    	private Key conectionKey = null;// Clave secreta de consexion
    	
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
                	}else if(i.equals("100")){
                		//Login
                		SS("E000");
                	}else if(i.equals("200")){
                		//Comprobacion de fichero
                		SS("E000");
                	}else if(i.equals("300")){
                		//Subida
                		SS("E000");
                	}else if(i.equals("400")){
                		//Descarga
                		SS("E000");
                	}else if(i.equals("500")){
                		//Borrado
                		SS("E000");
                	}else if(i.equals("600")){
                		//Compartir
                		SS("E000");
                	}else if(i.equals("700")){
                		//Cuenta
                		SS("E000");
                	}else if(i.equals("800")){
                		//Vacio
                		SS("E000");
                	}else if(i.equals("900")) {
                		//Cierre
                		SS("910");
                		System.out.println( this.client + ": Fin de conexion");
                		break;
                	}else{
	                	System.out.println(this.client + ": " + i);
	                	SS("E100");
                	}
                	i = (String)SR();
                }
                out.close();
                in.close();
                s.close();
			 }catch(Exception e) {
				 System.out.println(e.getMessage());
			 }
    	 }
   
    	 public void conection() {
    		 System.out.println(client + ": Conectando...");
    		 try {
    			 out.writeObject(KP.getPublic());
    			 //System.out.println("Emitida clave pública...");
    			 
    			 
    			 SealedObject i = (SealedObject) in.readObject();
    			 //System.out.println("Recibida clave secreta...");
    			 
    			 //System.out.println("Desencriptando clave secreta...");
    			 Cipher c = Cipher.getInstance("RSA");
    			 c.init(Cipher.DECRYPT_MODE, KP.getPrivate());
    			 conectionKey = (Key) i.getObject(c);
    			 
    			 
    			 //System.out.println("Clave secreta obtenida con exito!");
    			 //System.out.println(conectionKey);
    			 
    			 //System.out.println("Encriptando socket..");
    			 c = Cipher.getInstance("AES");
    			 c.init(Cipher.ENCRYPT_MODE, conectionKey);
    			 SealedObject socketEncrypted = new SealedObject("010", c);
    			 //System.out.println(socketEncrypted);
    			 
    			 
    			 System.out.println(client + ": Conexión segura!");
    			 
    			 out.writeObject(socketEncrypted);
    			 
    			 sc=true;
    			 
    		 }catch(Exception e) {
    			 System.out.println(e);
    		 }
    		
    	 }
    
    	 public void SS(Object o) throws Exception{
    		 if(sc) {
    			 Cipher c = Cipher.getInstance("AES");
    			 c.init(Cipher.ENCRYPT_MODE, conectionKey);
    			 SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
    			 out.writeObject(socketEncrypted);
    		 }else {
    			 out.writeObject(o);
    		 }
    	    }
    	 public Object SR() throws Exception {
    		 if(sc) {
	    		 SealedObject socket = (SealedObject) in.readObject();
	    		 Cipher c = Cipher.getInstance("AES");
	    		 c.init(Cipher.DECRYPT_MODE, conectionKey);
	    		 return socket.getObject(c);
    		 }else {
    			 return in.readObject();
    		 }
	    }
    }
}