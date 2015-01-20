#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv)
{
    int sockfd=socket(AF_INET,SOCK_STREAM,0);
    fd_set sockets;
    FD_ZERO(&sockets);


    struct sockaddr_in serveraddr, clientaddr;
    //client doesn't really care about listening
    //so doesn't need its own address
    serveraddr.sin_family=AF_INET;
    serveraddr.sin_port = htons(9876);
    serveraddr.sin_addr.s_addr=INADDR_ANY;

    bind(sockfd,(struct sockaddr*)&serveraddr,sizeof(serveraddr));
    listen(sockfd,10); 
    //int is size of backlog (connections to allow unhandled)
    FD_SET(sockfd,&sockets);
    int len = sizeof(clientaddr);
    while(1)
    {
        fd_set tmp_set = sockets;
        select(FD_SETSIZE,&tmp_set,NULL,NULL,NULL);
        int i;
        for(i=0; i<FD_SETSIZE; i++)
        {
            if (FD_ISSET(i,&tmp_set))
            {
                if (i==sockfd)
                {
                    printf("A Client connected\n");
                    int clientsocket = accept(sockfd,
                                            (struct sockaddr*)&clientaddr,
                                            &len);
                    FD_SET(clientsocket,&sockets);
                 } else {
                     char line[5000];
                     recv(i,line,5000,0);
                     printf("Got from client: %s\n",line);
                     send(i,line,strlen(line),0);
                     close(i);
                     FD_CLR(i,&sockets);
                 }
            }
        }
    }

    return 0;
}
