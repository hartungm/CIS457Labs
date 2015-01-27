import java.io.*;
import java.net.*;
import java.util.*;

class HTTPServer {
    public static void main(String argv[]) throws Exception {
        ServerSocket listenSocket = new ServerSocket(8080);
        while(true) {
            Socket s = listenSocket.accept();
            Runnable r = new ClientHandler(s);
            Thread t = new Thread(r);
            t.start();
        }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    ClientHandler(Socket connection) {
        this.connectionSocket = connection;
    }
    public void run() {
        try {
            BufferedInputStream reader = new BufferedInputStream(connectionSocket.getInputStream());
            byte[] arrayOfBytes = new byte[5000];
            reader.read(arrayOfBytes, 0, 5000);
            String request = new String(arrayOfBytes);
            String[] requestLines = request.split("\r\n");
            if(requestLines[0].contains("GET") && requestLines[0].contains("HTTP/1.1")) {
                String[] getLine = requestLines[0].split(" ");
                String filePath = "." + getLine[1]; 
            }
        } 
        catch(IOException e) {
            return;
        }
        catch (Exception e) {
            System.out.println("an error happened");
        }
    }

}
