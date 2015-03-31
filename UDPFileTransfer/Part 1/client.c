#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

#define REQUEST_TYPE 1
#define ACK_TYPE 2
#define HEADER_SIZE 8
#define BUFFER_SIZE 10

struct Packet {
	int totalPackets;
	int packetIndex;
	char data[1024];
};

int readInt(char *startIndex);
int addToBuffer(struct Packet packet, struct Packet *buffer, int bufferSize);
void sendAck(int packetIndex, int sockfd, const struct sockaddr *dest_addr, socklen_t addrlen);
int writePacket(struct Packet *buffer, int bufferSize, int lastPacketIndex, FILE *fp);

int main(int argc, char** argv){

	int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
	if(sockfd < 0){
		printf("There was an error creating the socket\n");
		return 1;
	}

	struct timeval to;
	to.tv_sec = 5;
	to.tv_usec = 0;

	struct sockaddr_in serveraddr;
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_port = htons(9876);
	serveraddr.sin_addr.s_addr = inet_addr("127.0.0.1");

	setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &to, sizeof(to));

	printf("Enter a file path: ");
	char filePath[1024];
	filePath[0] = REQUEST_TYPE;
	fgets(filePath + 1, 1023, stdin);
	
	sendto(sockfd, filePath, strlen(filePath), 0, (struct sockaddr*)&serveraddr, sizeof(struct sockaddr_in));
	
	struct Packet packetBuffer[BUFFER_SIZE];
	struct Packet tempPacket;
	int i;
	for(i = 0; i < BUFFER_SIZE; i++) {
		packetBuffer[i].totalPackets = -1;
	}
	
	int len = sizeof(struct sockaddr_in);
	int packetWriteIndex = 0;
	FILE *fp = fopen("/home/carlton/Desktop/test.txt", "w");
	
	do {
		int n = recvfrom(sockfd, tempPacket.data, 1024, 0, (struct sockaddr*)&serveraddr, &len);
		if(n < 0){
			printf("Sorry, had a problem receiving a response from the server.\n");
			return 1;
		}
		
		tempPacket.totalPackets = readInt(tempPacket.data);
		tempPacket.packetIndex = readInt(tempPacket.data + 4);
		
		if(tempPacket.totalPackets == 0 && tempPacket.packetIndex == 0) {
			printf("The server couldn't find the file.\n");
			return 1;
		}
		
		int added = addToBuffer(tempPacket, packetBuffer, BUFFER_SIZE);
		if(added == 0) {
			printf("The packet buffer overflowed!!\n");
			return 1;
		}
		
		sendAck(tempPacket.packetIndex, sockfd, (struct sockaddr*)&serveraddr, sizeof(serveraddr));
		int packetWriteIndex = writePacket(packetBuffer, BUFFER_SIZE, packetWriteIndex, fp);
		
		printf("%d, %d\n", packetWriteIndex, tempPacket.totalPackets);
	} while(packetWriteIndex < tempPacket.totalPackets);
	

	return 0;
}

int readInt(char *startIndex) {
	
	int result = 0;
	
	result = (int)startIndex[0] << 24;
	result |= (int)startIndex[1] << 16;
	result |= (int)startIndex[2] << 8;
	result |= (int)startIndex[3];
	
	return result;
}

int addToBuffer(struct Packet packet, struct Packet *buffer, int bufferSize) {
	
	int i;
	for(i = 0; i < bufferSize; i++) {
		if(buffer[i].totalPackets == -1) {
			buffer[i] = packet;
			return 1;
		}
	}
	
	return 0;
}

void sendAck(int packetIndex, int sockfd, const struct sockaddr *dest_addr, socklen_t addrlen) {
	
	char ackPacket[5];
	
	ackPacket[0] = ACK_TYPE;
	ackPacket[1] = (char)(packetIndex & 0xFF000000) >> 24;
	ackPacket[2] = (char)(packetIndex & 0x00FF0000) >> 16;
	ackPacket[3] = (char)(packetIndex & 0x0000FF00) >> 8;
	ackPacket[4] = (char)(packetIndex & 0x000000FF);
	
	sendto(sockfd, ackPacket, 5, 0, dest_addr, addrlen);
}

int writePacket(struct Packet *buffer, int bufferSize, int lastPacketIndex, FILE *fp) {
	
	int i;
	
	int poo;
	for(poo = 0; poo < 1024; poo++) {
		printf("%d - %c\n", poo, buffer[0].data[poo]);
	}
	
	printf("%d\n", lastPacketIndex);
// 	fwrite(buffer[0].data + HEADER_SIZE, sizeof(char), 1024 - HEADER_SIZE, fp);
	for(i = 0; i < bufferSize; i++) {
		if(buffer[i].packetIndex == lastPacketIndex) {
			printf("wrote %d\n", i);
			printf("packetindex %d\n", buffer[i].packetIndex);
			fwrite(buffer[i].data + HEADER_SIZE, sizeof(char), 1024 - HEADER_SIZE, fp);
			
			buffer[i].totalPackets = -1;
			return lastPacketIndex + 1;
		}
	}
	
	return lastPacketIndex;
}