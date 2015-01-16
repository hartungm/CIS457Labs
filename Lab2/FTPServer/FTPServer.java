import java.io.*;
import java.net.*;

class FTPServer {
	public static void main(String args[]) throws Exception {
	    ServerSocket listenSocket = new ServerSocket(9876);
		while(true) {
			// Accept Connection from client
			Socket connectionSocket = listenSocket.accept();
            
			// Initialized reader objects
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			// Read file path from client and find the file
			String serverFilePath = inFromClient.readLine();
			
            try {
                File serverFile = new File(serverFilePath);
                FileInputStream fileToClient = new FileInputStream(serverFile);

			    // Send file to client 1 byte at a time
			    for (long i = 0; i < serverFile.length(); i++) {
				    int b = fileToClient.read();
                    outToClient.writeByte(b);
			    }
            } catch (FileNotFoundException f) {
                System.out.println("File not found: " + serverFilePath);
            } finally {
                //close connection
                connectionSocket.close();
            }
		}
	}
}
