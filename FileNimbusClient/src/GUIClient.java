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
	private final int portNum = 8080;
	private final String ip = "localhost";
	
	private boolean sc;//Conexion segura
	private String pwd;
	private String username;
	private Key conectionKey = null; // Clave AES de conexion
	private KeyPair userKP;// Par de claves de usuario

	
	private ObjectOutputStream out;
	private static ObjectInputStream in;
	private static Socket socket = null;
	

    public int conection() throws Exception {
    	try {
            socket = new Socket(ip, portNum);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
    	}catch(ConnectException e) {
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
    public int login(String inUsername, String inPassword) throws Exception {
    	SS("100");
    	Object r = SR();
    	if(r.getClass().equals(String.class) 
    			&& r.equals("E100")) {
    		return;//Ya has hecho login
    	}

    	username = inUsername;
        pwd = inPassword;
        
		//Hashear la pass
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		//Aqui se puede poner una sal para anadir seguridad, 
		byte[] pwdh = md.digest(pwd.getBytes(StandardCharsets.UTF_8));

		
		SS(username);
		SS(pwdh);
		
		r=SR();
		if(r.getClass().equals(String.class)) {
			if(r.equals("E102")) {
				username=null;
				pwd=null;
				return;//Usuario incorrecto
			}else {
				r=SR();
				if(r.getClass().equals(String.class) 
						&& r.equals("E103")) {
					username=null;
    				pwd=null;
    				return;//Contrasena incorrecta
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
					
					return; //Login correcto
				}	
			}
    	}
    }
    public int logout() throws Exception {
    	if(username == null) {
    		return; //No estas logueado
    	}
		SS("120");
		Object r = SR();
		if(r.getClass().equals(String.class)) {
			if(((String) r).equals("121")) {
				username = null;
				pwd = null;
				userKP = null;
				return; //Logout realizado
			}else {
				username = null;
				pwd = null;
				userKP = null;
				return; //Error de sincronizacion
			}
		}
    }
    public int signin(String inUsername, String inPassword, String inPassword2) throws Exception {
    	if(username!=null) {
    		return; //Ya estas logueado
    	}


    	SS("110");
    	Object r = SR();
    	if(r.getClass().equals(String.class)
    			&& r.equals("E100")) {
    		return;//Error de sincronizacion
    	}
    	
    	if(!inPassword.equals(inPassword2)) {
    		return; //Las contraseñas no coinciden
    	}
    	
    	if(inPassword.length()<9) {
    		return; //Contraseña muy corta
    	}
    	
    	//Obtenemos datos:
    	username = inUsername;
        pwd = inPassword;
        
        //Hashear la pass
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] pwdh = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
		
		SS(username);
        SS(pwdh);
        
        r=SR();
        if(r.getClass().equals(String.class) && r.equals("E111")) {
        	return; //Nombre de usuario ya registrado
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
        
        r=SR();
        if(r.getClass().equals(String.class) && r.equals("113")) {
        	return; //Usuario creado
        }
    }
    public int check() throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	}
    	
    	SS("200");
    	Object r = SR();
    	if(r.getClass().equals(String.class) && ((String)r).equals("E201")) {
    		return;//Error de sincrionizacion
    	}
    	
    	//Leemos tres arrays de misma longitud
    	Object[] id = (Object[]) SR();
    	Object[] shared = (Object[]) SR();
    	Object[] name = (Object[]) SR();
    	
    	return;//Habra que generar un array con la lista
    }
    public int upload(String inPath) throws Exception {
    	if(username == null) {
    		return;//No estas logueado
    	}

    	File file = new File(inPath);
    	if(!file.exists()) {
    		return;//Fichero no encontrado
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
        		return;//Error de sincronizacion
        	}else {
        		
        		SS(file.toPath().getFileName().toString());//Enviar el nombre
        		SS(fctypyed);//Enviar el file es demasiado grande y habra que fraccionarlo
        		SS(kcrypted);//Enviar la clave    		
      
        		//A la espera de confirmacion
        		r = SR();
        		if(r.getClass().equals(String.class)) {
        			if(((String)r).equals("303")){
        				return;//Fichero subido con exito
        			}else if(((String)r).equals("E302")){
        				return;//Error subiendo el fichero
        			}
        		}
        	}
        }
        return;//Error desconocido
    }
    public int download(int inFile, String inPath, boolean inReplace) throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	}    	
    	SS("400");
    	String r =(String) SR();
    	if(r.equals("E401")) {
    		return;//Error de sincronizacion
    	}
    	
    	//Obtenemos el fichero
    	int fileid=inFile;
    	
    	//Mandamos el fichero
    	SS(fileid);
    	
    	r = (String) SR();
     	if(r.equals("E402")) {
     		return;//El fichero no existe
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
    	File path = new File(inPath);


    	if(!path.isDirectory()) {
    		return;//No es direccion
    	}


    	if(!path.exists()) {
    		return; //No existe la direccion
    	}
    	
    	try {
    	File filepath = new File(path.getAbsolutePath() +"/"+ filename);
	    	
    		if(inReplace) {
    			//Metodo para sobreescibir
    	    	if(filepath.exists()) {
    	    		filepath.delete();
    	    	}
    		}else {
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
    		}
    	
	    	
	    		
	    	try {
		    	FileOutputStream stream = new FileOutputStream(filepath);
			    stream.write(file);
			    stream.close();
		    	return;//Fichero descargado
	    	}catch(FileNotFoundException ef) {
	    		return; //No se encuentra el fichero, o acceso denegado
	    	}
    	
    	}catch(SecurityException e) {//Puede que nos denieguen el acceso
    		return; //Acceso denegado
    	}
    }
    public int delete(int inFile) throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	}
    	
    	SS("500");
    	
    	String r = (String) SR();
    	if(r.equals("E501")) {
    		return;//Error de sincronizacion
    	}
    	
    	//Obtenemos el fichero
    	SS(inFile);
    	
    	r=(String) SR();
    	
    	if(r.equals("E502")) {
    		return; //No tienes el fichero
    	}else if(r.equals("502")) {
    		return; //Fichero eliminado
    	}else {
    		return; //Error desconocido
    	}
    }
    public int share(int inFile, String inUser) throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	} 	
    	SS("600");
    	String r =(String) SR();
    	if(r.equals("E600")) {
    		return;//Error de sincronizacion
    	}
    	
    	//Obtenemos el fichero
    	int file=inFile;
    	
    	//Obtenemos el usuario
    	String usu=inUser;
    	
    	SS(file);
    	SS(usu);
    	
    	//Esperamos respuesta
    	r = (String)SR();
    	if(r.equals("E601")) {
    		println("Fichero inexistente");
    		return;//Fichero inexistente
    	}else if(r.equals("E602")){
    		println("Usuario inexistente");
    		return;//Usuario inexistente
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
    		return; //El usuario ya tenia este fichero
    	}else if(r.equals("603")) {
    		return; //Fichero compartido
    	}
    	
    }
    public int changePass(String inPassword, String inNewPassword, String inNewPassword2) throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	}
    	
    	SS("710");
    	
    	String r =(String) SR();
    	
    	if(r.equals("E711")) {
    		return;//Error de sincronizacion
    	}
    	
    	if(!inNewPassword.equals(inNewPassword2)) {
			return; //Las contraseñas no coinciden
		}
    	
    	if(inNewPassword.length()<9) {
    		return; //La contraseña es muy corta
    	}
    	
        String actual = inPassword;
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] actualByte = md.digest(actual.getBytes(StandardCharsets.UTF_8));
		
		String nueva = inNewPassword;
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
			return;//Contraseña incorrecta
		}else if(r.equals("E713")) {
			return;//Error desconocido
		}else{
			pwd = nueva;
			return;//Contraseña cambiada
		}	
    }
    public int changeUser(String inUser) throws Exception {
    	if(username==null) {
    		return;//No estas logueado
    	}
    	
    	SS("720");
    	String r = (String) SR();
    	
    	if(r.equals("E721")) {
    		return;//Error de sincronizacion
    	}
    	
    	String uname = inUser;
    	
    	SS(uname);
    	
    	r = (String) SR();
    	
    	if(r.equals("E722")) {
    		return;//Nombre ya en uso
    	}else {
    		username = uname;
    		return; //Nombre cambiado
    	}
    }
    public int close() throws Exception{
    	SS("900");
		if(SR().equals("910")) {
			socket.close();
			in.close();
			out.close();
			return;//Conexion cerrada
		}else {
			socket.close();
			in.close();
			out.close();
			return; //Error de sincronizacion, cerrando
		}
    }

 
    
    
    private void SS(Object o) throws Exception{
    	if(sc) {
	    	Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, conectionKey);
			SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
	    	out.writeObject(socketEncrypted);
    	}else {
    		out.writeObject(o);
    	}
    }
    private Object SR() throws Exception {
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