#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>

#define REQUEST_TYPE 1
#define ACK_TYPE 2
#define HEADER_SIZE 12
#define BUFFER_SIZE 10

struct Packet {
	int totalPackets;
	int packetIndex;
    int dataSize;
	char data[1024];
};

int readInt(char *startIndex);
int addToBuffer(struct Packet packet, struct Packet *buffer, int bufferSize);
int getChecksum(char *packet, int packetLength);
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
	
	sendto(sockfd, filePath, strlen(filePath) + 1, 0, (struct sockaddr*)&serveraddr, sizeof(struct sockaddr_in));
	
	struct Packet packetBuffer[BUFFER_SIZE];
	struct Packet tempPacket;
	int i;
	for(i = 0; i < BUFFER_SIZE; i++) {
		packetBuffer[i].totalPackets = -1;
	}
	
	unsigned int len = sizeof(struct sockaddr_in);
	int packetWriteIndex = 0;
	FILE *fp = fopen("/home/carlton/Desktop/test.png", "w");
	
	do {
		int n = recvfrom(sockfd, tempPacket.data, 1024, 0, (struct sockaddr*)&serveraddr, &len);
		if(n < 0){
			printf("Sorry, had a problem receiving a response from the server.\n");
			return 1;
		}
        
		tempPacket.totalPackets = readInt(tempPacket.data + 4);
		tempPacket.packetIndex = readInt(tempPacket.data + 8);
		
		if(tempPacket.totalPackets == 0 && tempPacket.packetIndex == 0) {
			printf("The server couldn't find the file.\n");
			return 1;
		}
		
        tempPacket.dataSize = n - HEADER_SIZE;
        
        int checksum = readInt(tempPacket.data);
        if(checksum == getChecksum(tempPacket.data + 4, tempPacket.dataSize)) {
            int added = addToBuffer(tempPacket, packetBuffer, BUFFER_SIZE);
            if(added == 0) {
                printf("The packet buffer overflowed!!\n");
                return 1;
            }
            
            sendAck(tempPacket.packetIndex, sockfd, (struct sockaddr*)&serveraddr, sizeof(serveraddr));
        }
        
		packetWriteIndex = writePacket(packetBuffer, BUFFER_SIZE, packetWriteIndex, fp);

	} while(packetWriteIndex < tempPacket.totalPackets);
	

	return 0;
}

int readInt(char *startIndex) {
	
	int result = 0;
	
	result = (unsigned char)startIndex[0] << 24;
    result |= (unsigned char)startIndex[1] << 16;
    result |= (unsigned char)startIndex[2] << 8;
    result |= (unsigned char)startIndex[3];
	
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

int getChecksum(char *packet, int packetLength) {
    
    unsigned int checksum = 0;
    
    int i = 0;
    for(i = 0; i < packetLength - 4; i++) {
        checksum += (int)packet[i];
    }
   
    return checksum;
}

void sendAck(int packetIndex, int sockfd, const struct sockaddr *dest_addr, socklen_t addrlen) {
	
	char ackPacket[5];
	
	ackPacket[0] = ACK_TYPE;
    ackPacket[1] = (packetIndex >> 24) & 0xFF;
    ackPacket[2] = (packetIndex >> 16) & 0xFF;
    ackPacket[3] = (packetIndex >> 8) & 0xFF;
    ackPacket[4] = packetIndex & 0xFF;
	
	printf("sending ack...%d\n",packetIndex);
	sendto(sockfd, ackPacket, 5, 0, dest_addr, addrlen);
}

int writePacket(struct Packet *buffer, int bufferSize, int lastPacketIndex, FILE *fp) {
	
	int i;
	for(i = 0; i < bufferSize; i++) {
		if(buffer[i].packetIndex == lastPacketIndex) {
			fwrite(buffer[i].data + HEADER_SIZE, sizeof(char), buffer[i].dataSize, fp);
			buffer[i].totalPackets = -1;
			return lastPacketIndex + 1;
		}
	}
	
	return lastPacketIndex;
}
