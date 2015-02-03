import java.io.*;
import java.net.*;

class UDPServer {
    public static void main (String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        while(true) {
            byte[] recvData = new byte[1024];
            byte[] sendData = new byte[1024];
            DatagramPacket recvPacket =
                new DatagramPacket(recvData, recvData.length);
            serverSocket.receive(recvPacket);
            String message = new String(recvPacket.getData());
            InetAddress IPAddress = recvPacket.getAddress();
            int port = recvPacket.getPort();
            System.out.println("Got Message: " + message);
            sendData = message.getBytes();
            DatagramPacket sendPacket = 
                new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}
