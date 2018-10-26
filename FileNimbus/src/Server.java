import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.sql.*;
import java.util.Arrays;

public class Server {
	private static final String sqlPwd = "OHEdUjgBpeYZ2OQy";
	private static final String sqlUser = "FileNimbusUser";
	private static final String sqlHost = "localhost";
	private static final String sqlPort = "3306";
	private static final String sqlDb = "filenimbusdb";
	private static Statement sql = null;
	private static Connection con = null;
	
	
	
	private static int port = 8080;
	private static String ip = "localhost";
	
	private static ServerSocket listener = null;
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	private static int cliente = 1;
	private final static KeyPair KP = buildKeyPair();
	

    public static void main(String[] args) throws Exception{
    	
    	try {
    	    con = DriverManager.getConnection(
    	    		"jdbc:mysql://"+sqlHost+":"+sqlPort+"/"+sqlDb, "root", "");
    	 
    	    sql = con.createStatement();
    	         
    	 }catch(SQLException e) {
    		System.out.println("Error conexion SQL.");
    		return;
    	 }
    	
    	
    	
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
    	private int userID=Integer.MAX_VALUE;
    	
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
                		login();
                	}else if(i.equals("110")) {
                		//Sigin
                		signin();
                	}else if(i.equals("120")) {
                		//Logout
                		logout();
                	}else if(i.equals("200")){
                		//Comprobacion de fichero
                		SS("E000");
                	}else if(i.equals("300")){
                		//Subida
                		upload();
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
   
    	 public void conection() throws Exception{
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
    	 public void login() throws Exception {
    		 if(userID!=Integer.MAX_VALUE) {
    			 SS("E101");
    			 return;
    		 }
    		 SS("101");
    		 Object user = SR();
    		 Object pasw = SR();
    		 
    		 if(user.getClass().equals(String.class) && user.getClass().equals(String.class)) {
    			 //Comparamos las claves
    			 ResultSet rs = sql.executeQuery("SELECT * FROM user WHERE user='"+ user +"'");
    			 if(rs.next()) {
    				 SS("102");
    				 byte[] pwd = rs.getBlob("pwd").getBytes(1, (int) rs.getBlob("pwd").length());
    				 if(Arrays.equals(pwd, (byte[])pasw)){
    					 SS("103");
    					//Mandamos las claves
    					 Blob privb = rs.getBlob("private");
    					 Blob pubb = rs.getBlob("public");
    					 
    					 byte[] priv = privb.getBytes(1, (int) privb.length());
    					 byte[] pub = pubb.getBytes(1, (int) pubb.length());
    					 
    					 SS(pub);
    					 SS(priv);
    					 
    					 userID = rs.getInt("id");
    					 
    					 System.out.println(client + ": Login: " + user);
    				 }else {
    					 SS("E103");//Contraseña no valida
    					 System.out.println("Error clave");
    				 }
    				 
    			 }else {
    				 SS("E102");//User no valido
    			 }
    		 }
    	 }
    	 public void logout() throws Exception {
    		 if(userID == Integer.MAX_VALUE) {
    			 SS("E121");
    		 }else {
    			 userID=Integer.MAX_VALUE;
    			 SS("121");
    			 System.out.println(client + ": Logout");
    		 }
    	 }
    	 public void signin() throws Exception{
    		 if(userID!=Integer.MAX_VALUE) {
    			 SS("E100");
    			 return;
    		 }
    		 SS("111");
    		 
    		 String newuser =(String) SR();
    		 byte[] pwdh = (byte[]) SR();
    		 
    		 ResultSet rs = sql.executeQuery ("SELECT user FROM user WHERE user = '"+newuser+"' LIMIT 1");
    		 if(rs.first()) {
    			 SS("E111");//USUARIO YA REGISTRADO
    			 return;
    		 }else {
    			 SS("112");
    		 }
    		 
    		 byte[] pub = (byte[]) SR();
    		 byte[] priv = (byte[]) SR();
    		 
    		 
    		 
    		 PreparedStatement ps = con.prepareStatement("INSERT INTO user(user, pwd, private, public) VALUES('"+ newuser +"', ?, ?, ?)",
    				 PreparedStatement.RETURN_GENERATED_KEYS);
    		 
    		 ps.setBytes(1, pwdh);
    		 ps.setBytes(2, priv);
    		 ps.setBytes(3, pub);
    		 ps.executeUpdate();
    		 rs = ps.getGeneratedKeys();
             if(rs.first())
             {
                 userID = rs.getInt(1);
                 SS("113");
                 System.out.println(client + ": Signin id:" +userID+ " username:" + newuser);
             }else {
            	 SS("E113");
             }
    		 ps.close();
    		 
    	 }
    	 public void upload() throws Exception{
    		 if(userID== Integer.MAX_VALUE) {
    			 //No registrado
    			 SS("E301");
    			 return;
    		 }
    		 SS("301");
    		 
    		 String filename =(String) SR();//Path
    		 byte[] file = (byte[]) SR();//File habra que hacer un bucle;
    		 byte[] key=(byte[]) SR();//Clave
    		 
    		 //Subir el fichero
    		 PreparedStatement ps = con.prepareStatement("INSERT INTO file(data, name) "
    		 		+ "VALUES(?, '"+ filename +"')",
    				 PreparedStatement.RETURN_GENERATED_KEYS);
    		 
    		 ps.setBytes(1, file);
    		 ps.executeUpdate();
    		 
    		 //Subir la clave
    		 ResultSet rs = ps.getGeneratedKeys();    		 
    		 if(rs.first()){
                 int fileID = rs.getInt(1);
        		 ps = con.prepareStatement("INSERT INTO fileuser(user, file, secretKey, shared) "
         		 		+ "VALUES('"+userID+"', "+ fileID +", ?, '"+userID+"')");
         		 ps.setBytes(1, key);
         		 ps.executeUpdate();
         		 System.out.println(cliente + ": Fichero subido: " + filename);
        		 SS("303");//Todo ok
             }else{
            	 SS("E302");//Error subiendo el fichero
             }
    	 }
    	 public void share() throws Exception{
    		 if(userID==Integer.MAX_VALUE) {
    			 SS("E600");
    			 return;
    		 }else {
    			 SS("601");
    		 }
    		 
    		 int file = (int) SR();
    		 String usu = (String) SR();
    		 int usuid;
    		 byte[] ku;
    		 byte[] kf;
    		 //Buscamos el fichero y almacenamos la clave secreta 
    		 ResultSet rs = sql.executeQuery("SELECT secretKey "
    		 		+ "FROM fileuser "
    		 		+ "WHERE user='"+ userID +"' "
    		 		+ "AND file= " + file);
			 if(!rs.first()) {
				 SS("E601");
				 return;
			 }else {
				 kf = rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length());
			 }
			 
			 //Buscamos el usuario a compartir y almacenamos su clave publica
			 rs = sql.executeQuery("SELECT id, public "
	    		 		+ "FROM user "
	    		 		+ "WHERE username='"+ usu +"'");
			 if(!rs.first()) {
				 SS("E602");
				 return;
			 }else {
				 SS("601");
				usuid = rs.getInt("id");
				ku = rs.getBlob("public").getBytes(1, (int) rs.getBlob("public").length());
			 }
			 
			 //Mandamos las claves
			 SS(kf);
			 SS(ku);
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