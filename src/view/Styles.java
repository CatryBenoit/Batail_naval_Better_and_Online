package view;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/*
 * Styles communs aux pages de menu.
 */
public final class Styles {

    // ombre sous les textes clairs, pour qu'ils restent lisibles sur une image
    // (commence par ";" : il peut être ajouté à la fin d'un style qui n'en a pas)
    public static final String OMBRE = ";-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 6, 0.5, 0, 1);";

    // voile sombre semi-transparent posé entre l'image de fond et le contenu
    public static final String VOILE = "-fx-background-color: rgba(0, 0, 0, 0.5);";

    private Styles() { }

    /*
     * Met une image de fond derrière le contenu : l'image remplit toute la page (proportions gardées,
     * centrée) et le contenu est posé sur un voile sombre pour que les textes clairs se lisent bien.
     */
    public static StackPane avecFond(Node contenu, String image) {
        contenu.setStyle(contenu.getStyle() + VOILE);
        StackPane fond = new StackPane(contenu);
        fond.setStyle("-fx-background-image: url('" + image + "');"
                + "-fx-background-size: cover;" + "-fx-background-position: center center;");
        return fond;
    }
}
