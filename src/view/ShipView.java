package view;

import boardifier.model.Model;
import boardifier.view.GameStageView;
import boardifier.view.RootPane;
import boardifier.view.View;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ShipView extends View {

    public ShipView(Model model, Stage stage, RootPane rootPane) {
        super(model, stage, rootPane);
        // fenêtre redimensionnable : le système refuse le plein écran à une fenêtre de taille fixe
        stage.setResizable(true);

        // plein écran : F11 (filtre sur la fenêtre, donc actif quelle que soit la page affichée) ou le menu
        stage.setFullScreenExitHint("Plein écran : appuyez sur F11 ou Échap pour en sortir");
        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                e.consume();
            }
        });
        stage.fullScreenProperty().addListener((obs, avant, plein) -> {
            menuPleinEcran.setSelected(plein);
            if (!plein) {
                // on retrouve une fenêtre à la taille du contenu, une fois que le système a fini de la restaurer
                PauseTransition attente = new PauseTransition(Duration.millis(200));
                attente.setOnFinished(e -> stage.sizeToScene());
                attente.play();
            }
            ajusterPleinEcran();
        });
        // le contenu change (page de menu, partie...) ou la fenêtre change de taille : on recalcule l'échelle
        rootPane.getChildren().addListener((ListChangeListener<Node>) c -> Platform.runLater(this::ajusterPleinEcran));
        stage.sceneProperty().addListener((obs, avant, apres) -> Platform.runLater(this::ajusterPleinEcran));
        stage.widthProperty().addListener((obs, avant, apres) -> ajusterPleinEcran());
        stage.heightProperty().addListener((obs, avant, apres) -> ajusterPleinEcran());
    }

    private javafx.scene.control.MenuItem menuStart, menuIntro, menuQuit;
    private CheckMenuItem menuPleinEcran;
    // vrai quand une partie est affichée (sinon : une page de menu)
    private boolean partieAffichee;

    @Override
    protected void createMenuBar() {
        menuBar = new javafx.scene.control.MenuBar();
        javafx.scene.control.Menu menu1 = new javafx.scene.control.Menu("Game");
        menuStart = new javafx.scene.control.MenuItem("New game");
        menuIntro = new javafx.scene.control.MenuItem("Intro");
        menuPleinEcran = new CheckMenuItem("Plein écran (F11)");
        menuPleinEcran.setOnAction(e -> stage.setFullScreen(menuPleinEcran.isSelected()));
        menuQuit = new javafx.scene.control.MenuItem("Quit");
        menu1.getItems().addAll(menuStart, menuIntro, menuPleinEcran, menuQuit);
        menuBar.getMenus().add(menu1);
    }

    /*
     * Le framework crée une nouvelle scène à chaque partie (et repasse la fenêtre en taille fixe), ce qui fait
     * sortir du plein écran. Ici on garde la même scène et on change seulement son contenu.
     */
    @Override
    public void setView(GameStageView gameStageView) {
        rootPane.init(gameStageView);
        this.gameStageView = gameStageView;
        partieAffichee = true;
        rootPane.setClip(null);
        if (!stage.isFullScreen()) stage.sizeToScene();
    }

    @Override
    public void resetView() {
        rootPane.resetToDefault();
        partieAffichee = false;
        rootPane.setClip(null);
        if (!stage.isFullScreen()) stage.sizeToScene();
    }

    /*
     * Le contenu (page de menu ou partie) garde sa taille "normale", mais il est agrandi ou réduit pour
     * remplir la fenêtre (plein écran ou fenêtre redimensionnée), sans être déformé, et centré
     * (sur fond noir pour les menus, gris clair pour la partie : les grilles sont dessinées en noir).
     * Les clics restent justes : le framework convertit les coordonnées en tenant compte des transformations.
     */
    private void ajusterPleinEcran() {
        if (stage.getScene() == null) return;
        vbox.setStyle(partieAffichee ? "-fx-background-color: #f4f4f4;" : "-fx-background-color: black;");
        double hauteurMenu = (menuBar != null) ? menuBar.getHeight() : 0;
        double largeurDispo = stage.getScene().getWidth();
        double hauteurDispo = stage.getScene().getHeight() - hauteurMenu;
        // taille naturelle du contenu (sans agrandissement)
        double largeur = rootPane.prefWidth(-1);
        double hauteur = rootPane.prefHeight(-1);
        if (largeur <= 0 || hauteur <= 0 || largeurDispo <= 0 || hauteurDispo <= 0) return;
        double echelle = Math.min(largeurDispo / largeur, hauteurDispo / hauteur);
        rootPane.getTransforms().setAll(
                new Translate((largeurDispo - largeur * echelle) / 2, (hauteurDispo - hauteur * echelle) / 2),
                new Scale(echelle, echelle));
    }


    public javafx.scene.control.MenuItem getMenuStart() {
        return menuStart;
    }

    public javafx.scene.control.MenuItem getMenuIntro() {
        return menuIntro;
    }

    public javafx.scene.control.MenuItem getMenuQuit() {
        return menuQuit;
    }
}
