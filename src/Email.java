
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
		return "Not implemented yet.";
	}
	
}
