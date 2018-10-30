import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Client{
    private boolean printDebug = true; // Activate print debug
	private int portNum;
	private String ip;
	
	private boolean isSecure = false;
	private String pwd;
	private String username;
	private Key connectionKey = null; 
	private KeyPair userKP = null;

	
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
    private Socket socket = null;
    
    // Client class constructor 
    public Client(int portNum, String ip){
        this.portNum = portNum;
        this.ip = ip;
    }

    // Init the client connection
    public boolean initializeClient() throws Exception{
        try{
            socket = new Socket(ip, portNum);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch(ConnectException e){
            println("Servidor desconectado, intentalo mas tarde.");
            return false;
        }

        connectTo();

        return isSecure;
    }

    // Connects to the server socket
    public void connectTo() throws Exception {
    	println("Generando conexion segura...");
    	secureSend("000");
    	Key K = (Key) secureReceive();
    	//println("Clave p�blica recibida...");
    	
    	//println("Generando clave secreta...");
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        connectionKey = kgen.generateKey();
        
        //println("Cifrando clave secreta...");
    	Cipher ciph = Cipher.getInstance("RSA");
    	ciph.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted = new SealedObject(connectionKey, ciph);
    	
    	//println("Enviando clave secreta cifrada...");
    	secureSend(conectionKeyEncrypted);
    	
    	//println("A la espera de confirmacion...");
    	
    	if(secureReceive().equals("010")) {
            println("Conexi�n AES Segura!");
            isSecure = true;
        }
        else {
    		println("Error de conexion, canal no seguro!");
    		isSecure = false;
    	}
    }

    // Sends data with security checks
    private void secureSend(Object o) throws Exception{
        if(isSecure){
            Cipher ciph = Cipher.getInstance("AES");
			ciph.init(Cipher.ENCRYPT_MODE, connectionKey);
			SealedObject socketEncrypted = new SealedObject((Serializable) o, ciph);
	    	out.writeObject(socketEncrypted);
        }
        // TODO Is this alright?
        else{
            out.writeObject(o);
        }
    }
    
    // Receives data with security checks
    private Object secureReceive() throws Exception{
        if(isSecure){
            SealedObject socket = (SealedObject)in.readObject();
            Cipher ciph = Cipher.getInstance("AES");
            ciph.init(Cipher.DECRYPT_MODE, connectionKey);
            return socket.getObject(ciph);
        }
        // TODO Is this alright?
        else{
            return in.readObject();
        }
    }

    // Overload of print
    private void print(Object o) {
    	if(printDebug) {
    		System.out.print(o);
    	}
    }

    // Overload of println
    private void println(Object o) {
    	if(printDebug) {
    		System.out.println(o);
    	}
    }

    // TODO Is this alright?
    public int menu() {
    	if(username == null) {
    	    print(" Unkown: ");
        }
        else {
    	    print(" " + username + ": ");
    	}
    	
    	String r = System.console().readLine();
    	
    	try {
    		return Integer.parseInt(r);
        }
        catch(NumberFormatException e) {
    		println("Introduce un n�mero");
    		return 0;
    	}
    }
}