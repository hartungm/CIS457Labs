#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define MAX_PACKET_SIZE 512

uint16_t convertBytesToShort(unsigned char high, unsigned char low);
void convertShortToBytes(uint16_t shortVal, unsigned char charArray[]);

int main(int argc, char* argv[])
{
	int portNum;
	struct sockaddr_in resolverAddr, dnsAddr;
	// Command line arguments
	if(argc > 1)
	{
		portNum = atoi(argv[1]);
	}
	else
	{
		printf("No port number specified, setting to 9876\n");
		portNum = 9876;
	}

	// Declare resolver information 
	int socketfd = socket(AF_INET, SOCK_DGRAM, 0);
	resolverAddr.sin_family = AF_INET;
	resolverAddr.sin_port = htons(portNum);
	resolverAddr.sin_addr.s_addr = INADDR_ANY;

	// Declare dnsAddr information
	int dnsSocketFd = socket(AF_INET, SOCK_DGRAM, 0);
	dnsAddr.sin_family = AF_INET;
	dnsAddr.sin_port = htons(53);
	inet_aton("192.58.128.30", &dnsAddr.sin_addr);

	// Bind the sockets to their respective addresses
	bind(socketfd, (struct sockaddr*) &resolverAddr, sizeof(resolverAddr));

	while(1)
	{
		unsigned int length = sizeof(resolverAddr);
		unsigned char packet[MAX_PACKET_SIZE];
		recvfrom(socketfd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &resolverAddr, &length);
		packet[2] &= 0xfe;
		sendto(dnsSocketFd, packet, (sizeof(unsigned char) * 512), 0, (struct sockaddr*) &dnsAddr, sizeof(dnsAddr));
		
		// uint16_t id = convertBytesToShort(packet[1], packet[0]);
		// uint16_t flags = convertBytesToShort(packet[3], packet[2]);
		// flags &= 0xfeff;
		// uint16_t qdCount = convertBytesToShort(packet[5], packet[4]);
		// uint16_t anCount = convertBytesToShort(packet[7], packet[6]);
		// uint16_t nsCount = convertBytesToShort(packet[9], packet[8]);
		// uint16_t arCount = convertBytesToShort(packet[11], packet[10]);
	}
}

uint16_t convertBytesToShort(unsigned char high, unsigned char low)
{
	 return ((uint16_t) low | ((uint16_t) high << 8));
}

void convertShortToBytes(uint16_t shortVal, unsigned char charArray[])
{
	charArray[0] = shortVal & 0xFF;
	charArray[1] = shortVal >> 8;
}