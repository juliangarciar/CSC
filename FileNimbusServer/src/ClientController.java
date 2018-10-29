import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.*;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class ClientController extends Thread{
	private boolean secureConnection = false; // Secure connection sentinel
	private final Socket clientSocket;
	private final ObjectInputStream in;
	private final ObjectOutputStream out;
	private final int kClient;
	private Key connectionKey = null; // Secret connection key
	private int userID = Integer.MAX_VALUE;
	private KeyPair keyPair = null;
	private Statement sqlSentence;
	private Connection sqlConnection;
	
	public ClientController(Socket clientSocket, ObjectInputStream in, ObjectOutputStream out, int client, Statement sql, Connection con) {
		 this.clientSocket = clientSocket;
		 this.in = in;
		 this.out = out;
		 this.kClient = client;
		 this.keyPair = buildKeyPair();
		 this.sqlSentence = sql;
		 this.sqlConnection = con;
	} 
	 
	public void run(){
		 try {
	        String i = (String)secureReceive();
	        while(!i.isEmpty()) {
	        	if(i.equals("000")) {
	        		connect();
	        	}else if(i.equals("100")){
	        		login();
	        	}else if(i.equals("110")) {
	        		signIn();
	        	}else if(i.equals("120")) {
	        		logout();
	        	}else if(i.equals("200")){
	        		check();
	        	}else if(i.equals("300")){
	        		upload();
	        	}else if(i.equals("400")){
	        		download();
	        	}else if(i.equals("500")){
	        		delete();
	        	}else if(i.equals("600")){
	        		share();
	        	}else if(i.equals("710")){
	        		changePasecureSendword();
	        	}else if(i.equals("720")){
	        		changeUser();
	        	}else if(i.equals("800")){
	        		// Void
	        		secureSend("E000");
	        	}else if(i.equals("900")) {
	        		// Close
	        		secureSend("910");
	        		System.out.println(this.kClient + ": Fin de conexion");
	        		return;
	        	}
	        	else{
	            	System.out.println(this.kClient + ": " + i);
	            	secureSend("E100");
            	}
	        	
            	i = (String)secureReceive();
            }
            out.close();
            in.close();
            clientSocket.close();
		}
		catch(Exception e) {
			 e.printStackTrace();
		}
	}
	   
	public void connect() throws Exception{
		 System.out.println(kClient + ": Conectando...");
		 try {
			 // TODO Clean method
			 out.writeObject(keyPair.getPublic());
			 //System.out.println("Emitida clave pública...");
			 
			 SealedObject i = (SealedObject) in.readObject();
			 //System.out.println("Recibida clave secreta...");
			 
			 //System.out.println("Desencriptando clave secreta...");
			 Cipher c = Cipher.getInstance("RSA");
			 c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			 connectionKey = (Key) i.getObject(c);
			 
			 //System.out.println("Clave secreta obtenida con exito!");
			 //System.out.println(connectionKey);
			 
			 //System.out.println("Encriptando socket..");
			 c = Cipher.getInstance("AES");
			 c.init(Cipher.ENCRYPT_MODE, connectionKey);
			 SealedObject socketEncrypted = new SealedObject("010", c);
			 //System.out.println(socketEncrypted);
			 
			 System.out.println(kClient + ": Conexión segura!");
			 
			 out.writeObject(socketEncrypted);
			 secureConnection=true;
		 }
		 catch(Exception e) {
			 System.out.println(e);
		 }
	 }
	 public void login() throws Exception {
		 if(userID!=Integer.MAX_VALUE) {
			 secureSend("E101");
			 return;
		 }
		 secureSend("101");
		 Object user = secureReceive();
		 Object pasw = secureReceive();
		 
		 if(user.getClass().equals(String.class) && user.getClass().equals(String.class)) {
			 // Check keys
			 ResultSet rs = sqlSentence.executeQuery("SELECT * FROM user WHERE user='" + user + "'");
			 if(rs.next()) {
				 secureSend("102");
				 byte[] pwd = rs.getBlob("pwd").getBytes(1, (int) rs.getBlob("pwd").length());
				 if(Arrays.equals(pwd, (byte[])pasw)){
					 secureSend("103");
					// Send keys
					 Blob privb = rs.getBlob("private");
					 Blob pubb = rs.getBlob("public");
					 
					 byte[] priv = privb.getBytes(1, (int) privb.length());
					 byte[] pub = pubb.getBytes(1, (int) pubb.length());
					 
					 secureSend(pub);
					 secureSend(priv);
					 
					 userID = rs.getInt("id");
					 
					 System.out.println(kClient + ": Login: " + user);
				 }
				 else {
					 secureSend("E103"); // Invalid pass
					 System.out.println("Error clave");
				 }
			 }
			 else {
				 secureSend("E102"); // Invalid user
			 }
		 }
	 }
	 public void logout() throws Exception {
		 if(userID == Integer.MAX_VALUE) {
			 secureSend("E121");
		 }else {
			 userID=Integer.MAX_VALUE;
			 secureSend("121");
			 System.out.println(kClient + ": Logout");
		 }
	 }
	 public void signIn() throws Exception{
		 if(userID!=Integer.MAX_VALUE) {
			 secureSend("E100");
			 return;
		 }
		 secureSend("111");
		 
		 String newuser =(String) secureReceive();
		 byte[] pwdh = (byte[]) secureReceive();
		 
		 ResultSet rs = sqlSentence.executeQuery ("SELECT user FROM user WHERE user = '"+newuser+"' LIMIT 1");
		 if(rs.first()) {
			 secureSend("E111");//USUARIO YA REGISTRADO
			 return;
		 }
		 else {
			 secureSend("112");
		 }
		 
		 byte[] pub = (byte[]) secureReceive();
		 byte[] priv = (byte[]) secureReceive();
		 
		 PreparedStatement ps = sqlConnection.prepareStatement("INSERT INTO user(user, pwd, private, public) VALUES('"+ newuser +"', ?, ?, ?)",
				 PreparedStatement.RETURN_GENERATED_KEYS);
		 
		 ps.setBytes(1, pwdh);
		 ps.setBytes(2, priv);
		 ps.setBytes(3, pub);
		 ps.executeUpdate();
		 rs = ps.getGeneratedKeys();
	     if(rs.first())
	     {
	         userID = rs.getInt(1);
	         secureSend("113");
	         System.out.println(kClient + ": Signin id:" +userID+ " username:" + newuser);
	     }else {
	    	 secureSend("E113");
	     }
		 ps.close();
		 
	 }
	 public void upload() throws Exception{
		 if(userID== Integer.MAX_VALUE) {
			 // Not registered
			 secureSend("E301");
			 return;
		 }
		 secureSend("301");
		 
		 String filename = (String) secureReceive();
		 byte[] file = (byte[]) secureReceive(); // TODO Repeat send
		 byte[] key = (byte[]) secureReceive();
		 
		 // Upload file
		 String sentence = "INSERT INTO file(data, name) " + "VALUES(?, '" + filename + "')";
		 PreparedStatement ps = sqlConnection.prepareStatement(sentence, PreparedStatement.RETURN_GENERATED_KEYS);
		 
		 ps.setBytes(1, file);
		 ps.executeUpdate();
		 
		 // Upload key
		 ResultSet rs = ps.getGeneratedKeys();    		 
		 if(rs.first()){
	         int fileID = rs.getInt(1);
	         sentence = "INSERT INTO fileuser(user, file, secretKey, shared) " + "VALUES('"+userID+"', "+ fileID +", ?, '"+ userID + "')";
			 ps = sqlConnection.prepareStatement(sentence);
	 		 ps.setBytes(1, key);
	 		 ps.executeUpdate();
	 		 System.out.println(kClient + ": Fichero subido: " + filename);
			 secureSend("303"); // OK
	     }else{
	    	 secureSend("E302"); // Error uploading file
	     }
	 }
	 public void share() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E600");
			 return;
		 }
		 else {
			 secureSend("601");
		 }
	
		 int file = (int) secureReceive();
		 String usu = (String) secureReceive();
		 int usuid;
		 byte[] ku;
		 byte[] kf;
	
		 // Search the file and save the key
		 ResultSet rs = sqlSentence.executeQuery("SELECT secretKey "
		 		+ "FROM fileuser "
		 		+ "WHERE user='"+ userID +"' "
		 		+ "AND file= " + file);
		 if(!rs.first()) {
			 secureSend("E601");
			 return;
		 }
		 else {
			 kf = rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length());
		 }
		 
		 // Search the user to share with and upload key
		 rs = sqlSentence.executeQuery("SELECT id, public "
			 		+ "FROM user "
			 		+ "WHERE user='"+ usu +"'");
		 if(!rs.first()) {
			 secureSend("E602");
			 return;
		 }
		 else {
			secureSend("601");
			usuid = rs.getInt("id");
			ku = rs.getBlob("public").getBytes(1, (int) rs.getBlob("public").length());
		 }
		 
		 // Send keys
		 secureSend(kf);
		 secureSend(ku);
		 
		 byte[] k = (byte[]) secureReceive();
		 
		 // Update table
		 String sentence = "INSERT INTO fileuser(user, file, secretKey, shared) " + "VALUES("+usuid+", "+file+", ? , "+ userID +" )";
		 PreparedStatement ps = sqlConnection.prepareStatement(sentence, PreparedStatement.RETURN_GENERATED_KEYS);
		 
		 ps.setBytes(1, k);
		 try {
			 ps.executeUpdate();
		 }
		 catch(SQLException e) {
			 secureSend("E603"); // Already shared
			 System.out.println(e);
			 return;
		 }
		 secureSend("603");
	 }
	 public void download() throws Exception{
		 if(userID == Integer.MAX_VALUE) {
			 secureSend("E401");
			 return;
		 }
		 secureSend("401");
		 
		 int fileID = (int) secureReceive();
		 
		 ResultSet rs = sqlSentence.executeQuery("SELECT * "
		 		+ "FROM fileuser, file "
		 		+ "WHERE fileuser.user = '"+ userID +"' "
		 		+ "AND file.id = fileuser.file "
		 		+ "AND file.id = "+fileID+" LIMIT 1");
		 if(!rs.first()) {
			 secureSend("E402");
			 return;
		 }
		 else {
			 secureSend("402");
			 secureSend(rs.getBlob("data").getBytes(1, (int) rs.getBlob("data").length()));
			 secureSend(rs.getBlob("secretKey").getBytes(1, (int) rs.getBlob("secretKey").length()));
			 secureSend(rs.getString("name"));
			 
			 String name =rs.getString("name");
			 System.out.println(kClient + ": DesecureConnectionargado fichero: " + name + " id: " + fileID);
		 }
	 }
	 public void check() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E201");
		 }else {
			 secureSend("201");
		 }
		 
		 // Search in the DDBB
		 ResultSet rs = sqlSentence.executeQuery("SELECT f.name filename, f.id fileid, u.user shared "
		 		+ "FROM fileuser r, file f, user u "
		 		+ "WHERE r.shared = u.id "
		 		+ "AND r.file = f.id "
		 		+ "AND r.user = "+userID+" ");

		 ArrayList<Integer>  id = new ArrayList<Integer>();
		 ArrayList<String> shared = new ArrayList<String>();
		 ArrayList<String> name = new ArrayList<String>();
		 
		 while(rs.next()) {
			 id.add(rs.getInt("fileid"));
			 shared.add(rs.getString("shared"));
			 name.add(rs.getString("filename"));
		 }
		 
		 secureSend(id.toArray());
		 secureSend(shared.toArray());
		 secureSend(name.toArray());
	 }
	 public void delete() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E501");
			 return;
		 }
		 else {
			 secureSend("501");
		 }
		 
		 int r= (Integer) secureReceive();
		 
		 String query = "DELETE FROM fileuser WHERE user=" + userID + " AND file=" + r;
		 try {
			 sqlSentence.executeQuery(query);
		 }
		 catch(SQLException e){
			 secureSend("E502");
			 return;
		 }
		 secureSend("502");
	 }
	 public void changePasecureSendword() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E711");
		 }
		 else {
			 secureSend("711");
		 }
		 
		 byte[] actual = (byte[]) secureReceive();
		 byte[] nueva = (byte[]) secureReceive();
		 byte[] priv = (byte[]) secureReceive();
		 
		 PreparedStatement ps = sqlConnection.prepareStatement("UPDATE user SET "
		 		+ "pwd = ?, "
		 		+ "private = ? "
		 		+ "WHERE id = "+userID
		 		+ " AND pwd = ?");
		 ps.setBytes(1, nueva);
		 ps.setBytes(2, priv);
		 ps.setBytes(3, actual);
		 int i = ps.executeUpdate();
		 
		 if(i < 1) {
			 secureSend("E712");
		 }else if(i>1) {
			 secureSend("E713");
		 }else {
			 secureSend("712");
		 } 
	 }
	 public void changeUser() throws Exception{
		 if(userID==Integer.MAX_VALUE) {
			 secureSend("E721");
		 }else {
			 secureSend("721");
		 }
		 
		 String name = (String) secureReceive();
		 
		 ResultSet rs = sqlSentence.executeQuery("SELECT id FROM user WHERE user.user='"+name+"'");
		 if(rs.first()) {
			 secureSend("E722");
			 return;
		 }
		 String query = "UPDATE user SET user='"+name+"' WHERE id = "+userID;
		 System.out.println(query);
		 int r = sqlSentence.executeUpdate("UPDATE user SET user.user='"+name+"' WHERE id = "+userID);
		 if(r==1) {
			 secureSend("722");
		 }
		 else {
			 secureSend("723");
		 }
	 }
	 
	public void secureSend(Object o) throws Exception{
		 if(secureConnection) {
			 Cipher c = Cipher.getInstance("AES");
			 c.init(Cipher.ENCRYPT_MODE, connectionKey);
			 SealedObject socketEncrypted = new SealedObject((Serializable) o, c);
			 out.writeObject(socketEncrypted);
		 }
		 else {
			 out.writeObject(o);
		 }
    }
	
	public Object secureReceive() throws Exception {
		 if(secureConnection) {
			 SealedObject socket = (SealedObject) in.readObject();
			 Cipher c = Cipher.getInstance("AES");
			 c.init(Cipher.DECRYPT_MODE, connectionKey);
			 return socket.getObject(c);
		 }else {
			 return in.readObject();
		 }
	}
	
	public KeyPair buildKeyPair(){
		try {
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(2048);      
	        return keyPairGenerator.genKeyPair();
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return null;
    }
}
