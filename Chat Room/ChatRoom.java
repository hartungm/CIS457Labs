import java.io.*;
import java.net.*;

class ChatRoomServer {
    public static void main(String argv[]) throws Exception{
        ServerSocket listenSocket = new ServerSocket(9876);
        ArrayList<Socket> sockets = new ArrayList<Socket>();
        while(true) {
            Socket s=listenSocket.accept();
            sockets.add(s);
            Runnable r = new ClientHandler(s);
            Thread t = new Thread(r);
            t.start();
        }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    String person;
    String clientMessage = "";
    ArrayList<Socket> sockets;
    ClientHandler(Socket connection, String person, ArrayList<Socket> sockets) {
        this.connectionSocket = connection;
        this.person = person;
        this.sockets = sockets;
    }
    public void run() {
        try {
            while(clientMessage != "exit") {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                clientMessage = inFromClient.readLine();

                System.out.println( person + " said: " + clientMessage + '\n');
                for(Socket s: sockets) {
                    DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
                    outToClient.writeBytes(person + " said: " + clientMessage + '\n');
                }
            }
            connectionSocket.close();
        } catch (Exception e) {
            System.out.println("an error happened");
        }
    }


}
