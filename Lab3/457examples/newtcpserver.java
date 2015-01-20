mport java.io.*;
import java.net.*;

class newtcpserver {
    public static void main(String argv[]) throws Exception{
        ServerSocket listenSocket = new ServerSocket(9876);
        while(true) {
            Socket s=listenSocket.accept();
            Runnable r new ClientHandler(s);
            Thread t = new Thread(r);
            t.start();
        }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    ClientHandler(Socket connection) {
        connectionSocket = connection;
    }
    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionScoket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStreamI());
            String clientMessage = inFromClient.readLine();
            System.out.println("The Client said: " + clientMessage);
            outToClient.writeBytes(clientMessage+'\n');
            connectionSocket.close();
        } catch (Exception e) {
            System.out.println("an error happened");
        }
    }
}
