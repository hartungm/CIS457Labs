import java.io.*;
import java.net.*;
import java.util.*;
//this Java will be a little different than normal...
//...because Kalafut hates Java and everything it stands for.

class ChatRoomClient {
    public static void main(String args[]) throws Exception {
        String message = "";
        Socket clientSocket = new Socket("127.0.0.1",9876);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Runnable r = new ClientListener(inFromServer);
        Thread t = new Thread(r);
        t.start();

        while(message != "exit") {
            
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter a message: ");
            message = inFromUser.readLine();
            outToServer.writeBytes(message+'\n');

        }

        t.interrupt();
        clientSocket.close();
    }
}

class ClientListener implements Runnable {

    BufferedReader reader;
    ClientListener(BufferedReader inFromServer) {
        this.reader = inFromServer;
    }

    public void run() {
        while(true) {
            try {
                if(Thread.interrupted()) {
                    break;
                }
                String serverMessage = reader.readLine();
                System.out.println(serverMessage);
            }
            catch(IOException e) {
                return;
            }
        }
    }
}
