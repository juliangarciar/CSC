public class Main {

	public static void main(String[] args) throws Exception {
		String ip = "localhost";
		int port = 8080;

		String sqlHost = "localhost";
		String sqlPort = "3306";
		String sqlDb = "filenimbusdb";
		String sqlUser = "cs";
		String sqlPass = "cs123";
		
		Server server = new Server(port, ip, sqlHost, sqlPort, sqlDb, sqlUser, sqlPass);
		
		try{
			server.initServer();
		}
		catch(Exception e) {
			System.out.println("El servidor no pudo iniciarse.");
		}
		return;
	}
}
