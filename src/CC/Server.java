package CC;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;

import CC.*;

/**
 * @author zhl
 */
public class Server {
	private static ServerHandler serverHandler;
	private ServerSocket serverSocket;

	private Server() {
		// Only once
		serverHandler = new ServerHandler();
	}

	private void startAccept() throws IOException {
		try {
			serverSocket = new ServerSocket(4755);
			Send send = new Send();
			Recive recive = new Recive();
			CheckBeats cb = new CheckBeats(6000);
			ExecutorService cached = Executors.newCachedThreadPool();
			cached.execute(send);
			cached.execute(recive);
			cached.execute(cb);
			// Start Listening
			while (true) {
				Socket socket = serverSocket.accept();
				Authorized authorized = new Authorized(socket);
				cached.execute(authorized);
			}
		} finally {
			System.out.println("Vérifiez qu'une instance d'un client ou serveur ne tourne pas deja");
			serverSocket.close();
		}
	}

	/**
	 * For Test
	 * 
	 * @throws IOException
	 * 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Launching");
		Server server = new Server();
		System.out.println("Listening");
		server.startAccept();

	}

	static class Send implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (ServerHandler.waitForSend != null && ServerHandler.waitForSend.size() != 0) {
					try {
						serverHandler.send();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	static class Recive implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (serverHandler.getOnlineUsers().getOnlineUserList().size() != 1) {
					for (int i = 1; i < serverHandler.getOnlineUsers().getOnlineUsers().size(); i++) {
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(serverHandler
									.getOnlineUsers().getOnlineUsers().get(i).getSocket().getInputStream()));
							String temp;
							String data = "";
							if (reader.ready() && (data = reader.readLine()) != null) {

								// System.out.println("(Message.analyseFormat(data" +
								// Message.analyseFormat(data));

								if (Message.analyseFormat(data) == 0) {
									continue;
								}

								System.out.println("data" + data);
								// Simple judge the user who sent is themselves
								System.out.println(data);
								if (Message.analyseFormat(data) == 2) {
									if (!data.split("@-")[1]
											.equals(serverHandler.getOnlineUsers().getOnlineUsers().get(i).getName())) {
										continue;
									}
								}

								serverHandler.analyseToMessage(data.replaceAll("\n", ""));
							} else {

								continue;
							}
						} catch (IOException e) {
							System.out.println("Nothing");
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	static class Authorized implements Runnable {
		Socket s;

		public Authorized(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Writer writer = new OutputStreamWriter(s.getOutputStream());
					BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
					writer.write("Please Input Your Name:\n");
					writer.flush();
					String name;
					while (true) {
						if ((name = reader.readLine()) != null) {
							break;
						}
					}
					if (serverHandler.getOnlineUsers().isRepeat(name)) {
						writer.write("User exists,please rename\n");
						writer.flush();
						continue;
					} else {
						serverHandler.getOnlineUsers().addUser(s, name);
						Message m = new Message(name,
								"Bienvenue sur le serveur, faites /aide pour connaitre les règles ", "Server");

						serverHandler.analyseToMessage(m);
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class CheckBeats implements Runnable {

		int time = 0;

		public CheckBeats(int delayTime) {
			time = delayTime;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(time);
					for (int i = 0; i < serverHandler.getOnlineUsers().getOnlineUsers().size(); i++) {
						for (Message m : serverHandler.getBeats()) {
							if (serverHandler.getOnlineUsers().getOnlineUserList().contains(m.getWho())) {
								continue;
							} else {
								serverHandler.getOnlineUsers()
										.userLogout(serverHandler.getOnlineUsers().getOnlineUsers().get(i).getName());
							}
						}
					}
					serverHandler.initialBeats();
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
