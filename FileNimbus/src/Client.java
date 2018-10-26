import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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


public class Client {
	private static final boolean print = true;//Valor que indica si se muestran o no por consola 
	private static final int portNum = 8080;
	private static final String ip = "localhost";
	
	private static boolean sc;//Conexion segura
	private static String pwd;
	private static String username;
	private static Key conectionKey = null; // Clave AES de conexion
	private static KeyPair userKP;// Par de claves de usuario

	
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static Socket socket = null;
	
	
    public static void main(String arg[]) throws Exception {

    	try {
        socket = new Socket(ip, portNum);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    	}catch(ConnectException e) {
    		println("Servidor desconectado, intentalo mas tarde");
    		return;
    	}
        
        
        conection();
        
        boolean w = true;
        printMenu();
        while(w){
	        switch( menu() ){
	        	case 1:
	        		login();
	        		break;
	        	case 11:
	        		signin();
	        		break;
	        	case 12:
	        		logout();
	        		break;
	        	case 2:
	        		check();
	        		break;
	        	case 3:
	        		upload();
	        		break;
	        	case 4:
	        		download();
	        		break;
	        	case 5:
	        		delete();
	        		break;
	        	case 6:
	        		share();
	        		break;
	        	case 7:
	        		account();
	        		break;
	        	case 8:
	        		break;
	        	case 9:
	        		close();
	        		w=false;
	        		break;
	        }
        }
    }

    public static void conection() throws Exception {
    	println("Generando conexi�n segura...");
    	SS("000");
    	Key K = (Key) SR();
    	//println("Clave p�blica recibida...");
    	
    	//println("Generando clave secreta...");
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); //TODO Tama�o de clave secreta
        conectionKey = kgen.generateKey();
        
        //println("Cifrando clave secreta...");
    	Cipher c = Cipher.getInstance("RSA");
    	c.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted  = new SealedObject(conectionKey, c);
    	
    	//println("Enviando clave secreta cifrada...");
    	SS(conectionKeyEncrypted);
    	
