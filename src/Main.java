import java.util.ArrayList;




public class Main {

	//We lunch the webserver
	public static void main(String[] args) {
		ArrayList<Email> delayed = new ArrayList<Email>();
		Thread web_server = new ThreadWeb(delayed);
		Thread email_check = new ThreadEmail(delayed);
		web_server.start();
		email_check.start();
	}

}
