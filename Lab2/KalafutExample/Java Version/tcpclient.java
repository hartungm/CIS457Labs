import java.io.*;
import java.net.*;
//this Java will be a little different than normal...
//...because Kalafut hates Java and everything it stands for.

class tcpclient {
    String message = "";
    public static void main(String args[]) throws Exception {
        while(message != "exit") {
            Socket clientSocket = new Socket("127.0.0.1",9876);
            DataOutputStream outToServer = 
                new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = 
                new BufferedReader(
                        new InputStreamReader(
                            clientSocket.getInputStream()));

            Runnable r = new ClientListener();
            Thread t = new Thread(r);
            t.start();

            BufferedReader inFromUser = 
                new BufferedReader(
                        new InputStreamReader(System.in));
            System.out.println("Enter a message: ");
            message = inFromUser.readLine();
            outToServer.writeBytes(message+'\n');
        }
        clientSocket.close();
        t.interrupt();
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
                if(Thread.isInterrupted()) {
                    break;
                }
                String serverMessage = reader.readLine();
                System.out.println(serverMessage);
            }
            catch(InterruptedException e) {
                return;
            }
        }
    }
}
