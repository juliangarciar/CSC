import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
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
            println("Server disconnected, try it later.");
            return false;
        }

        connectTo();

        return isSecure;
    }

    // Connects to the server socket
    public void connectTo() throws Exception {
    	println("Generating secure connection ...");
        secureSend("000");
    	Key K = (Key) secureReceive();
    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        connectionKey = kgen.generateKey();
        
    	Cipher ciph = Cipher.getInstance("RSA");
    	ciph.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted = new SealedObject(connectionKey, ciph);

        // TODO Omg esto era un error del infierno
        secureSend(conectionKeyEncrypted);
        isSecure = true;
        Object codeRv = secureReceive();
    	if(codeRv.equals("010")) {
            println("Secure AES connection!");
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
            println("You have already login");
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
    				println("Incorrect user");
    				username = null;
    				pwd = null;
                }
                else {
    				r = secureReceive();
    				if(r.getClass().equals(String.class) && r.equals("E103")) {
    					println("Incorrect password");
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
    					
                        println("Welcome " + username);
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
    		println("You are not logged in");
        }
        else{
    		secureSend("120");
    		Object r = secureReceive();
    		if(r.getClass().equals(String.class)) {
    			if(((String) r).equals("121")) {
    				println("Logout successfull!");
    				username = null;
    				pwd = null;
    				userKP = null;
                }
                else {
    				println("You are not loggued into the server");
    				username = null;
    				pwd = null;
    				userKP = null;
    			}
    		}
        }
    }

    public void close() throws Exception{
    	secureSend("900");
		if(secureReceive().equals("910")) {
			socket.close();
			in.close();
			out.close();
			println("Connection closed successfully");
        }
        else {
			socket.close();
			in.close();
			out.close();
			println("Closing error, closed also");
		}
    }

    public boolean signUp(String user, String pass) throws Exception {
        boolean signinResponse = false;
        secureSend("110");
        println("Debug-2");
        Object r = secureReceive();
        println("Debug-1");
    	if(r.getClass().equals(String.class) && r.equals("E100")) {
    		println("You have already login");
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
                println("User name already registered");
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
                    println("Created user: " + username);
                    signinResponse = true;
                }
            }
        }
        return signinResponse;
    }

    // Check user files in the server
    // TODO Checking this method
    public void check() throws Exception {
    	if(username==null) {
    		println("You are not loggued in");
    		return;
    	}
    	
    	secureSend("200");
    	Object r = secureReceive();
    	if(r.getClass().equals(String.class) && ((String)r).equals("E201")) {
    		println("Synchronization error");
    		return;
    	}
    	
    	//Leemos tres arrays de misma longitud
    	Object[] id = (Object[]) secureReceive();
    	Object[] shared = (Object[]) secureReceive();
    	Object[] name = (Object[]) secureReceive();
    	
    	
    	//Esto es solo mostrar con formato por consola
    	println("  | Id  | Compartido x | Nombre de Archivo |");
    	println("  |_____|______________|___________________|");
    	if(id.length==shared.length && id.length==name.length) {
    		for(int i=0; i<id.length; i++) {
    			print("  | ");
    			print(id[i]);
    			
    			int a = (int)(Math.log10((int)id[i]) +1);
    			a=3-a; a = Math.max(a, 0); a= Math.min(a, 3);
    			for(int j = 0 ; j<=a; j++) {print(" ");}
    			print("| ");
    			
    			print(shared[i]);
    			
    			a=((String)shared[i]).length();
    			a=12-a; a = Math.max(a, 0); a= Math.min(a, 13);
    			for(int j = 0 ; j<=a; j++) {print(" ");}
    			print("| ");
    			
    			print(name[i]);
    			
    			a=((String)name[i]).length();
    			a=17-a; a = Math.max(a, 0); a= Math.min(a, 18);
    			for(int j = 0 ; j<=a; j++) {print(" ");}
    			print("|");
    			
    			println("");
    		}
    	}
    }

    // TODO Checking this method
    public void upload(File file) throws Exception {
    	if(username == null) {
    		println("You are not logged in");
    		return;
    	}
    	print("Fichero a subir: ");
    	if(!file.exists()) {
    		println("File not found");
    		return;
    	}
        byte[] fileContent = Files.readAllBytes(file.toPath());
        //Genera AES
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); //TODO Tama�o de clave secreta
        SecretKey k = kgen.generateKey();
         
        //Encripta File AES
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, k);
        byte[] fctypyed = c.doFinal(fileContent);
         
        //Encripta K RSA
        c = Cipher.getInstance("RSA");
        c.init(Cipher.ENCRYPT_MODE, userKP.getPublic());
        byte[] kcrypted = c.doFinal(k.getEncoded());
         
        
        secureSend("300");
        Object r = secureReceive();
        if(r.getClass().equals(String.class)) {
        	if(((String)r).equals("E301")) {
        		println("Synchronization error");//Logueado en cliente y no en servidor
			}
			else {
        		secureSend(file.toPath().getFileName().toString()); // Enviar el nombre
        		secureSend(fctypyed); // Enviar el file es demasiado grande y habra que fraccionarlo
        		secureSend(kcrypted); // Enviar la clave    		
      
        		// A la espera de confirmacion
        		r = secureReceive();
        		if(r.getClass().equals(String.class)) {
        			if(((String)r).equals("303")){
        				println("File uploaded successfully");
                    }
                    else if(((String)r).equals("E302")){
        				println("Error uploading the file");
        			}
                }
                else{println("Unknown error");}
        	}
        }
        else{println("Unknown error");}       
    }

    /*public static void download() throws Exception {
    	if(username==null) {
    		println("No estas logueado");
    		return;
    	}    	
    	SS("400");
    	String r =(String) SR();
    	if(r.equals("E401")) {
    		println("Error de sincronizacion");
    		return;
    	}
    	
    	//Obtenemos el fichero
    	int fileid=Integer.MAX_VALUE;
    	print("Id del fichero a descargar:");
    	do {
    		try {
    			fileid = Integer.parseInt(System.console().readLine());
    		}catch(NumberFormatException e) {
    			print("Introduce un n�mero:");
    		}
    	}while(fileid==Integer.MAX_VALUE);
    	
    	//Mandamos el fichero
    	SS(fileid);
    	
    	r = (String) SR();
     	if(r.equals("E402")) {
     		println("El fichero no existe");
     		return;
     	}
     	
    	//Obtenemos el fichero, la clave, y el nombre
    	byte[] file = (byte[]) SR();
    	byte[] key = (byte[]) SR();
    	String filename = (String) SR();
    	
    	//Desencriptar la clave
    	Cipher c = Cipher.getInstance("RSA");
    	c.init(Cipher.DECRYPT_MODE, userKP.getPrivate());
    	key = c.doFinal(key);
    	
    	//Desencriptamos el file
    	c = Cipher.getInstance("AES");
    	SecretKey sk = new SecretKeySpec(key, 0, key.length, "AES");
    	c.init(Cipher.DECRYPT_MODE, sk);
    	file = c.doFinal(file);
    	
    	
    	//Obtenemos la direccion
    	print("Direccion donde guardar: ");
    	File path = new File(System.console().readLine());
    	
    	//Puede que no sea una dir valida
    	if(!path.isDirectory()) {
    		println("No es direccion!");
    		return;
    	}
    	//Puede que la dir no exista
    	if(!path.exists()) {
    		println("La direccion no existe!");
    	}
    	
    	try {
    	File filepath = new File(path.getAbsolutePath() +"/"+ filename);
	    	
	    	//Metodo para usar nombres unicos
	    	int i = 0;
	    	
	    	String name = filepath.getName().substring(0, filepath.getName().lastIndexOf("."));
    		String ext = filepath.getName().substring(filepath.getName().lastIndexOf("."));
    		
	    	while (filepath.exists()) {
	    		i++;
	    		filename = name + " ("+i+")" + ext;
	    		filepath = new File(path.getAbsolutePath() +"/"+ filename);
	    		System.out.println(path.toString());
	    	}
	    	
	    	
	    	//Metodo para sobreescibir
	    	
	    	//if(filepath.exists()) {
	    	//	filepath.delete();
	    	//}
	    	
	    	try {
		    	FileOutputStream stream = new FileOutputStream(filepath);
			    stream.write(file);
			    stream.close();
		    	println("Fichero descargado con �xito!");
	    	}catch(FileNotFoundException ef) {
	    		println("No se encuentra el fichero, o acceso denegado");
	    	}
    	
    	}catch(SecurityException e) {//Puede que nos denieguen el acceso
    		println("No tienes permisos para acceder a esa carpeta");
    	}
    }*/

    /*public static void delete() throws Exception {
    	//TODO ------------------------- delete
    	if(username==null) {
    		println("No estas logeado");
    		return;
    	}
    	
    	SS("500");
    	
    	String r = (String) SR();
    	if(r.equals("E501")) {
    		println("Error de sincronizacion");
    		return;
    	}
    	
    	//Obtenemos el fichero
    	int file=Integer.MAX_VALUE;
    	print("Id del fichero a eliminar:");
    	do {
    		try {
    			file = Integer.parseInt(System.console().readLine());
    		}catch(NumberFormatException e) {
    			print("Introduce un n�mero:");
    		}
    	}while(file==Integer.MAX_VALUE);
    	
    	SS(file);
    	
    	r=(String) SR();
    	
    	if(r.equals("E502")) {
    		println("No tienes ese fichero");
    	}else if(r.equals("502")) {
    		println("Fichero eliminado!");
    	}else {
    		println("Error desconocido");
    	}
    }*/

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
    		println("Enter a number");
    		return 0;
    	}
    }
}