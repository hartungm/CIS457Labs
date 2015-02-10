import java.io.*;
import java.net.*;
import java.util.*;
//this Java will be a little different than normal...
//...because Kalafut hates Java and everything it stands for.

class UDPClient {
    public static void main(String args[]) throws Exception {
		String sentMessage = "";
		Scanner scanner = new Scanner(System.in);

		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(5000);

		System.out.println("Enter server IP Address: ");
		String ipAddr = scanner.next();
		InetAddress ipAddress = InetAddress.getByName(ipAddr);

		System.out.println("Enter port number: ");
		int portNo = scanner.nextInt();

		Runnable r = new ClientListener(socket);
		Thread t = new Thread(r);
		t.start();

		byte[] sentMessageBytes = new byte[1024];
		while(!sentMessage.equals("/exit")) {		
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter a message: ");
			sentMessage = inFromUser.readLine();
			sentMessageBytes = sentMessage.getBytes();
			
			DatagramPacket sendPacket = new DatagramPacket(sentMessageBytes, sentMessageBytes.length, ipAddress, portNo);
			socket.send(sendPacket);
			Thread.sleep(100);
		}

		t.interrupt();
		socket.close();
    }
}

class ClientListener implements Runnable {

    private DatagramSocket socket;
	
    ClientListener(DatagramSocket socket) {
		this.socket = socket;
    }

    public void run() {
        while(true) {
            try {
                if(Thread.interrupted()) {
                    break;
				}
                
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				socket.receive(receivePacket);
				String serverMessage = new String(receivePacket.getData());
				serverMessage = serverMessage.trim();

				if (serverMessage != null) {
					System.out.println(serverMessage);
				} else {
					System.out.println("Server DIED!!!");
					break;
				}
            }catch(SocketTimeoutException e) {
                
            } catch (IOException e1) {
				System.out.println("Client Closed.");
				return;
			}
        }
    }
}
