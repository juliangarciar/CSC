import javax.swing.JFileChooser;

public class OpenFile{
    JFileChooser fileChooser = new JFileChooser();
    StringBuilder sb = new StringBuilder();

    public void PickMe() throws Exception{
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            // Get the file
            java.io.File file = fileChooser.getSelectedFile();
        }
    }
}