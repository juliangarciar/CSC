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

public class GUIClient {
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
	

    public static int conection() throws Exception {
    	try {
            socket = new Socket(ip, portNum);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
    	}catch(ConnectException e) {
    		println("Servidor desconectado, intentalo mas tarde");
    		return 001;//Servidor Desconectado
    	}
    	
    	SS("000");
    	Key K = (Key) SR();

    	KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); //TODO Tamaño de clave secreta
        conectionKey = kgen.generateKey();
      
    	Cipher c = Cipher.getInstance("RSA");
    	c.init(Cipher.ENCRYPT_MODE, K); 
    	SealedObject conectionKeyEncrypted  = new SealedObject(conectionKey, c);

    	SS(conectionKeyEncrypted);
    	
    	sc=true;
    	if(SR().equals("010")) {
    		return 010; //Conexion segura
    	}else {
    		sc=false;
    		return 011;//Error de conexion segura.
    	}
    }
    public static int login() throws Exception {
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
    					println("Contraseña incorrecta");
    					username=null;
        				pwd=null;
    				}else {
    					byte[]  pub = (byte[])SR();
    					byte[]  priv = (byte[])SR();
    					
    					
    					//Crear AES KEY desde pwd
    			        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    			        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), pwd.getBytes(), 65536, 256);
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
    public static int logout() throws Exception {
    	if(username == null) {
    		println("No estas logueado");
    	}else{
    		SS("120");
    		Object r = SR();
    		if(r.getClass().equals(String.class)) {
    			if(((String) r).equals("121")) {
    				println("Logout realizado con éxito");
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
    public static int signin() throws Exception {
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
        keyPairGenerator.initialize(2048); //TODO Tamaño de par de claves
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
		
		//Enviamos las claves
        SS(userKP.getPublic().getEncoded());
        SS(encrypted);
        
        if(SR().equals("113")) {
        	println("Usuario creado: " + username);
        }
    }
    public static int check() throws Exception {
    	if(username==null) {
    		println("No estas logueado");
    		return;
    	}
    	
    	SS("200");
    	Object r = SR();
    	if(r.getClass().equals(String.class) && ((String)r).equals("E201")) {
    		println("Error de sincronización");
    		return;
    	}
    	
    	//Leemos tres arrays de misma longitud
    	Object[] id = (Object[]) SR();
    	Object[] shared = (Object[]) SR();
    	Object[] name = (Object[]) SR();
    	
    	
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
    public static int upload() throws Exception {
    	if(username == null) {
    		println("No estas logueado");
    		return;
    	}
    	print("Fichero a subir: ");
    	File file = new File(System.console().readLine());
    	if(!file.exists()) {
    		println("Fichero no encontrado");
    		return;
    	}
        byte[] fileContent = Files.readAllBytes(file.toPath());
         
        //Genera AES
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); //TODO Tamaño de clave secreta
        SecretKey k = kgen.generateKey();
         
        //Encripta File AES
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, k);
        byte[] fctypyed = c.doFinal(fileContent);
         
        //Encripta K RSA
        c = Cipher.getInstance("RSA");
        c.init(Cipher.ENCRYPT_MODE, userKP.getPublic());
        byte[] kcrypted = c.doFinal(k.getEncoded());
         
        
        SS("300");
        Object r = SR();
        if(r.getClass().equals(String.class)) {
        	if(((String)r).equals("E301")) {
        		println("Error de sincronizacion");//Logueado en cliente y no en servidor
        	}else {
        		
        		SS(file.toPath().getFileName().toString());//Enviar el nombre
        		SS(fctypyed);//Enviar el file es demasiado grande y habra que fraccionarlo
        		SS(kcrypted);//Enviar la clave    		
      
        		//A la espera de confirmacion
        		r = SR();
        		if(r.getClass().equals(String.class)) {
        			if(((String)r).equals("303")){
        				println("Fichero subido con éxito");
        			}else if(((String)r).equals("E302")){
        				println("Error subiendo el fichero");
        			}
        		}else{println("Error desconocido");}
        	}
        }else{println("Error desconocido");}       
    }
    public static int download() throws Exception {
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
    			print("Introduce un número:");
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
	    	/*
	    	if(filepath.exists()) {
	    		filepath.delete();
	    	}
	    	*/
	    	try {
		    	FileOutputStream stream = new FileOutputStream(filepath);
			    stream.write(file);
			    stream.close();
		    	println("Fichero descargado con éxito!");
	    	}catch(FileNotFoundException ef) {
	    		println("No se encuentra el fichero, o acceso denegado");
	    	}
    	
    	}catch(SecurityException e) {//Puede que nos denieguen el acceso
    		println("No tienes permisos para acceder a esa carpeta");
    	}
    }
    public static int delete() throws Exception {
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
    			print("Introduce un número:");
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
    }
    public static int share() throws Exception {
    	if(username==null) {
    		println("No estas logueado");
    		return;
    	}    	
    	SS("600");
    	String r =(String) SR();
    	if(r.equals("E600")) {
    		println("Error de sincronizacion");
    		return;
    	}
    	
    	//Obtenemos el fichero
    	int file=Integer.MAX_VALUE;
    	print("Id del fichero a compartir:");
    	do {
    		try {
    			file = Integer.parseInt(System.console().readLine());
    		}catch(NumberFormatException e) {
    			print("Introduce un número:");
    		}
    	}while(file==Integer.MAX_VALUE);
    	
    	//Obtenemos el usuario
    	String usu;
    	print("Id del usuario a compartir:");
    	usu = System.console().readLine();
    	
    	SS(file);
    	SS(usu);
    	
    	//Esperamos respuesta
    	r = (String)SR();
    	if(r.equals("E601")) {
    		println("Fichero inexistente");
    		return;
    	}else if(r.equals("E602")){
    		println("Usuario inexistente");
    		return;
    	}
    	
    	//Leemos las claves
    	byte[] kf = (byte[]) SR();//Clave secreta de fichero
    	byte[] ku = (byte[]) SR();//Clave publica de usuario
    	
    	//Desencriptar la clave
    	Cipher c = Cipher.getInstance("RSA");
    	c.init(Cipher.DECRYPT_MODE, userKP.getPrivate());
    	byte[] k = c.doFinal(kf);
    	
    	//Encriptar la clave
    	PublicKey pku = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(ku));
    	c.init(Cipher.ENCRYPT_MODE, pku);
    	k = c.doFinal(k);
    	
    	//Enviamos la clave
    	SS(k);
    	r = (String) SR();
    	if(r.equals("E603")) {
    		println("Este usuario ya tiene ese fichero");
    	}else if(r.equals("603")) {
    		println("Fichero compartido!");
    	}
    	
    }
    public static int changePass() throws Exception {
    	if(username==null) {
    		println("No estas logueado");
    		return;
    	}
    	
    	SS("710");
    	
    	String r =(String) SR();
    	
    	if(r.equals("E711")) {
    		println("Error de sincronización");
    		return;
    	}
    	
    	print("Password actual: ");
        String actual = new String(System.console().readPassword());
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] actualByte = md.digest(actual.getBytes(StandardCharsets.UTF_8));
		
		print("Password nueva: ");
		String nueva = new String(System.console().readPassword());
		byte[] nuevaByte = md.digest(nueva.getBytes(StandardCharsets.UTF_8));
			
		//Crear AES KEY desde nueva
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(nueva.toCharArray(), nueva.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		
        //Encriptamos
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, secret);
		byte[] priv = c.doFinal(userKP.getPrivate().getEncoded());	
		
		SS(actualByte);
		SS(nuevaByte);
		SS(priv);
		
		r =(String) SR();
		if(r.equals("E712")) {
			println("Contraseña incorrecta");
		}else if(r.equals("E713")) {
			println("Error desconocido");
		}else{
			pwd = nueva;
			println("Contraseña cambiada con éxito!");
		}	
    }
    public static int changeUser() throws Exception {
    	if(username==null) {
    		println("No estas logueado");
    		return;
    	}
    	
    	SS("720");
    	String r = (String) SR();
    	
    	if(r.equals("E721")) {
    		println("Error de sincronizacion");
    		return;
    	}
    	
    	print("Nuevo username: ");
    	String uname = System.console().readLine();
    	
    	SS(uname);
    	
    	r = (String) SR();
    	
    	if(r.equals("E722")) {
    		println("Nombre de usuario en uso");
    	}else {
    		println("Nombre cambiado con éxito");
    		username = uname;
    	}
    }
    public static int close() throws Exception{
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
}