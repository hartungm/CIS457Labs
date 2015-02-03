import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

class HTTPServer {
    public static void main(String[] args) throws Exception {
        String docRoot = null;
        int port = -1;
        String logPath = null;
        boolean errors = false;

        if (args.length == 0 || (args.length >= 2 && args.length <= 6 && args.length % 2 == 0)) {
            try {
                for (int i=0; i<args.length-1; i+=2) {
                    switch(args[i]) {
                        case "-p": 
                            port = Integer.parseInt(args[i+1]);
                            if (port <= 0) {
                                throw new NumberFormatException();
                            }
                            break;
                        case "-docroot": 
                            docRoot = args[i+1];
                            break;
                        case "-logfile": 
                            logPath = args[i+1];
                            break;
                        default: 
                            System.out.println("Invalid Argument: " + args[i]);
                            errors = true;
                            break;
                    }
                }
                if (port == -1) {
                    port = 8080;
                }
                if (!errors) {
                    ServerSocket listenSocket = new ServerSocket(port);
                    
                    while(true) {
                        Socket s = listenSocket.accept();
                        Runnable r = new ClientHandler(s, docRoot, logPath);
                        Thread t = new Thread(r);
                        t.start();
                    }
                }
            }
            catch (NumberFormatException n) {
                System.out.println("Invalid Port Number!");
            }
            catch (BindException b) {
                System.out.println("Port Unavailable!");
            }
        } else {
            System.out.println("Invalid Number of Arguments!");
        }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    String docRoot;
    String logPath;
    boolean logging;
    BufferedWriter logFile;
    
    ClientHandler(Socket connection, String docRoot, String logPath) {
        this.connectionSocket = connection;

        this.docRoot = docRoot;
        if (this.docRoot == null) {
            this.docRoot = "./";
        }

        this.logPath = logPath;
        this.logging = this.logPath != null;
        if (logging) {
            try {
                this.logFile = new BufferedWriter(new FileWriter(logPath));
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void run() {
    	
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

            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            
            byte[] requestBytes = new byte[5000];
            reader.read(requestBytes, 0, 5000);
            
            String request = new String(requestBytes);
            String[] requestLines = request.split("\r\n");
            
            System.out.println(request);
            if (logging) {
                logFile.write(request);
                logFile.flush();
            }
            
            String[] getLine;
            String filePath = "";

            if(requestLines[0].contains("GET") && requestLines[0].contains("HTTP/1.1")) {
                getLine = requestLines[0].split(" ");
                filePath = docRoot + getLine[1];
            } else {
                // Errors for non-implemented response headers
            	String clientOutput = "The functionality you are trying to use is not implemented by this server. (These are not the droids you're looking for)";
                String response =   "HTTP/1.1 501 Not Implemented\r\n" +
                                    "Date: " + df.format(date) + "\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + clientOutput.length() + "\r\n" +
                                    "Connection: keep-alive\r\n\r\n";
                System.out.println(response);
                if (logging) {
                    logFile.write(response);
                    logFile.flush();
                }
                output.writeBytes(response);
                output.writeBytes(clientOutput);
                output.flush();
                return;
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

            // Access Denied to directory
            if(securityCounter < 0) {
                String clientOutput = "Security Issue detected, access denied";
                System.out.println(clientOutput);
                String response =   "HTTP/1.1 403 Forbidden\r\n" +
                                    "Date: " + df.format(date) + "\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + clientOutput.length() + "\r\n" +
                                    "Connection: close\r\n\r\n";
                System.out.println(response);
                if (logging) {
                    logFile.write(response);
                    logFile.flush();
                }
                output.writeBytes(response);
                output.writeBytes(clientOutput);
                output.flush();
                connectionSocket.close();
                return;
            }
            
            File requestedFile = new File(filePath);
            
            if(requestedFile.exists() && !requestedFile.isDirectory()) {
                String fileType = filePath.substring(filePath.lastIndexOf("."), filePath.length());
                
                String contentType = contentTypes.get(fileType.toLowerCase());
                if(contentType == null) {
                	// Unsupported File Type
                }
                
                Date fileModDate = new Date(requestedFile.lastModified());

                for (i=0; i<requestLines.length; i++) {
                    if(requestLines[i].contains("If-Modified-Since:")) {

                        String dateString = requestLines[i].substring(19);
                        Date requestModDate = df.parse(dateString);

                        if (!fileModDate.after(requestModDate)) {
                            String response = "HTTP/1.1 304 Not Modified\r\n"
                                            + "Date: " + df.format(date) + "\r\n"
                                            + "Connection: keep-alive\r\n\r\n";

                                System.out.println(response);
                                if (logging) {
                                    logFile.write(response);
                                    logFile.flush();
                                }
                                output.writeBytes(response);
                                output.flush();
                                return;
                        } else {
                            break;
                        }
                    }
                }

                String response = "HTTP/1.1 200 OK\r\n"
                				+ "Date: " + df.format(date) + "\r\n"
                				+ "Last-Modified: " + df.format(fileModDate) + "\r\n" 
                				+ "Content-Type: " + contentType + "\r\n" 
                				+ "Content-Length: " + requestedFile.length() + "\r\n"
                                + "Connection: keep-alive\r\n\r\n";
                
                System.out.println(response);
                if (logging) {
                    logFile.write(response);
                    logFile.flush();
                }
                output.writeBytes(response);
                
                byte[] fileData = new byte[(int) requestedFile.length()];
                FileInputStream fileInput = new FileInputStream(requestedFile);
                fileInput.read(fileData);
                fileInput.close();
                
                output.write(fileData);
                output.flush();
            }
            else {

                // File not Found Errors
                String clientOutput = "404 File not Found!";
                String response =   "HTTP/1.1 404 Not Found\r\n" +
                                    "Date: " + df.format(date) + "\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: " + clientOutput.length() + "\r\n" +
                                    "Connection: keep-alive\r\n\r\n";
                System.out.println(response);
                if (logging) {
                    logFile.write(response);
                    logFile.flush();
                }
                output.writeBytes(response);
                output.writeBytes(clientOutput);
                output.flush();
            }
        } 
        catch(IOException e) {}
        catch (Exception e) {}
    }
}
