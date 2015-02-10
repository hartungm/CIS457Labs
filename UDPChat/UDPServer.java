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
			message = message.trim();
            
            InetAddress ipAddress = recvPacket.getAddress();
            int port = recvPacket.getPort();
            
            boolean existingClient = false;
            for (ConnectedClient client: clientList) {
                if (client.getIP().equals(ipAddress) && client.getPort() == port) {
                    existingClient = true;
                    break;
                }
            }
            //if client doesn't exist, create new and add
            if (!existingClient) {
                ConnectedClient newClient = new ConnectedClient(ipAddress, port);
                clientList.add(newClient);
                System.out.printf("New Client Connected! IP: %s Port: %d\n\n",ipAddress.getHostAddress(), port);
            }
            //for (int j=0; j<clientList.size(); j++) {
            //    System.out.printf("Client %d: IP: %s Port: %d\n", j, clientList.get(j).getIP().getHostAddress(), clientList.get(j).getPort());
            //}
			//System.out.println("\n");

            if (message.equals("/exit")) {
                int i=0;
                while (i<clientList.size() &&
                        !clientList.get(i).getIP().equals(ipAddress) &&
                        clientList.get(i).getPort() != port) {
                    i++;
                }
                clientList.remove(i);
                System.out.printf("Client Exited! IP: %s Port: %d\n",ipAddress.getHostAddress(), port);
            } else {
				System.out.println("Got Message from " + ipAddress.getHostAddress() + ":" + port + ": " + message);
			}
			
			message = ipAddress.getHostAddress() + ":" + port + " said: " + message;
            sendData = message.getBytes();

            for (ConnectedClient client : clientList) {
                //send to all other clients
                if (!client.getIP().equals(ipAddress) || client.getPort() != port) {
                    DatagramPacket sendPacket = 
                        new DatagramPacket(sendData,sendData.length,client.getIP(), client.getPort());
                    serverSocket.send(sendPacket);
                    System.out.printf("Sent to %s:%d\n",client.getIP().getHostAddress(),client.getPort());
                }
            }
			System.out.println();
            String successMessage = "(message sent)";
            sendData = successMessage.getBytes();
            DatagramPacket successPacket =
                new DatagramPacket(sendData,sendData.length,ipAddress,port);
            serverSocket.send(successPacket);
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
