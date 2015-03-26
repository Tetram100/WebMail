import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer {

	/**
	 * @param args
	 */
	int port = 2037;

	protected void start() {
	    ServerSocket s;

	    System.out.println("The Webserver is starting up on port " + port);
	    System.out.println("(press ctrl-c to exit java program)");
	    try {
	      // create the main server socket
	      s = new ServerSocket(port);
	    } catch (Exception e) {
	      System.out.println("Error: " + e);
	      return;
	    }

	    System.out.println("Waiting for connection");
	    
	    while (true) {
	    	Socket connection = null;
	    	try {
	            //We wait for a connection
	            connection = s.accept();	
	            System.out.println("Connection, sending data.");
	            
	            //We read the input and prepare the output
	            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            OutputStream output = new BufferedOutputStream(connection.getOutputStream());
                PrintStream poutput = new PrintStream(output);
	            
	            //We interpret the input
	            boolean error = false;
	            int code_error;  
	            String request = input.readLine();
	            
	            //We check the header of the request
	            if (request == null) {
	            	error = true;
	            	code_error = 400;
	            } else if (!(request.startsWith("GET") || request.startsWith("POST"))){
	            	error = true;
	            	code_error = 405;
	            } else if (!(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))){
	            	error = true;
	            	code_error = 505;
	            } else if (request.startsWith("GET")){
	            	String arr[] = request.split(" ");
	            	String url = arr[1];
	            	if (url.equals("/")){
	            		url = "index.html";
	            	}
	            	System.out.println("The user ask for the page " + url);
	            	//We check if the user is trying to access to a page outside the java server
	            	if (url.indexOf("..")!=-1 || url.indexOf("/.ht")!=-1 || url.endsWith("~")) {
	            		error = true;
	            		code_error = 403;
	            	} else {
	            		if (url.startsWith("/")){
	            			url = url.substring(1);
	            		}
	            		String filePath = url;
	            		File f = new File(filePath);
	            		//We check if the page exist
	            		if(!(f.exists() && !f.isDirectory())){
	            			System.out.println("Page doesn't exist");
	            			error = true;
	            			code_error = 404;
	            		} else {
	            			System.out.println("sending the page");
	            			//We send the page
	            			InputStream page = new FileInputStream(f);
	            			//We write the HTTP header
	            			poutput.println("HTTP/1.0 200 OK");
	            			String extension = url.replaceAll("^.*\\.([^.]+)$", "$1");
	            			poutput.println("Content-Type: " + contentType(extension) + "; ; charset=utf-8");
	            			//With the blank line we finish the header
	            			poutput.println("");
	            			//The rest of the page
	            			sendFile(page, output); // send raw file
	            			output.flush();
	            	        connection.close();
	            		}
	            	}
	            } 
	            
	    	} catch (Exception e) {
	            System.out.println("Error: " + e);
	        }
	    }
	    
	}

	private String contentType(String extension) {
		if (extension.equals("js")){
			return "application/javascript";
		} else if (extension.equals("css")){
			return "text/css";
		} else if (extension.equals("ico")){
			return "image/jpeg";
		} else {
			return "text/html";
		}
	}
	
    private static void sendFile(InputStream file, OutputStream out)
    {
        try {
            byte[] buffer = new byte[1000];
            while (file.available()>0) 
                out.write(buffer, 0, file.read(buffer));
        } catch (IOException e) { System.err.println(e); }
    }	
	
	public static void main(String[] args) {
	    WebServer ws = new WebServer();
	    ws.start();
	}

}
