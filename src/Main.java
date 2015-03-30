import java.util.ArrayList;




public class Main {

	public static int web_port = 2001;
	
	//We lunch the webserver
	public static void main(String[] args) {
		ArrayList<Email> delayed = new ArrayList<Email>();
		Thread web_server = new ThreadWeb(delayed, web_port);
		Thread email_check = new ThreadEmail(delayed);
		//We lunch the web server
		web_server.start();
		//We lunch the thread that will send the delayed emails
		email_check.start();
	}

}
