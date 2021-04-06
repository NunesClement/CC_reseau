package CC;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import CC.OnlineUser;

@JsonDeserialize(as = Message.class)
public class Message {
	private String toWho;
	private String content;
	private String who;

	public Message() {
	}

	public Message(String toWho, String content, String who) {
		this.toWho = toWho;
		this.content = content + "\n";
		this.who = who;
	}

	public Message(String toWho, String who) {
		this.toWho = toWho;
		this.who = who;
	}

	public static int analyseFormat(String data) {
		return data.split("@-").length;
	}

	public String getToWho() {
		return toWho;
	}

	public String getContent() {
		return content;
	}

	public void setToWho(String toWho) {
		this.toWho = toWho;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setWho(String who) {
		this.who = who;
	}

	public String getWho() {
		return who;
	}
}