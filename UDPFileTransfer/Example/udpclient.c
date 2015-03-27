
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv){

  int sockfd = socket(AF_INET,SOCK_DGRAM,0);
  if(sockfd<0){
    printf("There was an error creating the socket\n");
    return 1;
  }

  struct timeval to;
  to.tv_sec=5;
  to.tv_usec=0;

  struct sockaddr_in serveraddr;
  serveraddr.sin_family=AF_INET;
  serveraddr.sin_port=htons(9876);
  serveraddr.sin_addr.s_addr=inet_addr("127.0.0.1");

  setsockopt(sockfd,SOL_SOCKET,SO_RCVTIMEO,&to,sizeof(to));


  printf("Enter a line: ");
  char line[5000];
  fgets(line,5000,stdin);
  sendto(sockfd,line,strlen(line),0,
          (struct sockaddr*)&serveraddr,sizeof(struct sockaddr_in));
  char line2[5000];
  int len = sizeof(struct sockaddr_in);
  int n = recvfrom(sockfd,line2,5000,0,(struct sockaddr*)&serveraddr,&len);
  if(n<0){
    printf("Sorry, had a problem receiving\n");
    return 1;
  }
  printf("Got from server: %s\n",line2);

  return 0;
}
