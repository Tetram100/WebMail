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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class WebServer {

	int port;
	public ArrayList<Email> delayed = null;

	public WebServer(int port, ArrayList<Email> delayed) {
		this.port = port;
		this.delayed = delayed;
	}

	protected void start() {
		String www = "Web/";
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
					} else if (url.equals("/admin")){
						sendAdmin(poutput);
						output.flush();
						connection.close();	
					} else {
						//We delete the "/" in the url
						if (url.startsWith("/")){
							url = url.substring(1);
						}
						url = www + url;
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
							poutput.println("HTTP/1.1 200 OK");
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
					//We find the url
					String head[] = request.split(" ");
					String url = head[1];
					if (url.startsWith("/")){
						url = url.substring(1);
					}
					System.out.println("The user post on the page " + url);

					//We download the post body with the exact number of byte from the Content-Length field
					String temp;
					int contentLength = 0;
					while (!(temp = input.readLine()).equals("")){
						if (temp.startsWith("Content-Length:")){
							String arr[] = temp.split(" ");
							contentLength = Integer.parseInt(arr[1]);
						}
					}
					StringBuilder requestContent = new StringBuilder();
					for (int i = 0; i < contentLength; i++)
					{
						requestContent.append((char) input.read());
					}
					String body = requestContent.toString();
					String request_cut[] = body.split("&");

					//The case it's for sending an email
					if (url.equals("send_email")){
						boolean false_form = false;
						boolean now = true;

						//Default values for fields
						String from = "default@kth.se";
						String to = "default@kth.se";
						Date sending_time = new Date();
						String server = "";
						String subject = "Message from test Webmail";
						String message = "This is a default message.";

						//For each field we decode the post request and change the default value if it's not empty
						for(String param : request_cut){
							if (param.startsWith("from")){
								String block_from[] = param.split("=",2);
								if (block_from.length == 2 && (!block_from[1].equals(""))){
									from = java.net.URLDecoder.decode(block_from[1], "UTF-8");
								} else {
									false_form = true;
								}
							}
							if (param.startsWith("to")){
								String block_to[] = param.split("=",2);
								if (block_to.length == 2 && (!block_to[1].equals(""))){
									to = java.net.URLDecoder.decode(block_to[1], "UTF-8");
								} else {
									false_form = true;
								}
							}
							if (param.startsWith("sending_time")){
								String block_date[] = param.split("=",2);
								if (block_date.length == 2 && (!block_date[1].equals(""))){
									now = false;
									String time_string = java.net.URLDecoder.decode(block_date[1], "UTF-8");
									//We transform the string into a date object
									DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
									sending_time = formatter.parse(time_string);
									if (sending_time.before(new Date())){
										now = true;
									}
								}
							}							
							if (param.startsWith("server")){
								String block_server[] = param.split("=",2);
								if (block_server.length == 2  && (!block_server[1].equals(""))){
									server = java.net.URLDecoder.decode(block_server[1], "UTF-8");
								}
							}
							if (param.startsWith("subject")){
								String block_subject[] = param.split("=",2);
								if (block_subject.length == 2 && (!block_subject[1].equals(""))){
									subject = java.net.URLDecoder.decode(block_subject[1], "UTF-8");
								} else {
									false_form = true;
								}
							}
							if (param.startsWith("message")){
								String block_message[] = param.split("=",2);
								if (block_message.length == 2 && (!block_message[1].equals(""))){
									message = java.net.URLDecoder.decode(block_message[1], "UTF-8");
								} else {
									false_form = true;
								}
							}
						}
						//If the user didn't fill in every field
						if (false_form == true){
							error = true;
							writeError(poutput, connection, 400, "Bad request", "Please fill in the field in the form.");
						} else {
							//We send the email
							Email new_message = new Email(from, to, sending_time, server, subject, message);
							System.out.println("We send a new message:");
							System.out.println("The message will be sent at :" + sending_time);
							System.out.println("From: " + from + ", To: " + to + ", server: " + server);
							System.out.println("Subject: " + subject);
							System.out.println("Message: " + message);

							//If the message has to be sent now
							if (now ==true){
								String response = new_message.sendEmail();
								System.out.println("Response of the sending :" + response);
								//We send the response
								writeSendingReponse(poutput, response);
							} else {
								//We add the message in the delayed emails table
								synchronized( this.delayed ) {
									this.delayed.add(new_message);
									System.out.println("Sending planned");
									writeReportedEmail(poutput, sending_time);
								}
							}
							output.flush();
							connection.close();	
						}
					} else {
						System.out.println("Page doesn't exist");
						error = true;
						writeError(poutput, connection, 404, "Not Found", "This page doesn't exist. Please check the url.");           		
					}
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
		poutput.println("HTTP/1.1 " + code_error + " " + error_title);
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

	private static void writeSendingReponse(PrintStream poutput, String response){
		poutput.println("HTTP/1.1 200 OK");
		poutput.println("Content-Type: text/html; ; charset=utf-8");
		poutput.println("");
		poutput.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
		poutput.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		poutput.println("<head>");
		poutput.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
		poutput.println("<link rel='icon' type='image/x-icon' href='favicon.ico' />");
		poutput.println("<script src='jquery-1.11.2.js'></script>");
		poutput.println("<link href='bootstrap.min.css' rel='stylesheet'>");
		poutput.println("<script src='bootstrap.min.js'></script>");
		poutput.println("<title> Webmail</title>");
		poutput.println("</head>");
		poutput.println("<body>");
		poutput.println("<div class = 'container well'>");
		if (response.equals("OK")){
			poutput.println("<div class='alert alert-success'>");
			poutput.println("The email has been successfully sent.");
		} else {
			poutput.println("<div class='alert alert-danger'>");
			poutput.println("ERROR : " + response);
		}
		poutput.println("</div>");
		poutput.println("</div>");
		poutput.println("</body>");
		poutput.println("</html>");	
	}

	private void sendAdmin(PrintStream poutput){
		poutput.println("HTTP/1.1 200 OK");
		poutput.println("Content-Type: text/html; ; charset=utf-8");
		poutput.println("");
		poutput.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
		poutput.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		poutput.println("<head>");
		poutput.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
		poutput.println("<link rel='icon' type='image/x-icon' href='favicon.ico' />");
		poutput.println("<script src='jquery-1.11.2.js'></script>");
		poutput.println("<link href='bootstrap.min.css' rel='stylesheet'>");
		poutput.println("<script src='bootstrap.min.js'></script>");
		poutput.println("<title> Webmail</title>");
		poutput.println("</head>");
		poutput.println("<body>");
		poutput.println("<div class = 'container well'>");
		poutput.println("<h1>Admin page</h1>");
		poutput.println("<hr>");
		poutput.println("<table class='table table-hover'>");
		poutput.println("<thead>");
		poutput.println("<tr>");
		poutput.println("<th>From</th>");
		poutput.println("<th>To</th>");
		poutput.println("<th>Subject</th>");
		poutput.println("<th>Submit time</th>");
		poutput.println("<th>Sending time</th>");
		poutput.println("</tr>");
		poutput.println("</thead>");
		poutput.println("<tbody>");
		for (Email email : this.delayed) {
			poutput.println("<tr>");
			poutput.println("<td>"+email.from+"</td>");
			poutput.println("<td>"+email.to+"</td>");
			poutput.println("<td>"+email.subject+"</td>");
			poutput.println("<td>"+email.creation_time+"</td>");
			poutput.println("<td>"+email.sending_time+"</td>");
			poutput.println("</tr>");
		}
		poutput.println("</tbody>");
		poutput.println("</table>");
		poutput.println("</div>");
		poutput.println("</body>");
		poutput.println("</html>");	
	}


	private static void writeReportedEmail(PrintStream poutput, Date sending_time){
		poutput.println("HTTP/1.1 200 OK");
		poutput.println("Content-Type: text/html; ; charset=utf-8");
		poutput.println("");
		poutput.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
		poutput.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
		poutput.println("<head>");
		poutput.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
		poutput.println("<link rel='icon' type='image/x-icon' href='favicon.ico' />");
		poutput.println("<script src='jquery-1.11.2.js'></script>");
		poutput.println("<link href='bootstrap.min.css' rel='stylesheet'>");
		poutput.println("<script src='bootstrap.min.js'></script>");
		poutput.println("<title> Webmail</title>");
		poutput.println("</head>");
		poutput.println("<body>");
		poutput.println("<div class = 'container well'>");
		poutput.println("<div class='alert alert-success'>");
		poutput.println("The email has been transmited to the server and will be sent at: " + sending_time);
		poutput.println("</div>");
		poutput.println("</div>");
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
