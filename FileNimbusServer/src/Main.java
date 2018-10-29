public class Main {

	public static void main(String[] args) throws Exception {
		String sqlHost = "localhost";
		String sqlPort = "3306";
		String sqlDb = "filenimbusdb";
		int port = 8080;
		String ip = "localhost";
		
		Server server = new Server(port, ip, sqlHost, sqlPort, sqlDb);
		
		try{
			server.initServer();
		}
		catch(Exception e) {
			System.out.println("El servidor no pudo iniciarse.");
		}
		return;
	}
}
