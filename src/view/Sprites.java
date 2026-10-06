package view;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/*
 * Chargement des images du jeu (dans le classpath, sous /Images), chacune une seule fois.
 * Renvoie null si l'image n'existe pas : l'affichage se rabat alors sur des formes simples.
 */
public final class Sprites {

    public static final String MER = "/Images/effets/mer.jpg";
    public static final String BOMBE = "/Images/effets/bombe.png";
    public static final String EXPLOSION = "/Images/effets/explosion.png";
    public static final String PLOUF = "/Images/effets/plouf.png";

    private static final Map<String, Image> IMAGES = new HashMap<>();

    private Sprites() { }

    public static Image charger(String chemin) {
        if (!IMAGES.containsKey(chemin)) {
            Image image = null;
            try (InputStream in = Sprites.class.getResourceAsStream(chemin)) {
                if (in != null) image = new Image(in);
            } catch (Exception e) {
                System.err.println("Image illisible : " + chemin);
            }
            IMAGES.put(chemin, image);
        }
        return IMAGES.get(chemin);
    }
}
