package CC;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class OnlineUser {
	private List<User> onlineUser;
	int ordinal = 0;
	private List<Participant> participants = new ArrayList<Participant>();
	private List<Victoire> classement = new ArrayList<Victoire>();

	public OnlineUser() {
		onlineUser = new ArrayList<User>();
	}

	public void addUser(Socket s, String name) {
		User u = new User(s, name);
		onlineUser.add(u);
		System.out.println(name + " Online");
	}

	public void userLogout(String name) throws IOException {
		for (int i = 0; i < onlineUser.size(); i++) {
			if (onlineUser.get(i).getName().equals(name)) {
				onlineUser.get(i).getSocket().close();
				onlineUser.remove(i);
				break;
			}
		}
	}

	public Socket getUser(String name) throws IOException {

		for (User user : onlineUser) {
			if (user.getName().equals(name)) {
				return user.getSocket();
			}
		}
		return null;
	}

	public List<String> getOnlineUserList() {
		List<String> nameList = new ArrayList<String>();
		for (User user : onlineUser) {
			nameList.add(user.getName());
		}
		return nameList;
	}

	public List<User> getOnlineUsers() {
		return onlineUser;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public List<Victoire> getClassement() {
		return classement;
	}

	public void setParticipants(List<Participant> lp) {
		participants = lp;
	}

	public void addParticipant(Participant name) {
		participants.add(name);
	}

	public void setClassement(List<Victoire> lc) {
		classement = lc;
	}

	public void addClassement(Victoire v) {
		classement.add(v);
	}

	public boolean isRepeat(String name) throws IOException {
		for (User u : onlineUser) {
			if (u.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
