import java.io.*;
import java.net.*;
import java.util.Random;

class DNSClient {
	public static final short CLASS_IN = 1;
	public static final short TYPE_A = 1;
	
	public static void main(String[] args) throws Exception {
        InetAddress IPAddress = null;
		if(args.length == 0) {
			BufferedReader fileStream = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/resolv.conf")));
			String tempString;
			while((tempString = fileStream.readLine()) != null) {
				String[] tempStringArray = tempString.split(" ");
				if(tempStringArray[0].equals("nameserver"))
				{
					IPAddress = InetAddress.getByName(tempStringArray[1]);
				}
			}
		}
		else {
			IPAddress = InetAddress.getByName(args[0]);
		}
		
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(2000);
		
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
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 53);
		clientSocket.send(sendPacket);
		System.out.println("Sent our query");
		
		byte[] recvData = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
        try {
            clientSocket.receive(recvPacket);
		}
		catch(SocketTimeoutException e) {
			System.err.println("Request Timed Out");
			return;
		}
		
		ByteArrayInputStream byteIn = new ByteArrayInputStream(recvData);
		DataInputStream dataIn = new DataInputStream(byteIn);
		
		int currentPosition = 0;
		
		id = dataIn.readShort();
		System.out.println("\nHEADER INFO:\nID: " + id);
		
		flags = dataIn.readShort();
		
		qcount = dataIn.readShort();
		System.out.println("qcount: " + qcount);
		
		ancount = dataIn.readShort();
		System.out.println("ancount: " + ancount);
		
		authcount = dataIn.readShort();
		System.out.println("authcount: " + authcount);
		
		addcount = dataIn.readShort();
		System.out.println("addcount: " + addcount);
		
		currentPosition += 12;
		
		System.out.println("\nQUESTION:");
		
		int length = 0;
		byte[] name;
		String fullName = "";
		do {
			length = dataIn.readByte();
			name = new byte[length];
			
			byteIn.read(name);
			fullName = fullName + new String(name) + ".";
			
			currentPosition += length + 1;
		} while(length > 0);
		
		fullName = fullName.substring(0, fullName.length() - 2);
		
		System.out.println("qname: " + fullName);
		
		short qtype = dataIn.readShort();
		System.out.println("qtype: " + qtype);
		
		short qclass = dataIn.readShort();
		System.out.println("qclass: " + qclass);
		
		currentPosition += 4;
		
		System.out.println("\nANSWER(S):");
		
		for(int i = 0; i < ancount; i++)
		{
			fullName = "";
		
			length = dataIn.readByte();
			currentPosition += 1;

			if(length < 0) {
				dataIn.reset();
				dataIn.skipBytes(currentPosition - 1);
				
				length = dataIn.readShort();
				currentPosition += 1;
				length &= 0x0003FFF;
				
				dataIn.reset();
				dataIn.skipBytes(length);
				
				int l;
				do {
					l = dataIn.readByte();
					l &= 0x00000FF;
					name = new byte[l];
					byteIn.read(name);
					fullName += new String(name) + ".";
				} while(l > 0);
				
				dataIn.reset();
				dataIn.skipBytes(currentPosition);
			} else {
				dataIn.reset();
				currentPosition -= 1;
				dataIn.skip(currentPosition);
				
				int l;
				do {
					l = dataIn.readByte();
					name = new byte[l];
					byteIn.read(name);
					fullName += new String(name) + ".";
				} while(l > 0);
			}
			
			fullName = fullName.substring(0, fullName.length() - 2);
			System.out.println(fullName);
			
			short type = dataIn.readShort();
			System.out.println("Type: " + type);
			
			short classs = dataIn.readShort();
			System.out.println("Class: " + classs);
			
			int ttl = dataIn.readInt();
			System.out.println("TTL: " + ttl);
			
			short rdlength = dataIn.readShort();
			System.out.println("rdlength: " + rdlength);
			
			String ipAddress = "";
			byte num = dataIn.readByte();
			ipAddress += (0xFF&num) + ".";
			num = dataIn.readByte();
			ipAddress += (0xFF&num) + ".";
			num = dataIn.readByte();
			ipAddress += (0xFF&num) + ".";
			num = dataIn.readByte();
			ipAddress += (0xFF&num);
			
			currentPosition += 14;
			
			System.out.println("rdata: " + ipAddress);
			System.out.println("\n");
		}
	}
}
