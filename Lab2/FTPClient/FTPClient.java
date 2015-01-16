import java.io.*;
import java.net.*;
import java.util.*;

class FTPClient {
    public static void main(String args[]) throws Exception {
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP Address: ");
        String ipAddr = scanner.next();
        System.out.print("Enter port number: ");
        int portNo = scanner.nextInt();
        Socket clientSocket;
        try {
            clientSocket = new Socket(ipAddr, portNo);
        } catch (Exception e) {
            System.out.println("Error connecting to server.");
            return;
        }

        // Initialized reader objects
        BufferedInputStream inFromServer = new BufferedInputStream(clientSocket.getInputStream());
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        // Read file path from user and request from server
        System.out.print("Enter a file path: ");
        String filePath = scanner.next();
        outToServer.writeBytes(filePath+"\n");
        
        //read first byte from file
        int b = inFromServer.read();
        if (b == -1) {
            System.out.println("File Not Found On Server");
        } else {
            //Initialized File creator object
            FileOutputStream fileFromServer = new FileOutputStream(filePath);
            fileFromServer.write(b); //write first byte to file
            
            //grab rest of bytes 1 at a time
            while (true) {
                b = inFromServer.read();
                if (b == -1)  //signifies end of file
                    break;
                //write bytes to file
                fileFromServer.write(b);
            }
        }
        //close socket connection
        clientSocket.close();
    }
}
