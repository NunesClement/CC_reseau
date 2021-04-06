package CC;

public class Classement {
    private int nbRubis;
    private String nom;

    public Classement(int nbRubis, String nom) {
        this.nbRubis = nbRubis;
        this.nom = nom;
    }

    public String getNbRubis() {
        return nbRubis;
    }

    public void setNbRubis(String nbRubis) {
        this.nbRubis = nbRubis;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

}
