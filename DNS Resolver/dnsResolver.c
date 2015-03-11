#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#define MAX_PACKET_SIZE 512
#define ANSWER_RECORD_SIZE 32
#define QUESTION_RECORD_SIZE 32

uint16_t convertBytesToShort(unsigned char high, unsigned char low);
void convertShortToBytes(uint16_t shortVal, unsigned char charArray[]);
int findAddressForRequest(unsigned char packet[], int packetSize);
struct sockaddr_in resolverAddr, dnsAddr, clientaddr;
int socketfd, dnsSocketFd;

int main(int argc, char* argv[])
{
	int portNum;
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
	inet_aton("192.112.36.4", &dnsAddr.sin_addr);
	
	// Bind the sockets to their respective addresses
	bind(socketfd, (struct sockaddr*) &resolverAddr, sizeof(resolverAddr));
	
	while(1)
	{
		unsigned int length = sizeof(clientaddr);
		unsigned char packet[MAX_PACKET_SIZE];
		printf("Made it here!\n");
		recvfrom(socketfd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &clientaddr, &length);
		printf("Received packet!\n");
		packet[2] &= 0xfe;
		long result = sendto(dnsSocketFd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &dnsAddr, sizeof(struct sockaddr_in));
		unsigned int dnsLength = sizeof(dnsAddr);
		printf("Send to: %lu\n", result);
		printf("dnslength %d\n", dnsLength);
		recvfrom(dnsSocketFd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &dnsAddr, &dnsLength);
		printf("after receive from, into findAddress\n");
		int i = 0;
		for(i = 0; i < MAX_PACKET_SIZE; i++)
		{
			printf(" %c", packet[i]);
		}
		fflush(stdout);
		findAddressForRequest(packet, MAX_PACKET_SIZE);
		// uint16_t id = convertBytesToShort(packet[1], packet[0]);
		// uint16_t flags = convertBytesToShort(packet[3], packet[2]);
		// flags &= 0xfeff;
		// uint16_t qdCount = convertBytesToShort(packet[5], packet[4]);
		// uint16_t anCount = convertBytesToShort(packet[7], packet[6]);
		// uint16_t nsCount = convertBytesToShort(packet[9], packet[8]);
		// uint16_t arCount = convertBytesToShort(packet[11], packet[10]);
	}
}

int findAddressForRequest(unsigned char packet[], int packetSize)
{
	uint16_t anCount = convertBytesToShort(packet[7], packet[6]);
	if(anCount > 0)
	{
		printf("anCount == 1");
		sendto(socketfd, packet, (sizeof(unsigned char) * 512), 0, (struct sockaddr*) &clientaddr, sizeof(clientaddr));
		return 0;
	}
	else
	{
		uint16_t nsCount = convertBytesToShort(packet[9], packet[8]);
		printf("nscount: %d\n", nsCount);
		if(nsCount > 0)
		{
			int i = 12;
			while(packet[i] != '\0') // Walk through question name
			{
				i++;
			}
			i += 5;
			int theCount = 0;
			while(theCount < nsCount)
			{
				while(packet[i] != '\0') // walks through answer name
				{
					if((packet[i] & 0xc0) == 0xc0)
					{
						
					}
					i++;
				}
				i += 8;
				uint16_t rdLength = convertBytesToShort(packet[i + 1], packet[i]);
				rdLength &= 0x3fff;
				int j = i;
				int index = 0;
				char newRequest[rdLength];
				while(i < rdLength + j)
				{
					if((packet[i] & 0xc0) == 0xc0)
					{
						uint16_t pointer = convertBytesToShort(packet[i + 1], packet[i]);
						pointer &= 0x3fff;
						pointer++;
						int numChars = packet[pointer];
						int endPoint = numChars + pointer;
						while(pointer < endPoint)
						{
							newRequest[index] = packet[pointer];
							pointer++;
						}
					}
					if(isdigit(packet[i]))
					{
						newRequest[index] = '.';
					}
					else
					{
						newRequest[index] = packet[i];
					}
					i++;
					index++;
				}
				dnsAddr.sin_family = AF_INET;
				dnsAddr.sin_port = htons(53);
				inet_aton(newRequest, &dnsAddr.sin_addr);
				sendto(dnsSocketFd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &dnsAddr, sizeof(struct sockaddr_in));
				unsigned int dnsLength = sizeof(dnsAddr);
				recvfrom(dnsSocketFd, packet, MAX_PACKET_SIZE, 0, (struct sockaddr*) &dnsAddr, &dnsLength);
				if(!findAddressForRequest(packet, MAX_PACKET_SIZE))
				{
					return 0;
				}
			}
			
		}
	}
	return -1;
}

uint16_t convertBytesToShort(unsigned char low, unsigned char high)
{
	return ((uint16_t) low | ((uint16_t) high << 8));
}

void convertShortToBytes(uint16_t shortVal, unsigned char charArray[])
{
	charArray[0] = shortVal & 0xFF;
	charArray[1] = shortVal >> 8;
}
