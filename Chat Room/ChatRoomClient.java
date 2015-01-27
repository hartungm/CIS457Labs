import java.io.*;
import java.net.*;
import java.util.*;
//this Java will be a little different than normal...
//...because Kalafut hates Java and everything it stands for.

class ChatRoomClient {
    public static void main(String args[]) throws Exception {
        String message = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter server IP Address: ");
        String ipAddr = scanner.next();
        System.out.println("Enter port number: ");
        int portNo = scanner.nextInt();
        Socket clientSocket = new Socket(ipAddr, portNo);
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
                if (serverMessage != null) {
                    System.out.println(serverMessage);
                } else {
                    System.out.println("Server DIED!!!");
                    break;
                }
            }
            catch(IOException e) {
                return;
            }
        }
    }
}
