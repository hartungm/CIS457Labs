import java.io.*;
import java.net.*;
import java.util.*;

class FTPClient {
    public static void main(String args[]) throws Exception {
        
        Socket clientSocket = new Socket("127.0.0.1", 9876);

        // Initialized reader objects
        BufferedInputStream inFromServer = new BufferedInputStream(clientSocket.getInputStream());
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        // Read file path from user and request from server
        System.out.print("Enter a file path: ");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.next();
        outToServer.writeBytes(filePath+"\n");
        
        //Initialized File creator object
        FileOutputStream fileFromServer = new FileOutputStream(filePath);
        
       //Collect bytes until complete 
        while(true) {
            int b = inFromServer.read(); //grab byte from server
            if (b == -1) //if end of file, break loop
                break;
            fileFromServer.write(b); //write byte to file
        }
        
        //close socket connection
        clientSocket.close();
    }
}
