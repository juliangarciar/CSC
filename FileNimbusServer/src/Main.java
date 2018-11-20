public class Main {

	public static void main(String[] args) {
		int port = 8080;
		String ip = "localhost";
		
		try{
			Server server = new Server(port, ip);
			server.initServer();
		}
		catch(Exception e) {
			System.out.println("El servidor no pudo iniciarse.");
		}
		return;
	}
}
