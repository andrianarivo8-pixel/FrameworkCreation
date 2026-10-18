package main.java.model;

/**
 * Représente un fichier uploadé reçu depuis un formulaire multipart.
 */
public class MapFile {

    private String nomFichier;   // nom d'origine du fichier
    private String type;         // content-type MIME (ex: image/png)
    private byte[] contenu;      // contenu binaire

    public MapFile() {
    }

    public MapFile(String nomFichier, String type, byte[] contenu) {
        this.nomFichier = nomFichier;
        this.type = type;
        this.contenu = contenu;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }

    /** Indique si un fichier a réellement été envoyé. */
    public boolean isEmpty() {
        return contenu == null || contenu.length == 0;
    }
}