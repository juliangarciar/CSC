public class Main {

	public static void main(String[] args) {
		String sqlHost = "localhost";
		String sqlPort = "3306";
		String sqlDb = "filenimbusdb";
		int port = 8080;
		String ip = "localhost";
		
		try{
			Server server = new Server(port, ip, sqlHost, sqlPort, sqlDb);
			server.initServer();
		}
		catch(Exception e) {
			System.out.println("El servidor no pudo iniciarse.");
		}
		return;
	}
}
