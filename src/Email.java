import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class Email {

	String from;
	String to;
	String server;
	String subject;
	String message;
	
	//Builder
	public Email (String from, String to, String server, String subject, String message){
		this.from = from;
		this.to = to;
		this.server = server;
		this.subject = subject;
		this.message = message;
	}
	
	//Method for sending an email
	public String sendEmail(){
		int smtp_port = 1025;
		String default_server = "127.0.0.1";

		boolean error = true;
		String error_message = "";
		String smtp_server;
		
		Socket smtpSocket;
		try {
			if (this.server.equals("")){
				smtp_server = getMxRecord(this.to, default_server);
			} else {
				smtp_server = this.server;
			}
			System.out.println("The SMTP server used is: " + smtp_server);
			//We open the connection
			smtpSocket = new Socket(smtp_server, smtp_port);
            BufferedReader input = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
            OutputStream output = new BufferedOutputStream(smtpSocket.getOutputStream());
            PrintStream poutput = new PrintStream(output, true, "UTF-8");
            //We send the message to the SMTP server
			poutput.println("HELO client");
			poutput.println("MAIL FROM: <" + this.from + ">");
			poutput.println("RCPT TO: <" + this.to + ">");
			poutput.println("DATA");
			poutput.println("From: " + this.from);
			poutput.println("To: " + this.to);
			poutput.println("Subject: " + this.subject);
			poutput.println("Content-Type: text/plain; charset='ISO-8859-1'");
			poutput.println("Content-Transfer-Encoding: Base64");
			poutput.println("MIME-Version: 1.0");
			poutput.println("");
			poutput.println(encode(this.message));
			poutput.println(".");
			poutput.println("QUIT");
			//We look at the server response
			String temp;
			while (!((temp = input.readLine()) == null)){
				System.out.println(temp);
				//We look if it's OK or we had a problem
				if(temp.indexOf("221 Ok") != -1){
					error = false;
				} else if (temp.indexOf("421") != -1){
					error_message = writeError(421, "Service not available.");
				} else if (temp.indexOf("452") != -1){
					error_message = writeError(452, "Requested action not taken: insufficient system storage.");
				} else if (temp.indexOf("550") != -1){
					error_message = writeError(550, "Requested action not taken: mailbox unavailable.");
				} else if (temp.indexOf("554") != -1){
					error_message = writeError(421, "Transaction failed.");
				} else if (temp.indexOf("451") != -1){
					error_message = writeError(421, "Requested action aborted: local error in processing.");
				}
        	}
			smtpSocket.close();
		} catch (Exception e) {
		      System.out.println("Error: " + e);
		      error = true;
		      error_message = "Error : " + e + "\r\n The message has not been sent. An error occured with the SMTP server. Please check that the server is up and works.";
		}
		
		if (error == true){
			return error_message;
		} else{
			return "OK";
		}
	}
	
	//To write the error message
	private String writeError(int code_error, String error_message){
		String return_message = "ERROR " + code_error + ": " + error_message + "<br><br> The message has not been sent. An error occured with the SMTP server. Please check that the server is up and works.";
		return return_message;
	}
	
	//Return the message encoded in ISO-8859-1 Base64 and ready to be sent
	private String encode(String message){
		String iso_message = isoencode(message);
		String base64_message = base64encode(iso_message);
		return base64_message;
	}
	
	//Return the string in ISO-8859-1
	private String isoencode( String message){
		String iso_message = "";
		try{
			iso_message = new String(message.getBytes("ISO-8859-1"), "ISO-8859-1");
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
		return iso_message;
	}
	
	//Return the string in base64
	private String base64encode(String message){
		String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		String r = "", p = "";
		int c = message.length() % 3;
	 
		// add a right zero pad to make this string a multiple of 3 characters
		if (c > 0) {
		    for (; c < 3; c++) {
			p += "=";
			message += "\0";
		    }
		}
	 
		// increment over the length of the string, three characters at a time
		for (c = 0; c < message.length(); c += 3) {
	 
		    // we add newlines after every 76 output characters, according to
		    // the MIME specs
		    if (c > 0 && (c / 3 * 4) % 76 == 0)
			r += "\r\n";
	 
		    // these three 8-bit (ASCII) characters become one 24-bit number
		    int n = (message.charAt(c) << 16) + (message.charAt(c + 1) << 8) + (message.charAt(c + 2));
	 
		    // this 24-bit number gets separated into four 6-bit numbers
		    int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63, n3 = (n >> 6) & 63, n4 = n & 63;
	 
		    // those four 6-bit numbers are used as indices into the base64
		    // character list
		    r += "" + base64chars.charAt(n1) + base64chars.charAt(n2) + base64chars.charAt(n3) + base64chars.charAt(n4);
		}
		return r.substring(0, r.length() - p.length()) + p;
	}
	
	private String getMxRecord(String email, String default_server){
		try{
			String email_split[] = email.split("@");
			String domain = email_split[email_split.length-1];
			Lookup lookup = new Lookup(domain, Type.MX);
			Record[] records = lookup.run();
			MXRecord mx = (MXRecord) records[0];
			return ""+ mx.getTarget();
		} catch(Exception e){
	        System.out.println("Error: "+e.getMessage());
	        return default_server;
	    }
	}
}
