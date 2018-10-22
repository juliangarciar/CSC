import java.io.*;
import java.net.*;
import java.util.Base64;
import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class Client {
	private static final int portNum = 8082;
	private static final String ip = "localhost";
	
	private static String pwd;
	private static Key conectionKey = null; // Clave AES de conexion
	private static KeyPair UserKP;// Par de claves de usuario

	
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	
	
    public static void main(String arg[]) throws Exception {


        Socket socket = new Socket(ip, portNum);

        // Integer Object to send to Server.
        Integer num = new Integer(50);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        
        conection();
        
        SSend("999");
        
        
        socket.close();
        
    }

    public static void conection() throws Exception {
    	out.writeObject("000");
    	Key K = (Key) in.readObject();
    	System.out.println("Clave pública recibida...");
    	
    	System.out.println("Generando clave secreta...");
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        conectionKey = kgen.generateKey();
        System.out.println(conectionKey);
        
        System.out.println("Cifrando clave secreta...");
    	Cipher c = Cipher.getInstance("RSA");
    	c.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted  = new SealedObject(conectionKey, c);
    	
    	System.out.println("Enviando clave secreta cifrada...");
    	out.writeObject(conectionKeyEncrypted);
    	
    	System.out.println("A la espera de confirmacion...");
    	if(SRecive().equals("010")) {
    		System.out.println("Conexión AES Segura!");
    	}else {
    		System.out.println("Error de conexion!");
    	}
    }


    public static void SSend(Object o) throws Exception{
    	Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, conectionKey);
		SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
    	out.writeObject(socketEncrypted);
    }
    public static Object SRecive() throws Exception {
    	SealedObject socket = (SealedObject) in.readObject();
    	Cipher c = Cipher.getInstance("AES");
    	c.init(Cipher.DECRYPT_MODE, conectionKey);
    	return socket.getObject(c);
    }
}