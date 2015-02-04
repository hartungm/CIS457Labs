import java.io.*;
import java.net.*;
import java.util.ArrayList;

class UDPServer {
    public static void main (String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        ArrayList<ConnectedClient> clientList = new ArrayList<ConnectedClient>();
        while(true) {
            byte[] recvData = new byte[1024];
            byte[] sendData = new byte[1024];
            DatagramPacket recvPacket =
                new DatagramPacket(recvData, recvData.length);
            serverSocket.receive(recvPacket);
            
            String message = new String(recvPacket.getData());
            
            InetAddress ipAddress = recvPacket.getAddress();
            int port = recvPacket.getPort();
            
            boolean existingClient = false;
            for (ConnectedClient client: clientList) {
                if (client.getIP() == ipAddress && client.getPort() == port) {
                    existingClient = true;
                    break;
                }
            }
            //if client doesn't exist, create new and add
            if (!existingClient) {
                ConnectedClient newClient = new ConnectedClient(ipAddress, port);
                clientList.add(newClient);
                System.out.printf("New Client Connected! IP: %s Port: %d\n",ipAddress.getHostAddress(), port);
            }

            if (message.equals("/exit")) {
                //remove client
                System.out.printf("Client Exited! IP: %s Port: %d\n",ipAddress.getHostAddress(), port);
            }

            System.out.println("Got Message: " + message);
            sendData = message.getBytes();

            for (ConnectedClient client : clientList) {
                //send to all other clients
                if (client.getIP() != ipAddress || client.getPort() != port) {
                    DatagramPacket sendPacket = 
                        new DatagramPacket(sendData,sendData.length,client.getIP(), client.getPort());
                    serverSocket.send(sendPacket);
                }
            }
            
        }
    }
}

class ConnectedClient {
    InetAddress ip;
    int port;

    ConnectedClient(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    InetAddress getIP() {
        return this.ip;
    }

    int getPort() {
        return this.port;
    }

}
