#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv)
{
    int sockfd = socket(AF_INET,SOCK_STREAM,0); //file descriptor
    //AF: address family (internet)
    //Sock Stream: stream transport (TCP)
    //0: options for socket (none)
    if(sockfd<0)
    {
        printf("There was an error creating the socket\n");
        return 1;
    }

    struct sockaddr_in serveraddr;
    serveraddr.sin_family=AF_INET;
    serveraddr.sin_port=htons(9876);
    //port number: what program to give data to on receiving side
    //16 bit number...should be above 1024
    serveraddr.sin_addr.s_addr=inet_addr("127.0.0.1");

    int e = connect(sockfd,(struct sockaddr*)&serveraddr,
            sizeof(struct sockaddr));
    if(e<0)
    {
        printf("There was an error with connecting\n");
        return 1;
    }

    printf("Enter a line: ");
    char line[5000];
    fgets(line,5000,stdin);
    send(sockfd,line,strlen(line),0);
    //strlen is ONLY FOR STRINGS. not binary data, etc.
    char line2[5000];
    int n = recv(sockfd,line2,5000,0);
    
    if(n<0)
    {
        printf("There was an error recieving\n");
        //most common, connection closed on other end
        return 1;
    }

    printf("Got from server: %s\n", line2);

    return 0;
}
