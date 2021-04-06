package CC;

public class Participant {
    private String nomParticipant;
    private int essaieRestant;
    private int butinEnCoffre;
    private int butinEnMain;

    public Participant(String nomParticipant, int essaieRestant) {
        this.nomParticipant = nomParticipant;
        this.essaieRestant = essaieRestant;
    }

    public Participant(String nomParticipant, int butinEnCoffre, int butinEnMain) {
        this.nomParticipant = nomParticipant;
        this.butinEnCoffre = butinEnCoffre;
        this.butinEnMain = butinEnMain;
    }

    public String getNomParticipant() {
        return nomParticipant;
    }

    public void setNomParticipant(String nomParticipant) {
        this.nomParticipant = nomParticipant;
    }

    public int getEssaieRestant() {
        return essaieRestant;
    }

    public void setEssaieRestant(int essaieRestant) {
        this.essaieRestant = essaieRestant;
    }

    public int getButinEnCoffre() {
        return butinEnCoffre;
    }

    public void setButinEnCoffre(int butinEnCoffre) {
        this.butinEnCoffre = butinEnCoffre;
    }

    public int getButinEnMain() {
        return butinEnMain;
    }

    public void setButinEnMain(int butinEnMain) {
        this.butinEnMain = butinEnMain;
    }

}
