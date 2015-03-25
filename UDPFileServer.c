#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define HEADER_LENGTH 2
#define ACK_MESSAGE_SIZE 4

int main (int argc, char **argv)
{
    int sockfd=socket(AF_INET,SOCK_DGRAM,0);

    struct sockaddr_in serveraddr, clientaddr;
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_port = htons(9876);
    serveraddr.sin_addr.s_addr = INADDR_ANY;
    char fileNotFound[] = { 0x00, 0x00 };
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
        fp = fopen( strcat("./", line), "r");

        if (fp == NULL)
        {
            printf("File Not Found");
            sendto(sockfd,fileNotFound,fnfSize,0,(struct sockaddr*)&clientaddr,sizeof(clientaddr));
            continue;
        }
        else
        {
            fseek(fp, 0L, SEEK_END);
            long size = ftell(fp);
            size += HEADER_LENGTH;
            fseek(fp, 0L, SEEK_SET);
            if(size < 1024)
            {
                char fileBytes[(size + 2)];
                fileBytes[0] = 0x01;
                fileBytes[1] = 0x01;
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

            }
        }
        
        //here have more code
        //Split file up into packet-sized chunks
        //Send file via sliding window
    }

    return 0;
}
