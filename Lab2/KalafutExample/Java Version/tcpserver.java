import java.io.*;
import java.net.*;

class tcpserver {
    public static void main (String argv[]) throws Exception {
        ServerSocket listenSocket = new ServerSocket(9876);
        while(true) {
            Socket connectionSocket=listenSocket.accept();
            //accept: blocking call. Will sit until connection.
            BufferedReader inFromClient =
                new BufferedReader(
                        new InputStreamReader(
                            connectionSocket.getInputStream()));
            DataOutputStream outToClient =
                new DataOutputStream(
                        connectionSocket.getOutputStream());
            String clientMessage = inFromClient.readLine();
            System.out.println("The client said: " + clientMessage);
            outToClient.writeBytes(clientMessage+'\n');
            connectionSocket.close();
        }
    }
}
