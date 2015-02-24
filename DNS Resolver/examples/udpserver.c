#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv){
  int sockfd=socket(AF_INET,SOCK_DGRAM,0);

  struct sockaddr_in serveraddr, clientaddr;
  serveraddr.sin_family=AF_INET;
  serveraddr.sin_port = htons(9876);
  serveraddr.sin_addr.s_addr=INADDR_ANY;

  bind(sockfd,(struct sockaddr*)&serveraddr,sizeof(serveraddr));

  while(1){
    int len=sizeof(clientaddr);
    char line[5000];
    recvfrom(sockfd,line,5000,0,(struct sockaddr*)&clientaddr,&len);
    printf("Got from client: %s\n",line);
    sendto(sockfd,line,strlen(line),0,
            (struct sockaddr*)&clientaddr,sizeof(clientaddr));
  }

  return 0;
}