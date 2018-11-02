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
    	
    	
        //println("A la espera de confirmacion...");
        // TODO Omg esto era un error del infierno
        
        secureSend(conectionKeyEncrypted);
        isSecure = true;
        Object codeRv = secureReceive();
    	if(codeRv.equals("010")) {
            println("Conexi�n AES Segura!");
            isSecure = true;
        }
        else {
    		isSecure = false;
    	}
    }

    // Logs the client
    public boolean login(String user, String pass) throws Exception {
        boolean loginResponse = false;
    	secureSend("100");
    	Object r = secureReceive();
    	if(r.getClass().equals(String.class) && r.equals("E100")) {
            println("Ya has hecho login");
        }
        else if(r.getClass().equals(String.class) && r.equals("101")) {
        	// Get the data
        	username = user;
            pwd = pass;
            
    		// Hash
    		MessageDigest messageDig = MessageDigest.getInstance("SHA-512");
    		//Aqui se puede poner una sal para anadir seguridad, 
    		byte[] pwdHash = messageDig.digest(pwd.getBytes(StandardCharsets.UTF_8));

    		secureSend(username);
            secureSend(pwdHash);
            
    		r = secureReceive();
    		if(r.getClass().equals(String.class)) {
    			if(r.equals("E102")) {
    				println("Usuario incorrecto");
    				username = null;
    				pwd = null;
                }
                else {
    				r = secureReceive();
    				if(r.getClass().equals(String.class) && r.equals("E103")) {
    					println("Contraseï¿½a incorrecta");
    					username = null;
                        pwd = null;
                    }
                    else {
    					byte[]  pub = (byte[])secureReceive();
    					byte[]  priv = (byte[])secureReceive();
    					
    					// Create AES key from pwd
    			        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    			        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), pwd.getBytes(), 65536, 256);
    			        SecretKey tmp = factory.generateSecret(spec);
    			        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
    			        
    			        // Decrypt
    			        Cipher ciph = Cipher.getInstance("AES");
    					ciph.init(Cipher.DECRYPT_MODE, secret);
    					priv = ciph.doFinal(priv);
    					
    					// Cast from byte to key
    					KeyFactory kf = KeyFactory.getInstance("RSA"); 
    					PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(priv));
    					PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pub));
    					
    					userKP = new KeyPair(publicKey, privateKey);
    					
                        println("Bienvenido " + username);
                        loginResponse = true;
    				}	
    			}
    		}
        }
        return loginResponse;
    }

    // Logout the client
    public void logout() throws Exception {
    	if(username == null) {
    		println("No estas logueado");
        }
        else{
    		secureSend("120");
    		Object r = secureReceive();
    		if(r.getClass().equals(String.class)) {
    			if(((String) r).equals("121")) {
    				println("Logout realizado con ï¿½xito");
    				username = null;
    				pwd = null;
    				userKP = null;
    			}else {
    				println("No estabas logueado en el servidor");
    				username = null;
    				pwd = null;
    				userKP = null;
    			}
    		}
    	}
    }
    public boolean signUp(String user, String pass) throws Exception {
        boolean signinResponse = false;
        secureSend("110");
        println("Debug-2");
        Object r = secureReceive();
        println("Debug-1");
    	if(r.getClass().equals(String.class) && r.equals("E100")) {
    		println("Ya has hecho login");
    	}
    	else{
            username = user;
            pwd = pass;
            
            //Hashear la pass
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] pwdHash = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
            
            secureSend(username);
            secureSend(pwdHash);
            
            println("Debug0");

            r = secureReceive();
            if(r.getClass().equals(String.class) && r.equals("E111")) {
                println("Nombre de usuario ya registrado");
            }
            else{
                println("Debug1");
                //Crear un par de claves
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                userKP = keyPairGenerator.genKeyPair();
                
                
                //Crear AES KEY desde pwd
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(pwd.toCharArray(), pwd.getBytes(), 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
                
                //Encriptamos
                Cipher c = Cipher.getInstance("AES");
                c.init(Cipher.ENCRYPT_MODE, secret);
                byte[] encrypted = c.doFinal(userKP.getPrivate().getEncoded());		
                
                println("Debug2");
                
                //Enviamos las claves
                secureSend(userKP.getPublic().getEncoded());
                secureSend(encrypted);
                
                if(secureReceive().equals("113")) {
                    println("Usuario creado: " + username);
                    signinResponse = true;
                }
            }
        }
        return signinResponse;
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