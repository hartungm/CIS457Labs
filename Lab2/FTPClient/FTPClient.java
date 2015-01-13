import java.io.*;
import java.net.*;

class FTPClient {
    public static void main(String args[]) throws Exception {
        
        Socket clientSocket = new Socket("127.0.0.1", 9876);

        // Initialized reader objects
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        // Read file path from client and find the file
        System.out.print("Enter a file path: ");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.readLine();

        outToServer.writeBytes(filePath);

        // TODO: Read File in, 1 byte at a time and save to a file

        // File serverFile = new File(filePath);
        // FileInputStream fileToClient = new FileInputStream(serverFile);

        // // Send file to client
        // System.out.println("File Requested: " + serverFilePath);
        // for (long i = 0; i < serverFile.length(); i++) {
        //     outToClient.writeByte(fileToClient.read());
        // }
        clientSocket.close();
        
    }
}