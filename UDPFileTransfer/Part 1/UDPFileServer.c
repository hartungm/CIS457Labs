#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <unistd.h>
#include <pthread.h>
#include <time.h>

#define ACK_TYPE 2
#define HEADER_LENGTH 8
#define ACK_MESSAGE_SIZE 5
#define MAX_PACKET_SIZE 1024
#define SLIDING_WINDOW_SIZE 5
#define MAX_TIME 0x7fffffffffffffff

typedef struct {
	int totalPackets;
	int sockfd;
	struct sockaddr_in* clientAddr;
} AckThreadInfo;

void trimFileName(char *str, int length);
void writeIntToPacket(int num, char *packet);
char* convertIntToByteArray(int num);
void* ackListener(void *args);

int lastAckIndex = -1;
int ackBuffer[5] = {0, 0, 0, 0, 0};

int main (int argc, char **argv)
{
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);

    struct sockaddr_in serveraddr, clientaddr;
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_port = htons(9876);
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    char fileNotFound[] = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    size_t fnfSize = sizeof(fileNotFound);

    bind(sockfd, (struct sockaddr*)&serveraddr, sizeof(serveraddr));

    while(1)
    {
        unsigned int len = sizeof(clientaddr);
        char fileRequest[1024];
        printf("Waiting for a file request...");
        recvfrom(sockfd, fileRequest, 1024, 0, (struct sockaddr*)&clientaddr, &len);
		
		trimFileName(fileRequest, sizeof(fileRequest));
        printf("File requested: %s\n",fileRequest);
        
        FILE *fp = fopen(fileRequest + 1, "r");

        if (fp == NULL)
        {
            printf("File Not Found\n");
            sendto(sockfd, fileNotFound, fnfSize, 0, (struct sockaddr*)&clientaddr, sizeof(clientaddr));
            continue;
        }
        else
        {
			fseek(fp, 0L, SEEK_END);
			long fileSize = ftell(fp);
            fseek(fp, 0L, SEEK_SET);
			
			int totalPackets;
			if(fileSize < MAX_PACKET_SIZE - HEADER_LENGTH) 
			{
				totalPackets = 1;
			} 
			else 
			{
				totalPackets = (int) ceil(fileSize / (MAX_PACKET_SIZE - HEADER_LENGTH)) + 1;
			}
			
			AckThreadInfo ackThreadInfo;
			ackThreadInfo.totalPackets = totalPackets;
			ackThreadInfo.sockfd = sockfd;
			ackThreadInfo.clientAddr = &clientaddr;
			
			pthread_t ackThread;
			pthread_create(&ackThread, NULL, ackListener, &ackThreadInfo);
			sleep(1);

			// Packet Loss Control Variables
			char packetBuffer[SLIDING_WINDOW_SIZE][MAX_PACKET_SIZE];
			time_t sendTimes[SLIDING_WINDOW_SIZE] = { MAX_TIME, MAX_TIME, MAX_TIME, MAX_TIME , MAX_TIME };
			
			int packetIndex = 0;
			while(packetIndex < totalPackets) 
			{
				int packetMod;
				// Check to Make sure no packets are over there timeout time
				for(packetMod = 0; packetMod < SLIDING_WINDOW_SIZE; packetMod++)
				{
					if((time(NULL) - sendTimes[packetMod]) > 2)
					{
						printf("Packet Timeout! Sending another!\n");
						sendto(sockfd, packetBuffer[packetMod], 1024, 0, (struct sockaddr*) &clientaddr, sizeof(clientaddr));
                    }	
				}
				if(packetIndex < lastAckIndex + SLIDING_WINDOW_SIZE) 
				{
					char packet[MAX_PACKET_SIZE];
					writeIntToPacket(totalPackets, packet);
					writeIntToPacket(packetIndex, packet + 4);
					
					int dataLength = fread(packet + 8, sizeof(char), MAX_PACKET_SIZE - HEADER_LENGTH, fp);
					if(dataLength == 0) 
					{
						printf("Error reading from file\n");
					}
					sendto(sockfd, packet, 1024, 0, (struct sockaddr*) &clientaddr, sizeof(clientaddr));
					packetIndex++;

					// Add the packet to the buffer and send time arrays, 
					// because we have a sliding window size of 5 no packet
					// should ever be overwritten that is still in use
					int i;
					for(i = 0; i < MAX_PACKET_SIZE; i++)
					{
						packetBuffer[packetIndex % SLIDING_WINDOW_SIZE][i] = packet[i];
					}
					sendTimes[packetIndex % SLIDING_WINDOW_SIZE] = time(NULL);
				}
				else 
				{
					//*** Handle lost packets here ***//
				}
			}
			sleep(1);
        }
    }
    return 0;
}

void trimFileName(char *str, int length) 
{	
	int j = 0;
	for(j = 0; j < length; j++) 
	{
		if(str[j] == '\n') 
		{
			str[j] = '\0';
			break;
		}
	}
}

void writeIntToPacket(int num, char *packet) 
{    
    packet[0] = (num >> 24) & 0xFF;
    packet[1] = (num >> 16) & 0xFF;
    packet[2] = (num >> 8) & 0xFF;
    packet[3] = num & 0xFF;
    return;
}

int readInt(char *startIndex) 
{	
	int result = 0;
	
	result = (int)startIndex[0] << 24;
	result |= (int)startIndex[1] << 16;
	result |= (int)startIndex[2] << 8;
	result |= (int)startIndex[3];
	
	return result;
}

void* ackListener(void *args) 
{
	AckThreadInfo *info = (AckThreadInfo*)args;
	int totalPackets = info->totalPackets;
	int sockfd = info->sockfd;
	struct sockaddr_in* clientAddr = info->clientAddr;
	unsigned int len = sizeof(*clientAddr);
	
	int i;
	while(lastAckIndex < totalPackets - 1) 
	{
		char ackMessage[ACK_MESSAGE_SIZE];
		
		int result = recvfrom(sockfd, ackMessage, ACK_MESSAGE_SIZE, 0, (struct sockaddr*) &clientAddr, &len);
		
		if(result > 0) 
		{
			if(ackMessage[0] == ACK_TYPE) 
			{
				int ackIndex = readInt(ackMessage + 1);
				printf("Ack response received! - %d\n", ackIndex);
				
				if(ackIndex == lastAckIndex + 1) 
				{
					lastAckIndex++;
					
					for(i = 0; i < sizeof(ackBuffer); i++) 
					{
						if(ackBuffer[i] == lastAckIndex + 1) 
						{
							lastAckIndex++;
							ackBuffer[i] = 0;
							i = -1;
						}
					}
				} 
				else if(ackIndex > lastAckIndex && ackIndex <= lastAckIndex + 5) 
				{
					for(i = 0; i < sizeof(ackBuffer); i++) 
					{
						if(ackBuffer[i] == 0) 
						{
							ackBuffer[i] = ackIndex;
							break;
						}
					}
				}
			} 
			else 
			{
				printf("Invalid packet.\n");
			}
		} 
		else 
		{
			printf("Socket timed out.\n");
		}
	}
	
	return 0;
}
