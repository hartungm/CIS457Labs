import java.io.*
import java.net.*
import java.util.Random;

class dnsquery {
	public static final short CLASS_IN = 1;
	public static final short TYPE_A = 1;
	public static void main(String args[]) throws Exception {
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(5000);
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter a domain name: ");
		String domain = inFromUser.readLine();
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(b);
		Random r = new Random();
		short id = (short) r.nextInt();
		short flags = 0;
		flags |= (1<<8);
		short qcount = 1;
		short ancount = 0;
		short authcount = 0;
		short addcount = 0;
		d.writeShort(id);
		d.writeShort(flags);
		d.writeShort(qcount);
		d.writeShort(ancount);
		d.writeShort(authcount);
		d.writeShort(addcount);
		String[] labels = domain.split("\\.");
		for(String label : labels) {
			d.writeByte(label.length());
			d.writeBytes(label);
		}
		d.writeByte(0);
		d.writeShort(TYPE_A);
		d.writeShort(CLASS_IN);
		d.flush();
		byte[] sendData = b.toByteArray();
		InetAddress IPAddress = InetAddress.getByName("8.8.8.8");
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData, length, IPAddress, 53);
		clientSocket.send(sendPacket);
		System.out.println("Sent our query");
	}
}