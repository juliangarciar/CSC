import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Test {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		
		//Hashear la pass
		SecureRandom random = new SecureRandom();
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		
		/*
		Aqui se puede poner una sal para anadir seguridad, 
		pero no la ponemos porque es aleatoria y no siempre saldria el mismo hash
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		md.update(salt);
		*/
		//byte[] pwdH = md.digest(pwd.getBytes(StandardCharsets.UTF_8));
		//System.out.println("Clave hasheada: ");
		//System.out.println(new String(pwdH, StandardCharsets.UTF_8));
		
		System.out.println("");
		//De nuevo
		byte[] pwdH = md.digest("password".getBytes(StandardCharsets.UTF_8));
		System.out.println("Clave hasheada: ");
		System.out.println(new String(pwdH, StandardCharsets.UTF_8));


	}

}