    	//println("A la espera de confirmacion...");
    	sc=true;
    	if(SR().equals("010")) {
    		println("Conexi�n AES Segura!");
    	}else {
    		println("Error de conexion, canal no seguro!");
    		sc=false;
    	}
    }
    public static void login() throws Exception {
    	//TODO ------------------------- login
    	SS("100");
    	Object r = SR();
    	if(r.getClass().equals(String.class) 
    			&& r.equals("E100")) {
    		println("Ya has hecho login");
    		return;
    	}else if(r.getClass().equals(String.class) 
    			&& r.equals("101")) {

        	//Obtenemos datos:
    		println(" ~ Login ~");
        	print(" Username: ");
        	username = System.console().readLine();
        	print(" Password: ");
            pwd = new String(System.console().readPassword());
            
    		//Hashear la pass
    		MessageDigest md = MessageDigest.getInstance("SHA-512");
    		//Aqui se puede poner una sal para anadir seguridad, 
    		byte[] pwdh = md.digest(pwd.getBytes(StandardCharsets.UTF_8));

    		
    		SS(username);
    		SS(pwdh);
    		r=SR();
    		if(r.getClass().equals(String.class)) {
    			if(r.equals("E102")) {
    				println("Usuario incorrecto");
    				username=null;
    				pwd=null;
    			}else {
    				r=SR();
    				if(r.getClass().equals(String.class) 
    						&& r.equals("E103")) {
    					println("Contrase�a incorrecta");
    					username=null;
        				pwd=null;
    				}else {
    					println("Datos correctos");
    					
    					
    					byte[]  pub = (byte[])SR();
    					byte[]  priv = (byte[])SR();
    					
    					
    					 //Crear AES KEY desde pwd
    			        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    			        KeySpec spec = new PBEKeySpec("clave".toCharArray(), "clave".getBytes(), 65536, 256);
    			        SecretKey tmp = factory.generateSecret(spec);
    			        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
    			        
    			        //Desencriptamos
    			        Cipher c = Cipher.getInstance("AES");
    					c.init(Cipher.DECRYPT_MODE, secret);
    					priv = c.doFinal(priv);
    					
    					//Pasar de byte[] a Key
    					KeyFactory kf = KeyFactory.getInstance("RSA"); 
    					PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(priv));
    					PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pub));
    					
    					userKP = new KeyPair(publicKey, privateKey); //Creamos el par de claves
    					
    					println("Bienvenido " + username);
    				}	
    			}
    		}
    	}
    }
    public static void logout() throws Exception {
    	if(username == null) {
    		println("No estas logueado");
    	}else{
    		SS("120");
    		Object r = SR();
    		if(r.getClass().equals(String.class)) {
    			if(((String) r).equals("121")) {
    				println("Logout realizado con �xito");
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
    public static void signin() throws Exception {
    	SS("110");
    	Object r = SR();
    	if(r.getClass().equals(String.class)
    			&& r.equals("E100")) {
    		println("Ya has hecho login");
    		return;
    	}
    	
    	//Obtenemos datos:
		println(" ~ SIGN IN ~");
    	print(" Username: ");
    	username = System.console().readLine();
    	print(" Password: ");
        pwd = new String(System.console().readPassword());
        
        //Hashear la pass
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] pwdh = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
		
		SS(username);
        SS(pwdh);
        
        r=SR();
        if(r.getClass().equals(String.class) && r.equals("E111")) {
        	println("Nombre de usuario ya registrado");
        	return;
        }
    	
        //Crear un par de claves
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); //TODO Tama�o de par de claves
        userKP = keyPairGenerator.genKeyPair();
        
        
        //Crear AES KEY desde pwd
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec("clave".toCharArray(), "clave".getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        
        //Encriptamos
        Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, secret);
		byte[] encrypted = c.doFinal(userKP.getPrivate().getEncoded());		
		
		//Enviamos las claves
        SS(userKP.getPublic().getEncoded());
        SS(encrypted);
        
        if(SR().equals("113")) {
        	println("Usuario creado: " + username);
        }
    }
    public static void check() throws Exception {
    	//TODO ------------------------- check
    	SS("200");
    	println(SR());
    }
    public static void upload() throws Exception {//Esto son solo test, me lo estoy inventando toodo
    	//TODO ------------------------- upload
    	File file = new File("C:/Users/Usuario/Desktop/captura.png");
    	
    	
    	//Generar clave para encriptar el file
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Key k = kgen.generateKey();
        
        //Encriptar File
        Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, k);
		SealedObject fileEncrypted = new SealedObject((Serializable) file, c);
    	
		//Encriptar key
		c = Cipher.getInstance("AES");
		byte[] pwdb = pwd.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		pwdb = sha.digest(pwdb);
		pwdb = Arrays.copyOf(pwdb, 16); // primeros 16 bytes

		SecretKeySpec pwdk = new SecretKeySpec(pwdb, "AES");
		c.init(Cipher.ENCRYPT_MODE, pwdk);
		SealedObject keyEncrypted = new SealedObject((Serializable) k, c);
		
		//Copiado desde aqui pasar a bytes un objeto serializable
		ByteArrayOutputStream bs= new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream (bs);
		os.writeObject(fileEncrypted); 
		os.close();
		//Copiado hasta aqui
        
    	println(bs.toByteArray());
    	println(keyEncrypted);
    	
    	SS("300");
    	println(SR());
    }
    public static void download() throws Exception {
    	//TODO ------------------------- download
    	SS("400");
    	println(SR());
    }
    public static void delete() throws Exception {
    	//TODO ------------------------- delete
    	SS("500");
    	println(SR());
    }
    public static void share() throws Exception {
    	//TODO ------------------------- share
    	SS("600");
    	println(SR());
    	
    }
    public static void account() throws Exception {
    	//TODO ------------------------- account
    	SS("700");
    	println(SR());
    	
    }
    public static void close() throws Exception{
    	SS("900");
		if(SR().equals("910")) {
			socket.close();
			in.close();
			out.close();
			println("Conexion cerrada con �xito");
		}else {
			socket.close();
			in.close();
			out.close();
			println("Error de cierre, cerrando igualemente");
		}
    }

    
    
    
    
    
    
    public static void SS(Object o) throws Exception{
    	if(sc) {
	    	Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, conectionKey);
			SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
	    	out.writeObject(socketEncrypted);
    	}else {
    		out.writeObject(o);
    	}
    }
    public static Object SR() throws Exception {
    	if(sc) {
	    	SealedObject socket = (SealedObject) in.readObject();
	    	Cipher c = Cipher.getInstance("AES");
	    	c.init(Cipher.DECRYPT_MODE, conectionKey);
	    	return socket.getObject(c);
    	}else {
    		return in.readObject();
    	}
    }
    public static void print(Object o) {
    	if(print) {
    		System.out.print(o);
    	}
    }
    public static void println(Object o) {
    	if(print) {
    		System.out.println(o);
    	}
    }
    public static void printMenu() {
    	println(" ");
    	println("--------------------------------");
    	println("---------- FileNimbus ----------");
    	println("--------------------------------");
    	println("  1  - Login");
    	println("  11 - Signin");
    	println("  12 - Logout");
    	println("  2  - Actualizar ficheros");
    	println("  3  - Subir");
    	println("  4  - Bajar");
    	println("  5  - Borrar");
    	println("  6  - Compartir");
    	println("  7  - Ajustes de cuenta");
    	println("  8  - Vacio");
    	println("  9  - Fin de conexi�n");
    }
    public static int menu() {
    	if(username == null) {
    	print(" Unkown: ");
    	}else {
    	print(" " + username + ": ");
    	}
    	
    	String r = System.console().readLine();
    	
    	try {
    		return Integer.parseInt(r);
    	}catch(NumberFormatException e) {
    		println("Introduce un n�mero");
    		return 0;
    	}
    }
    }