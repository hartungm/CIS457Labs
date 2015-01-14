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
			File serverFile = new File(serverFilePath);
			FileInputStream fileToClient = new FileInputStream(serverFile);

			// Send file to client 1 byte at a time
			for (long i = 0; i < serverFile.length(); i++) {
				outToClient.writeByte(fileToClient.read());
			}
            outToClient.writeByte(-1); //signifies end of file
            
            //close connection
			connectionSocket.close();
		}
	}
}
