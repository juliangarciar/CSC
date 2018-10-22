import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        // Socket for server to listen at.
        ServerSocket listener = new ServerSocket(8080, 0, InetAddress.getByName("192.168.43.111"));
        //System.out.println("Server is now running at port: " + portNum);
        System.out.println(listener.getInetAddress());
        // Simply making Server run continously.
        while (true) {
            try {
                // Accept a client connection once Server recieves one.
                Socket clientSocket = listener.accept();

                // Creating inout and output streams. Must creat out put stream first.
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                // Reading in Integer Object from input stream.
                String i = (String)in.readObject();
                out.writeObject("QUE TE FOLLEN");
                while(!i.isEmpty()) {
                	i = (String)in.readObject();
	                if(i.equals("SEND")) {
	                	System.out.println("GOT IT");
	                	out.writeObject("QUE TE FOLLEN");
	                }
	                else if(i.equals("QUIT")) {
	                	break;
	                }
                }
                //Sending response back to client
                //String response = "Integer Object Received.";
                //out.writeObject(response);

                // Outputting recieved Integer Object.
                System.out.println("Received integer: " + i);
                out.close();
                in.close();
                clientSocket.close();
                break;
            } finally {
                      // Closing Server Socket now.
                      listener.close();
            } 
        }
    }
}