import java.util.ArrayList;

public class ThreadWeb extends Thread {

	public ArrayList<Email> delayed = null;
	public int port;
	public ThreadWeb(ArrayList<Email> delayed, int port) {
		this.delayed = delayed;
		this.port = port;
	}

	public void run() {
		WebServer ws = new WebServer(this.port, this.delayed);
		ws.start();
	}

}
