import java.awt.EventQueue;

public class Main{
    
    public static void main(String arg[]) throws Exception{
        final int portNum = 8080;
        final String ip = "localhost";
        
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                try {
                    final ClientGUI clientGUI = new ClientGUI(portNum, ip);
                    clientGUI.setVisible(true);
                    clientGUI.initGUI();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}