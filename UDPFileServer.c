#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>
#define HEADER_LENGTH 8

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
        int len = sizeof(clientaddr);
        char line[5000];
        printf("Waiting for a file request...");
        recvfrom(sockfd,line,5000,0,(struct sockaddr*)&clientaddr,&len);
        printf("File requested: %s\n",line);
        
        FILE *fp;
        fp = fopen("./" + line, "r");

        if (fp == NULL)
        {
            printf("File Not Found");
            sendto(sockfd,fileNotFound,fnfSize,0,(struct sockaddr*)&clientaddr,sizeof(clientaddr));
            continue;
        }
        
        //here have more code
        //Split file up into packet-sized chunks
        //Send file via sliding window
    }

    return 0;
}
