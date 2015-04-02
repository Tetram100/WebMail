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
			synchronized( this.delayed ) {
				//We check every message to see if the sending time is exceeded
				for (Email email : this.delayed) {
					Date now = new Date();
					if (email.sending_time.before(now)){
						//We send the email
						String response = email.sendEmail();
						System.out.println("Response of the delayed sending: " + response);
						//We send the notification email to the sender
						Email notification = new Email("noreply@webmail.se", email.from, now, "", 
								"Your message has successfully been sent", "Your message to " + email.to + " on our webmail has been sent at " + email.sending_time + 
								". ");
						String response_notify = notification.sendEmail();
						System.out.println("Response of the sending notification: " + response_notify);
						//We add the email to the sent table because we can delete it in this loop
						sent.add(email);
					}
				}
			}
			//We remove the messages sent
			for (Email email : sent) {
				synchronized( this.delayed ) {
					this.delayed.remove(email);
				}
			}
		}
	}

}
