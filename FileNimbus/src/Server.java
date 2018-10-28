import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
	//private static final String sqlPwd = "OHEdUjgBpeYZ2OQy";
	//private static final String sqlUser = "FileNimbusUser";
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
        
        System.out.println("Servidor correindo en: " + listener.getInetAddress() + ":" +port);
        
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
				
                String i = (String)SR();
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
                		check();
                	}else if(i.equals("300")){
                		//Subida
                		upload();
                	}else if(i.equals("400")){
                		//Descarga
                		download();
                	}else if(i.equals("500")){
                		//Borrado
                		delete();
                	}else if(i.equals("600")){
                		//Compartir
                		share();
                	}else if(i.equals("710")){
                		//Clave
                		changePassword();
                	}else if(i.equals("720")){
                		//Usuario
                		SS("E000");
                		//changeUser();
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
				 System.out.println(e);
				 System.out.println(e.getClass());
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
	    		 		+ "WHERE user='"+ usu +"'");
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
			 
			 byte[] k = (byte[]) SR();
			 
			 //Actualizar tabla
    		 PreparedStatement ps = con.prepareStatement("INSERT INTO fileuser(user, file, secretKey, shared) "
    		 		+ "VALUES("+usuid+", "+file+", ? , "+ userID +" )",
    				 PreparedStatement.RETURN_GENERATED_KEYS);
    		 
    		 ps.setBytes(1, k);
    		 try {
    		 ps.executeUpdate();
    		 }catch(SQLException e) {
    			 SS("E603");//Ya compartido
    			 System.out.println(e);
    		 }
    		 SS("603");
    	 }
    	 public void download() throws Exception{
    		 if(userID == Integer.MAX_VALUE) {
    			 SS("E401");
    			 return;
    		 }
    		 SS("401");
    		 
    		 int fileID = (int) SR();
    		 
    		 ResultSet rs = sql.executeQuery("SELECT * "
     		 		+ "FROM fileuser, file "
     		 		+ "WHERE fileuser.user = '"+ userID +"' "
     		 		+ "AND file.id = fileuser.file "
     		 		+ "AND file.id = "+fileID+" LIMIT 1");
 			 if(!rs.first()) {
 				 SS("E402");
 				 return;
 			 }else {
 				 SS("402");
 				 SS(rs.getBlob("data").getBytes(1, (int) rs.getBlob("data").length()));
 				 SS(rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length()));
 				 SS(rs.getString("name"));
 				 
 				 String name =rs.getString("name");
 				 System.out.println(client + ": Descargado fichero: "+name+" id: " + fileID);
 			 }
    	 }
    	 public void check() throws Exception{
    		 if(userID==Integer.MAX_VALUE) {
    			 SS("E201");
    		 }else {
    			 SS("201");
    		 }
    		 
    		 //Buscamos en la base de datos
    		 ResultSet rs = sql.executeQuery("SELECT f.name filename, f.id fileid, u.user shared "
      		 		+ "FROM fileuser r, file f, user u "
      		 		+ "WHERE r.shared = u.id "
      		 		+ "AND r.file = f.id "
      		 		+ "AND r.user = "+userID+" ");

    		 
    		 ArrayList<Integer>  id =new ArrayList<Integer>();
    		 ArrayList<String> shared =new ArrayList<String>();
    		 ArrayList<String> name =new ArrayList<String>();
    		 
    		 while(rs.next()) {
    			 id.add(rs.getInt("fileid"));
    			 shared.add(rs.getString("shared"));
        		 name.add(rs.getString("filename"));
    		 }
    		 
    		 
    		 SS(id.toArray());
    		 SS(shared.toArray());
    		 SS(name.toArray());
    	 }
    	 public void delete() throws Exception{
    		 if(userID==Integer.MAX_VALUE) {
    			 SS("E501");
    			 return;
    		 }else {
    			 SS("501");
    		 }
    		 
    		 int r= (Integer) SR();
    		 
    		 String query = "DELETE FROM fileuser WHERE user="+userID+" AND file="+ r;
    		 try {
    			 sql.executeQuery(query);
    		 }catch(SQLException e){
    			 SS("E502");
    			 return;
    		 }
    		 SS("502");
    	 }
    	 public void changePassword() throws Exception{
    		 if(userID==Integer.MAX_VALUE) {
    			 SS("E711");
    		 }else {
    			 SS("711");
    		 }
    		 
    		 byte[] actual = (byte[]) SR();
    		 byte[] nueva = (byte[]) SR();
    		 byte[] priv = (byte[]) SR();
    		 
    		 PreparedStatement ps = con.prepareStatement("UPDATE user SET "
    		 		+ "pwd = ?, "
    		 		+ "private = ? "
    		 		+ "WHERE id = "+userID
    		 		+ " AND pwd = ?");
    		 ps.setBytes(1, nueva);
    		 ps.setBytes(2, priv);
    		 ps.setBytes(3, actual);
    		 int i = ps.executeUpdate();
    		 
    		 if(i < 1) {
    			 SS("E712");
    		 }else if(i>1) {
    			 SS("E713");
    		 }else {
    			 SS("712");
    		 } 
    	 }
    	 public void changeUser() throws Exception{
    		 
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