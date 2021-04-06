package CC;

import java.net.Socket;

public class User {
	private String name;
	private Socket s;

	public User(Socket s, String name) {
		this.name = name;
		this.s = s;
	}

	public String getName() {
		return name;
	}

	public Socket getSocket() {
		return s;
	}

}
