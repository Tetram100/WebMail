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
		int smtp_port = 25;
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
			System.out.println("The SMTP server uses is: " + smtp_server);
			//We open the connection
			smtpSocket = new Socket(smtp_server, smtp_port);
            BufferedReader input = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
            OutputStream output = new BufferedOutputStream(smtpSocket.getOutputStream());
            PrintStream poutput = new PrintStream(output);
            
            //We send the message to the SMTP server
            System.out.println(input.readLine());
			poutput.println("HELO client");
			output.flush();
			poutput.println("MAIL FROM: <" + this.from + ">");
			output.flush();
			System.out.println(this.to);
			poutput.println("RCPT TO: <" + this.to + ">");
			output.flush();
			poutput.println("DATA");
			output.flush();
			poutput.println("From: " + this.from);
			poutput.println("To: " + this.to);
			poutput.println("Subject: " + this.subject);
			poutput.println("");
			poutput.println(this.message);
			poutput.println(".");
			output.flush();
			poutput.println("QUIT");
			output.flush();
			//We look at the server response
			String temp;
			while (!((temp = input.readLine()) == null)){
				System.out.println(temp);
				//We look if it's OK or we had a problem
				if(temp.indexOf("Ok: queued") != -1){
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
		String return_message = "ERROR " + code_error + ": " + error_message + "\r\n The message has not been sent. An error occured with the SMTP server. Please check that the server is up and works.";
		return return_message;
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
