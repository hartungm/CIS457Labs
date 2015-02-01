import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

class HTTPServer {
    public static void main(String argv[]) throws Exception {
        ServerSocket listenSocket = new ServerSocket(8080);
        
        while(true) {
            Socket s = listenSocket.accept();
            Runnable r = new ClientHandler(s);
            Thread t = new Thread(r);
            t.start();
        }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    
    ClientHandler(Socket connection) {
        this.connectionSocket = connection;
    }
    
    public void run() {
    	final String ROOT = "./";
    	
    	Map<String, String> contentTypes = new HashMap<String, String>();
    	contentTypes.put(".html", "text/html");
    	contentTypes.put(".txt", "text/plain");
        contentTypes.put(".css", "text/css");
    	contentTypes.put(".jpg", "image/jpeg");
    	contentTypes.put(".jpeg", "image/jpeg");
    	contentTypes.put(".pdf", "application/pdf");
    	
        try {
            BufferedInputStream reader = new BufferedInputStream(connectionSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
            
            byte[] requestBytes = new byte[5000];
            reader.read(requestBytes, 0, 5000);
            
            String request = new String(requestBytes);
            String[] requestLines = request.split("\r\n");
            
            System.out.println(request);
            
            String[] getLine;
            String filePath = "";
            if(requestLines[0].contains("GET") && requestLines[0].contains("HTTP/1.1")) {
                getLine = requestLines[0].split(" ");
                filePath = ROOT + getLine[1];
            } else {
            	 String clientOutput = "The functionality you are trying to use is not implemented by this server. (These are not the droids you're looking for)";
                String response =   "HTTP/1.1 501 Not Implemented\r\n" +
                                    "Date: " + df.format(date) + "\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + clientOutput.length() + "\r\n" +
                                    "Connection: close\r\n\r\n";
                System.out.println(response);
                output.writeBytes(response);
                output.writeBytes(clientOutput);
                output.flush();
                break;
            }

            String[] filePathArr = filePath.split("/");
            int securityCounter = 0;
            int i = 0;
            while(i < filePathArr.length && securityCounter >= 0)
            {
                if(filePathArr[i].equals(".."))
                {
                    securityCounter--;
                }
                else {
                    securityCounter++;
                }
                i++;
            }
            if(securityCounter < 0) {
                System.out.println("Security Issue detected, access denied");
                //Send Access Denied Response back to html
            }
            
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            
            File requestedFile = new File(filePath);
            
            if(requestedFile.exists()) {
                String fileType = filePath.substring(filePath.lastIndexOf("."), filePath.length());
                
                String contentType = contentTypes.get(fileType.toLowerCase());
                if(contentType == null) {
                	// Unsupported File Type
                }
                
                String response = "HTTP/1.1 200 OK\r\n"
                				+ "Date: " + df.format(date) + "\r\n"
                				+ "Last-Modified: " + df.format(new Date(requestedFile.lastModified())) + "\r\n" 
                				+ "Content-Type: " + contentType + "\r\n" 
                				+ "Content-Length: " + requestedFile.length() + "\r\n\r\n";
                
                System.out.println(response);
                output.writeBytes(response);
                
                byte[] fileData = new byte[(int) requestedFile.length()];
                FileInputStream fileInput = new FileInputStream(requestedFile);
                fileInput.read(fileData);
                fileInput.close();
                
                output.write(fileData);
                output.flush();
            }
            else {
                String clientOutput = "404 File not Found!";
                String response =   "HTTP/1.1 404 Not Found\r\n" +
                                    "Date: " + df.format(date) + "\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + clientOutput.length() + "\r\n" +
                                    "Connection: close\r\n\r\n";
                System.out.println(response);
                output.writeBytes(response);
                output.writeBytes(clientOutput);
                output.flush();
            }
            connectionSocket.close();
        } 
        catch(IOException e) {
        	e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
