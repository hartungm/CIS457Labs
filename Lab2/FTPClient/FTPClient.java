import java.io.*;
import java.net.*;
import java.util.*;

class FTPClient {
    public static void main(String args[]) throws Exception {
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP Address: ");
        String ipAddr = scanner.next();
        System.out.print("\nEnter port number: ");
        int portNo = scanner.nextInt();
        
        try {
            Socket clientSocket = new Socket(ipAddr, portNo);
        } catch (Exception e) {
            System.out.println("Error connecting to server.");
            return 1;
        }


        // Initialized reader objects
        BufferedInputStream inFromServer = new BufferedInputStream(clientSocket.getInputStream());
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        // Read file path from user and request from server
        System.out.print("Enter a file path: ");
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
