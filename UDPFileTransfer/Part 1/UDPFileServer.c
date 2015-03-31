#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/sem.h>
#include <semaphore.h>

#define HEADER_LENGTH 8
#define ACK_MESSAGE_SIZE 4
#define MAX_PACKET_SIZE 1024

typedef struct
{
    int packetNum;
    char* packet;
} packetInfo;

char* convertIntToByteArray(int num);
void* sendPacketToClient(void* args);

sem_t semaphore;

int main (int argc, char **argv)
{
    int sockfd=socket(AF_INET,SOCK_DGRAM,0);

    struct sockaddr_in serveraddr, clientaddr;
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_port = htons(9876);
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    char fileNotFound[] = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    size_t fnfSize = sizeof(fileNotFound);

    bind(sockfd,(struct sockaddr*)&serveraddr,sizeof(serveraddr));

    while(1)
    {
        unsigned int len = sizeof(clientaddr);
        char line[5000];
        printf("Waiting for a file request...");
        recvfrom(sockfd,line,5000,0,(struct sockaddr*)&clientaddr,&len);
        printf("File requested: %s\n",line);
        
        FILE *fp;
        fp = fopen( line, "r");

        if (fp == NULL)
        {
            printf("File Not Found");
            sendto(sockfd,fileNotFound,fnfSize,0,(struct sockaddr*)&clientaddr,sizeof(clientaddr));
            continue;
        }
        else
        {
            fseek(fp, 0L, SEEK_END);
            long byteSize = ftell(fp);
            long size;
            size = HEADER_LENGTH + byteSize;
            fseek(fp, 0L, SEEK_SET);
            if(size < MAX_PACKET_SIZE)
            {
                char fileBytes[(size + 8)];
                fileBytes[3] = 0x01;
                fileBytes[7] = 0x01;
                int result = fputs(fileBytes, fp);
                if(result == EOF)
                {
                    printf("Error in writing file to Array!\n");
                    exit(1);
                }
                size_t fileSize = sizeof(fileBytes);
                sendto(sockfd, fileBytes, fileSize, 0, (struct sockaddr*) &clientaddr, sizeof(clientaddr));
                char ackMessage[ACK_MESSAGE_SIZE];
                result = recvfrom(sockfd, ackMessage, ACK_MESSAGE_SIZE, 0, (struct sockaddr*) &clientaddr, &len);
                if(result > 0)
                {
                    printf("Ack response received!\n");
                }
            }
            else
            {
                int numPackets = (int) ceil(byteSize / MAX_PACKET_SIZE);
                char packets[numPackets][MAX_PACKET_SIZE];
                int i;
                pthread_t threads[numPackets];
                for(i = 0; i < numPackets; i++)
                {
                    int numBytesToRead = MAX_PACKET_SIZE - HEADER_LENGTH;
                    char* packetNum = convertIntToByteArray(i);
                    char* numPacketsByteArray = convertIntToByteArray(numPackets);
                    packets[i][0] = numPacketsByteArray[0];
                    packets[i][1] = numPacketsByteArray[1];
                    packets[i][2] = numPacketsByteArray[2];
                    packets[i][3] = numPacketsByteArray[3];
                    packets[i][4] = packetNum[0];
                    packets[i][5] = packetNum[1];
                    packets[i][6] = packetNum[2];
                    packets[i][7] = packetNum[3];
                    fread((void *) &packets[i][8], 1, numBytesToRead, fp);
                    free(packetNum);
                    free(numPacketsByteArray);
                }
                sem_init(&semaphore, 0, 5);
                for(i = 0; i < numPackets; i++)
                {
                    packetInfo pInfo;
                    pInfo.packetNum = i;
                    pInfo.packet = packets[i];
                    pthread_create(&threads[i], NULL, sendPacketToClient, &pInfo);
                }
            }
        }
        
        //here have more code
        //Split file up into packet-sized chunks
        //Send file via sliding window
    }

    return 0;
}

char* convertIntToByteArray(int num)
{
    char* bytes = malloc(sizeof(char) * 4);
    bytes[0] = (num >> 24) & 0xFF;
    bytes[1] = (num >> 16) & 0xFF;
    bytes[2] = (num >> 8) & 0xFF;
    bytes[3] = num & 0xFF;
    return bytes;
}

void* sendPacketToClient(void* args)
{
    sem_wait(&semaphore);
    // Put code for send and wait for acknowledgement here
    sem_post(&semaphore);
    return 0;
}