import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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
	            String request = input.readLine();
	            
	            //We check the header of the request
	            if (request == null) {
	            	error = true;
	            	writeError(poutput, connection, 400, "Bad Request", "Your http request in incorrect.");
	            //We check the method
	            } else if (!(request.startsWith("GET") || request.startsWith("POST"))){
	            	error = true;
	            	writeError(poutput, connection, 405, "Method Not Allowed", "The server supports only GET and POST methods.");
	            // We check the HTTP version
	            } else if (!(request.endsWith("HTTP/1.0") || request.endsWith("HTTP/1.1"))){
	            	error = true;
	            	writeError(poutput, connection, 505, "HTTP Version not supported", "The server supports only HTTP/1.0 and HTTP/1.1."); 
	            //The GET method for sending a page
	            } else if (request.startsWith("GET")){
	            	//We extract the url asked and manage the root page case
	            	String arr[] = request.split(" ");
	            	String url = arr[1];
	            	if (url.equals("/")){
	            		url = "index.html";
	            	}
	            	System.out.println("The user ask for the page " + url);
	            	//We check if the user is trying to access to a page outside the java server
	            	if (url.indexOf("..")!=-1 || url.indexOf("/.ht")!=-1 || url.endsWith("~")) {
	            		error = true;
	            		writeError(poutput, connection, 403, "Forbidden", "You don't have access to this page.");
	            	} else {
	            		//We delete the "/" in the url
	            		if (url.startsWith("/")){
	            			url = url.substring(1);
	            		}
	            		String filePath = url;
	            		File f = new File(filePath);
	            		//We check if the page exist
	            		if(!(f.exists() && !f.isDirectory())){
	            			System.out.println("Page doesn't exist");
	            			error = true;
	            			writeError(poutput, connection, 404, "Not Found", "This page doesn't exist. Please check the url.");
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
	            } else if (request.startsWith("POST")){

	            	
	            } else {
	            	connection.close();
	            }
	            
	            if (error == true){
        			output.flush();
        	        connection.close();	            	
	            }
	            
	    	} catch (Exception e) {
	            System.out.println("Error: " + e);
	        }
	    }
	    
	}

	private static void writeError(PrintStream poutput, Socket connection, int code_error, String error_title, String error_message){
		poutput.println("HTTP/1.0 " + code_error + " " + error_title);
		poutput.println("Content-Type: text/html; ; charset=utf-8");
		poutput.println("");
		poutput.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
		poutput.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		poutput.println("<head>");
		poutput.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
		poutput.println("<title> Error " + code_error + " - Webmail</title>");
		poutput.println("</head>");
		poutput.println("<body>");
		poutput.println("<h1> Error " + code_error + " - " + error_title + "</h1>");
		poutput.println(error_message);
		poutput.println("</body>");
		poutput.println("</html>");
	}
	
	//To find the right content type for the file extension
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
	
	//To write the file in the outputstream
    private static void sendFile(InputStream file, OutputStream out)
    {
        try {
            byte[] buffer = new byte[1000];
            while (file.available()>0) 
                out.write(buffer, 0, file.read(buffer));
        } catch (IOException e) { System.err.println(e); }
    }	

}
