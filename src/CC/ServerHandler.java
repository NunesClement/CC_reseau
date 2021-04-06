package CC;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

import CC.OnlineUser.*;

public class ServerHandler {
	final String REGLES = "Regardez cette vidéo : https://www.youtube.com/watch?v=AZ_gcm2UYP8 \n Puis lisez le readme.md !";
	final int SEUIL_DEMARRAGE = 2;

	public static OnlineUser onlineUser;
	public static List<Message> waitForSend;
	public static List<Message> beats;
	Random rand = new Random();
	public static List<String> typeCarte;
	public static List<Integer> carteRubis;
	public static List<String> typePiege;

	public static double randomNumber;
	public boolean partieDemaree;
	public String carteTiree;
	public int rubisTiree;
	public String piegeTire;
	public int manche;
	public int manchePrecedente;
	public int butinATerre;
	public int nbRelique;
	public int nbPiege; // <= 2
	public int phase; // <= 3

	public ServerHandler() {
		onlineUser = new OnlineUser();
		waitForSend = new ArrayList<Message>();
		onlineUser.addUser(new Socket(), "Server");
		beats = new ArrayList<Message>();
		beats.add(new Message("Server", "alive", "Server"));
		randomNumber = (int) (Math.random() * ((10000 - 3000) + 1)) + 3000;
		partieDemaree = false;

		typeCarte = new ArrayList<String>();
		typeCarte.add("Rubis");
		typeCarte.add("Piege");
		typeCarte.add("Relique");

		typePiege = new ArrayList<String>();
		typePiege.add("Serpent");
		typePiege.add("Boulet");
		typePiege.add("Araignées");
		typePiege.add("Lave");
		typePiege.add("Pic");

		carteRubis = new ArrayList<Integer>();
		carteRubis.add(1);
		carteRubis.add(2);
		carteRubis.add(3);
		carteRubis.add(4);
		carteRubis.add(5);
		carteRubis.add(5);
		carteRubis.add(7);
		carteRubis.add(7);
		carteRubis.add(9);
		carteRubis.add(11);
		carteRubis.add(11);
		carteRubis.add(13);
		carteRubis.add(14);
		carteRubis.add(15);
		carteRubis.add(17);

		carteTiree = typeCarte.get(rand.nextInt(typeCarte.size()));
		rubisTiree = 0;
		manche = 0;
		manchePrecedente = 0;
		butinATerre = 0;
		nbRelique = 0;
		nbPiege = 0;
		phase = 0;
		// System.out.println(" carteTiree : " + carteTiree);
	}

	/**
	 * send message to users
	 * 
	 * @throws IOException
	 */
	public void send() throws IOException {
		int p = 0;
		Writer writer;
		for (int i = 0; i < waitForSend.size(); i++) {
			Message m = waitForSend.get(p);
			// Quand le serveur reçoit un message du client
			System.out.println("randomNumber" + randomNumber);
			if (m.getToWho().equals("Server")) {
				System.out.println("[" + m.getWho() + "]" + m.getContent());
				waitForSend.remove(p);
				continue;
			}
			Socket client = onlineUser.getUser(m.getToWho());
			// If do not exist User
			if (client == null) {
				waitForSend.add(new Message(m.getWho(), "L'utilisateur ou la commande n'existe pas !", "Server"));
				waitForSend.remove(p);
				continue;
			}
			writer = new OutputStreamWriter(client.getOutputStream());

			ObjectMapper om = new ObjectMapper();
			String json = om.writeValueAsString(m);
			writer.write(json);
			writer.write("parse-here");
			if (m.getContent() != null) {
				writer.write(m.getContent());
			}

			System.out.println("Send: " + m.getToWho() + " " + m.getContent());

			writer.flush();
			waitForSend.remove(p);
		}
	}

