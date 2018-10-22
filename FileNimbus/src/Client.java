import java.io.*;
import java.net.*;


public class Client {

    public static void main(String arg[]) throws IOException, ClassNotFoundException {

        int portNum = 8080;

        Socket socket = new Socket("localhost", portNum);

        // Integer Object to send to Server.
        Integer num = new Integer(50);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        
        
        System.out.print(":");
        String input = System.console().readLine();
        out.writeObject(input);
        String response = (String) in.readObject();
        while (!response.isEmpty()){
        	System.out.println("Server message: " + response);
        	
        	System.out.print(":");
            input = System.console().readLine();
        	out.writeObject(input);
        	response = (String) in.readObject();
        }

        
        
        //socket.close();
        
    }
}