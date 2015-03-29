import java.util.ArrayList;

public class ThreadWeb extends Thread {

	public ArrayList<Email> delayed = null;
	public ThreadWeb(ArrayList<Email> delayed) {
		this.delayed = delayed;
	}

	public void run() {
		int port = 2001;
		WebServer ws = new WebServer(port, this.delayed);
		ws.start();
	}

}