	public void analyseToMessage(String data) {
		Message message = new Message();

		String toWho = "";
		String content = "";
		String who = "";
		if (data.split("@-").length == 2) {
			toWho = data.split("@-")[0];
			who = data.split("@-")[1];
			message = new Message(toWho, who);
			System.out.println("Message vide de  " + who + " " + "adressé à " + toWho);
		}
		if (data.split("@-").length == 3) {
			toWho = data.split("@-")[0];
			content = data.split("@-")[1];
			who = data.split("@-")[2];
			message = new Message(toWho, content, who);
			System.out.println("Message de  " + who + "/ " + content + "/adressé à " + toWho);
		}

		if ("Server".equals(toWho)) {
			beats.add(message);
			// waitForSend.add(message);

			return;
		}

		// Broadcast to all users + server
		if (toWho.equals("all")) {
			List<String> connectedUser = onlineUser.getOnlineUserList();
			for (int j = 0; j < connectedUser.size(); j++) {
				if (!who.equals(connectedUser.get(j))) {
					if (content.equals("")) {
						message = new Message(connectedUser.get(j), who);
						waitForSend.add(message);
					} else {
						message = new Message(connectedUser.get(j), content, who);
						waitForSend.add(message);
					}

				}
			}
			return;
		}

		// Broadcast to all users + withoutserver
		if (toWho.equals("allClient")) {
			List<String> connectedUser = onlineUser.getOnlineUserList();
			for (int j = 0; j < connectedUser.size(); j++) {
				if (!who.equals(connectedUser.get(j)) && !who.equals("Server")) {
					if (content.equals("")) {
						message = new Message(connectedUser.get(j), who);
						waitForSend.add(message);
					} else {
						message = new Message(connectedUser.get(j), content, who);
						waitForSend.add(message);
					}
				}
			}
			return;
		}
		List<Participant> participantUser = onlineUser.getParticipants();

		if (participantUser.size() >= SEUIL_DEMARRAGE) { // seuil de démarrage -
			partieDemaree = true;
			// System.out.println("a déjà démarré");
		}

		if (partieDemaree && (toWho.equals("/stop") || toWho.equals("/encore"))) {
			// vérifier si c'est un participant
			boolean verifParticipation = false;
			for (int j = 0; j < participantUser.size(); j++) {
				if (participantUser.get(j).getNomParticipant().equals(who)) {
					// réduire la participation de who
					if (participantUser.get(j).getEssaieRestant() > 0) {
						participantUser.get(j).setEssaieRestant(participantUser.get(j).getEssaieRestant() - 1);
						verifParticipation = true;
					}
				}
			}
			if (verifParticipation) {
				// répondre publiquement à la réponse d'un client
				for (int k = 0; k < participantUser.size(); k++) {
					if (participantUser.get(k).getNomParticipant().equals(who)) {
						participantUser.get(k).setDernierChoix(toWho);
						message = new Message(who, "Vous avez fait le choix secret " + toWho, "Server");
						waitForSend.add(message);
						return;
					}
				}

			}

			// si plus personne ne peut jouer alors la partie se coupe et il faut de nouveau
			// participer
			boolean auMoinsUnParticipantAunEssaieRestant = false;
			for (int j = 0; j < participantUser.size(); j++) {
				if (!(participantUser.get(j).getEssaieRestant() <= 0)) {
					auMoinsUnParticipantAunEssaieRestant = true;
				}
			}
			if (!auMoinsUnParticipantAunEssaieRestant) {
				for (int k = 0; k < participantUser.size(); k++) {
					message = new Message(participantUser.get(k).getNomParticipant(),
							" personne a trouvé ! partie terminée", "Server");
					waitForSend.add(message);
				}
			}

			return;
		}
		if (toWho.equals("/aide")) {
			message = new Message(who, "Pour afficher cet aide : /aide", "Server");
			waitForSend.add(message);
			message = new Message(who, "Pour quitter : /quitter", "Server");
			waitForSend.add(message);
			message = new Message(who, "Pour afficher le classement du serveur : /classement", "Server");
			waitForSend.add(message);
			message = new Message(who, "Pour afficher l'état de la manche en cours : /statut", "Server");
			waitForSend.add(message);
			message = new Message(who, "Pour faire un choix lors d’une manche : /stop ou /encore", "Server");
			waitForSend.add(message);
			return;
		}
		if (toWho.equals("/status") || toWho.equals("status") || toWho.equals("/statut") || toWho.equals("statut")) {
			String concat = "";
			for (int k = 0; k < participantUser.size(); k++) {
				concat += " , " + participantUser.get(k).getNomParticipant();
				if (participantUser.get(k).getNomParticipant().equals(who)) {
					message = new Message(who,
							"Vous avez en main : " + participantUser.get(k).getButinEnMain() + " Rubis", "Server");
					waitForSend.add(message);

					message = new Message(who, "Manche en cours :", "Server");
					waitForSend.add(message);

					message = new Message(who, "Il y a eu " + nbPiege + " pièges rencontrés", "Server");
					waitForSend.add(message);

					message = new Message(who, "Il y a eu " + nbRelique + " relique au sol", "Server");
					waitForSend.add(message);

					message = new Message(who, "Il y a eu " + butinATerre + " butin à terre", "Server");
					waitForSend.add(message);
				}

			}
			message = new Message(who, "Il y a  : " + participantUser.size() + " participant(s) dont " + concat,
					"Server");
			waitForSend.add(message);
			return;

		}
		if (toWho.equals("/quitter")) {

			for (int k = 0; k < participantUser.size(); k++) {
				if (participantUser.get(k).getNomParticipant().equals(who)) {
					message = new Message(who, "vous avez quitté la partie", "Server");
					waitForSend.add(message);
				}
				message = new Message(participantUser.get(k).getNomParticipant(),
						who + " a quitté la partie ! C'est un looser.", "Server");
				waitForSend.add(message);
			}
			return;
		}
		if (toWho.equals("pret")) {
			System.out.println("manchePrecedente" + manchePrecedente);
			System.out.println("manche" + manche);

			for (int k = 0; k < participantUser.size(); k++) {
				message = new Message(participantUser.get(k).getNomParticipant(), "Le jeu continu !", "Server");
				waitForSend.add(message);
			}
			if (manchePrecedente != manche || phase >= 3) {
				System.out.println("Nouvelle manche");
				phase = 1;
				for (int k = 0; k < participantUser.size(); k++) {
					message = new Message(participantUser.get(k).getNomParticipant(), "Une nouvelle manche commence !",
							"Server");
					waitForSend.add(message);
				}
			}
			for (int k = 0; k < participantUser.size(); k++) {
				message = new Message(participantUser.get(k).getNomParticipant(),
						"La premiere carte tirée est " + carteTiree, "Server");
				waitForSend.add(message);
			}
			if (carteTiree.equals("Rubis")) {
				// répartir équitablement et avertir
				rubisTiree = carteRubis.get(rand.nextInt(carteRubis.size()));
				int aterre = rubisTiree % participantUser.size();
				int recuParJoueur = Math.round(rubisTiree / participantUser.size());
				butinATerre += aterre;
				for (int k = 0; k < participantUser.size(); k++) {
					message = new Message(participantUser.get(k).getNomParticipant(),
							"Vous avez reçu " + recuParJoueur + " et le butin total : " + butinATerre + " est à terre",
							"Server");
					participantUser.get(k).setButinEnMain(participantUser.get(k).getButinEnMain() + recuParJoueur);
					waitForSend.add(message);
				}
			}
			if (carteTiree.equals("Relique")) {
				for (int k = 0; k < participantUser.size(); k++) {
					butinATerre += 5;
					nbRelique = nbRelique + 1;
					message = new Message(participantUser.get(k).getNomParticipant(), "Relique, + 5 Rubis à terre "
							+ butinATerre + " est le butin total à terre " + " dont " + nbRelique + " relique",
							"Server");
					waitForSend.add(message);
				}
			}
			if (carteTiree.equals("Piege")) {
				piegeTire = typePiege.get(rand.nextInt(typePiege.size()));

				nbPiege += 1;
				if (nbPiege == 1) {
					for (int k = 0; k < participantUser.size(); k++) {
						message = new Message(participantUser.get(k).getNomParticipant(),
								piegeTire + " est sur votre passage !", "Server");
						waitForSend.add(message);
						message = new Message(participantUser.get(k).getNomParticipant(),
								"c'est le premier piège, il ne se passe rien mais faites attention !", "Server");
						waitForSend.add(message);
					}
				}
				if (nbPiege >= 2) {
					for (int k = 0; k < participantUser.size(); k++) {
						butinATerre = 0;
						participantUser.get(k).setButinEnMain(0);
						message = new Message(participantUser.get(k).getNomParticipant(),
								piegeTire + " est sur votre passage !", "Server");
						waitForSend.add(message);
						message = new Message(participantUser.get(k).getNomParticipant(),
								"Ouch ! second piège, vous perdez-vos butin", "Server");
						manche += 1;
						nbRelique = 0;
						nbPiege = 0;
						waitForSend.add(message);
					}
				}
			}
			return;
		}
		if (toWho.equals("/classement")) {
			List<Classement> c = onlineUser.getClassement();
			for (int i = 0; i < c.size(); i++) {
				message = new Message(who, c.get(i).getNom() + " a " + c.get(i).getNbRubis() + " rubis stockés !",
						"Server");
			}
			waitForSend.add(message);
			return;
		}

		if (toWho.equals("participe") || toWho.equals("/participe")) {

			for (int j = 0; j < participantUser.size(); j++) {
				if (participantUser.get(j).getNomParticipant().equals(who)) {
					message = new Message(who, "Vous etes déjà inscrit", "Server");
					waitForSend.add(message);
					return;
				}
			}
			if (partieDemaree != true) {
				Participant p = new Participant(who, 2);
				onlineUser.addParticipant(p);
				message = new Message(who,
						"Vous venez d'etre inscrit, bon jeu il y a " + participantUser.size() + " joueur(s) inscrit(s)",
						"Server");
				waitForSend.add(message);
				if (participantUser.size() == SEUIL_DEMARRAGE) { // seuil de démarrage -
					partieDemaree = true;
					manche = 1;
					for (int k = 0; k < participantUser.size(); k++) {
						message = new Message(participantUser.get(k).getNomParticipant(),
								" La partie de diamant peut commencer !! Faites la commande pret@-VotreNom", "Server");

						waitForSend.add(message);
					}

				}

				return;
			}
			waitForSend.add(message);
			return;
		}

		waitForSend.add(message);

	}

	public static boolean isInteger(String s) {
		boolean isValidInteger = false;
		try {
			Integer.parseInt(s);

			// s is a valid integer

			isValidInteger = true;
		} catch (NumberFormatException ex) {
			// s is not an integer
		}

		return isValidInteger;
	}

	public void analyseToMessage(Message m) {
		waitForSend.add(m);
	}

	public OnlineUser getOnlineUsers() {
		return onlineUser;
	}

	public List<Message> getBeats() {
		return beats;
	}

	public void initialBeats() {
		beats = new ArrayList<Message>();
		beats.add(new Message("Server", "alive", "Server"));
	}

}
