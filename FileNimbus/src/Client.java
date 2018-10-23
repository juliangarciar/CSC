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
	private static final boolean print = true;//Valor que indica si se muestran o no por consola 
	private static final int portNum = 8081;
	private static final String ip = "localhost";
	
	private static boolean sc;//Conexion segura
	private static String pwd;
	private static Key conectionKey = null; // Clave AES de conexion
	private static KeyPair UserKP;// Par de claves de usuario

	
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static Socket socket = null;
	
	
    public static void main(String arg[]) throws Exception {


        socket = new Socket(ip, portNum);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        
        
        conection();
        
        boolean w = true;
        printMenu();
        while(w){
	        switch( menu() ){
	        	case 1:
	        		login();
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
    	println("Generando conexión segura...");
    	SS("000");
    	Key K = (Key) SR();
    	//println("Clave pública recibida...");
    	
    	//println("Generando clave secreta...");
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
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
    		println("Conexión AES Segura!");
    	}else {
    		println("Error de conexion, canal no seguro!");
    		sc=false;
    	}
    }
    public static void login() throws Exception {
    	//TODO ------------------------- login
    	
    	//Obtenemos datos:
    	println(" ~ Login ~");
    	print(" Username: ");
    	String username = System.console().readLine();
    	print(" Password: ");
        String password = new String(System.console().readPassword());
        
        println(username + " -- " + password);
        
    	SS("100");
    	println(SR());
    }
    public static void check() throws Exception {
    	//TODO ------------------------- check
    	SS("200");
    	println(SR());
    }
    public static void upload() throws Exception {
    	//TODO ------------------------- upload
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
			println("Conexion cerrada con éxito");
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
    	println("  1 - Login");
    	println("  2 - Actualizar ficheros");
    	println("  3 - Subir");
    	println("  4 - Bajar");
    	println("  5 - Borrar");
    	println("  6 - Compartir");
    	println("  7 - Ajustes de cuenta");
    	println("  8 - Vacio");
    	println("  9 - Fin de conexión");
    }
    public static int menu() {
    	println("");
    	print(" Respuesta: ");
    	
    	String r = System.console().readLine();
    	
    	while(Integer.parseInt(r)>9 || Integer.parseInt(r)<1){
    		print(" Respuesta no válida: ");
    		r = System.console().readLine();
    	}
    	
    	return Integer.parseInt(r);
    }
    
    }