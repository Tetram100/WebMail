import java.util.ArrayList;
import java.util.Date;


public class ThreadEmail extends Thread {

	public ArrayList<Email> delayed = null;
	public ThreadEmail(ArrayList<Email> delayed) {
		this.delayed = delayed;
	}

	public void run() {
		while(true){
			ArrayList<Email> sent = new ArrayList<Email>();
			for (Email email : this.delayed) {
				Date now = new Date();
				if (email.sending_time.before(now)){
					String response = email.sendEmail();
					System.out.println("Response of the delayed sending: " + response);
					Email notification = new Email("noreply@webmail.se", email.from, now, "127.0.0.1", 
							"Your message has successfully been sent", "Your message to " + email.to + " on our webmail has been sent at " + email.sending_time + 
							". ");
					String response_notify = notification.sendEmail();
					System.out.println("Response of the sending notification: " + response_notify);
					sent.add(email);
				}
			}
			for (Email email : sent) {
				this.delayed.remove(email);
			}
		}
	}

}
