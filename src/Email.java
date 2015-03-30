import java.net.Socket;
import java.util.Date;
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
	Date creation_time;
	Date sending_time;
	String server;
	String subject;
	String message;
	
	//Builder
	public Email (String from, String to, Date sending_time, String server, String subject, String message){
		this.from = from;
		this.to = to;
		this.creation_time = new Date();
		this.sending_time = sending_time;
		this.server = server;
		this.subject = subject;
		this.message = message;
	}
	
	//Method for sending an email
	public String sendEmail(){
		int smtp_port = 1025;
		String default_server = "mail.ik2213.lab";

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
            PrintStream poutput = new PrintStream(output, true, "ISO-8859-15");
            //We send the message to the SMTP server
			poutput.println("HELO client");
			poutput.println("MAIL FROM: <" + this.from + ">");
			poutput.println("RCPT TO: <" + this.to + ">");
			poutput.println("DATA");
			poutput.println("From: " + this.from);
			poutput.println("To: " + this.to);
			String encode_subject = "Subject: =?ISO-8859-15?B?" + base64encode(this.subject) + "?=";
			poutput.println(encode_subject);
			poutput.println("Content-Type: text/plain; charset='ISO-8859-15'");
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
		      error_message = "Error : " + e + "<br><br> The message has not been sent. An error occured with the SMTP server. Please check that the server is up and works.";
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
	
	//Return the message encoded in Base64 and ready to be sent
	private String encode(String message){
		String base64_message = base64encode(message);
		return base64_message;
	}
	
	//Return the string in base64 following the base64 algorithm
	private String base64encode(String message){
		String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		String result = "";
		String padding = "";
		int remainder = message.length() % 3;
	 
		// add a right zero pad to make this string a multiple of 3 characters
		if (remainder > 0) {
		    for (; remainder < 3; remainder++) {
			padding += "=";
			message += "\0";
		    }
		}
	 
		// It takes the original binary data and operates on it by dividing it into tokens of three bytes
		for (remainder = 0; remainder < message.length(); remainder += 3) {
	 
		    // Newline characters are inserted to avoid exceeding any mail server's line length limit
		    if (remainder > 0 && (remainder / 3 * 4) % 76 == 0)
			result += "\r\n";
	 
		    // these three 8-bit (ASCII) characters become one 24-bit number
		    int n = (message.charAt(remainder) << 16) + (message.charAt(remainder + 1) << 8) + (message.charAt(remainder + 2));
	 
		    // We split the three bytes of n (24bits) into four numbers of six bits
		    int n1 = (n >> 18) & 63;
		    int n2 = (n >> 12) & 63;
		    int n3 = (n >> 6) & 63;
		    int n4 = n & 63;
	 
		    // those four 6-bit numbers are used as indices into the 64 ASCII characters table
		    result += "" + base64chars.charAt(n1) + base64chars.charAt(n2) + base64chars.charAt(n3) + base64chars.charAt(n4);
		}
		//We add the "=" of p for padding after removing the p last characters
		return result.substring(0, result.length() - padding.length()) + padding;
	}
	
	//We get the MX record from the client address
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
