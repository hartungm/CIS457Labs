import java.io.*;
import java.net.*;
import java.util.Random;

class DNSQuery {
    public static void main(String args[]) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(5000);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter a domain name: ");
        String domain = inFromUser.readLine();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(b);
        Random r = new Random();
        short id = (short)r.nextInt();
        short flags = 0;
        flags |= (1<<8);
    }
}
