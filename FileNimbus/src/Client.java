import java.io.*;
import java.net.*;


public class Client {

    public static void main(String arg[]) throws IOException, ClassNotFoundException {

        int portNum = 8080;

        Socket socket = new Socket("192.168.43.111", portNum);

        // Integer Object to send to Server.
        Integer num = new Integer(50);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        
        out.writeObject("SEND");
        String response = (String) in.readObject();
        while (!response.isEmpty()){
        	out.writeObject("SEND");
        	response = (String) in.readObject();
        }

        System.out.println("Server message: " + response);
        
        //socket.close();
        
    }
}