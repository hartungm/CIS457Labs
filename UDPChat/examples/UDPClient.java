import java.io.*;
import java.net.*;

class UDPClient {
    public static void main (String[] args) throws Exception {
        while(true) {
            //for ours, we need a try/catch based random port solution to set our port, so the server can identify where to send the messages
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(5000);
        BufferedReader inFromUser = 
            new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter a message: ");
        String message = inFromUser.readLine();
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();
        InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
        DatagramPacket sendPacket = 
            new DatagramPacket(sendData,sendData.length,IPAddress,9876);
        clientSocket.send(sendPacket);
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket =
            new DatagramPacket(receiveData,receiveData.length);
        try {
            clientSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            System.out.println("Sorry, didn't get a response");
            //UDP: important to have timeouts; no "connection,"
            //just sending packets
            return;
        }
        String serverMessage = new String(receivePacket.getData());
        System.out.println("Got from server: " + serverMessage);
        System.out.println("From port: " + receivePacket.getPort());
    }
    }
}
