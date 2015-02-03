import java.io.*;
import java.net.*;
import java.util.*;

class ChatRoomServer {
    public static void main(String argv[]) throws Exception {
        ServerSocket listenSocket = new ServerSocket(9876);
        ArrayList<Socket> sockets = new ArrayList<Socket>();
        int i = 1;
        while(true) {
            Socket s=listenSocket.accept();
            sockets.add(s);
            Runnable r = new ClientHandler(s, Integer.toString(i++), sockets);
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
        System.out.println(person + " Connected!\nConnected on " + connection.getRemoteSocketAddress().toString().substring(1));
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
            sockets.remove(connectionSocket);
            connectionSocket.close();
        } 
        catch(IOException e) {
            System.out.println(person + " Disconnected!");
            return;
        }
        catch (Exception e) {
            System.out.println("an error happened");
        }
    }

}
