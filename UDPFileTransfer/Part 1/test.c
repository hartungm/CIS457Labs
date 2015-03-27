#include <stdio.h>
#include <string.h>

#define HEADER_SIZE 8
#define BUFFER_SIZE 10

struct Packet {
	int totalPackets;
	int packetIndex;
	char data[1024];
};

int readInt(char *startIndex);
int addToBuffer(struct Packet packet, struct Packet *buffer, int bufferSize);
int writePacket(struct Packet *buffer, int bufferSize, int lastPacketIndex, FILE *fp);

int main(int argc, char** argv){
	
	/*FILE *fpwrite = fopen("/home/carlton/Desktop/test.txt", "w");
	if(fpwrite == NULL) {
		printf("ERRRORRRR\n");
	}
	char fakePacket[1024];
	
	fakePacket[0] = 0;
	fakePacket[1] = 0;
	fakePacket[2] = 0;
	fakePacket[3] = 20;
	
	fakePacket[4] = 0;
	fakePacket[5] = 0;
	fakePacket[6] = 0;
	fakePacket[7] = 0;
	
	int i;
	int j;
	char a = 'a';
	for(i = 0; i < 20; i++) {
		fakePacket[7] = i;
		for(j = 8; j < 1024; j++) {
			fakePacket[j] = a + i;
		}
		fakePacket[1023] = '\n';
		fwrite(fakePacket, sizeof(char), 1024, fpwrite);
	}
	
	fclose(fpwrite);
	return 0;*/
	
	FILE *fpread = fopen("/home/carlton/Desktop/test.txt", "r");
	
	struct Packet packetBuffer[BUFFER_SIZE];
	struct Packet tempPacket;
	int i;
	for(i = 0; i < BUFFER_SIZE; i++) {
		packetBuffer[i].totalPackets = -1;
	}
	
	int packetWriteIndex = 0;
	FILE *fp = fopen("/home/carlton/Desktop/test2.txt", "w");
	
	do {
		fread(tempPacket.data, sizeof(char), 1024, fpread);
		
		tempPacket.totalPackets = readInt(tempPacket.data);
		tempPacket.packetIndex = readInt(tempPacket.data + 4);
		
		int added = addToBuffer(tempPacket, packetBuffer, BUFFER_SIZE);
		if(added == 0) {
			printf("The packet buffer overflowed!!\n");
			return 1;
		}
		
		int packetWriteIndex = writePacket(packetBuffer, BUFFER_SIZE, packetWriteIndex, fp);
		//printf("%i, %i\n", packetWriteIndex, tempPacket.totalPackets);
		
		
	} while(packetWriteIndex < tempPacket.totalPackets);
	
	fclose(fpread);
	fclose(fp);

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
	
	for(i = 0; i < bufferSize; i++) {
		printf("%i, %i\n", buffer[i].totalPackets, buffer[i].packetIndex);
	}
	
	return 0;
}

int writePacket(struct Packet *buffer, int bufferSize, int lastPacketIndex, FILE *fp) {
	
	int i;
	for(i = 0; i < bufferSize; i++) {
		//printf("%i\n", lastPacketIndex);
		if(buffer[i].packetIndex == lastPacketIndex) {
			fwrite(buffer[i].data + HEADER_SIZE, sizeof(char), 1024 - HEADER_SIZE, fp);
			
			buffer[i].totalPackets = -1;
			return lastPacketIndex + 1;
		}
	}
	
	return lastPacketIndex;
}