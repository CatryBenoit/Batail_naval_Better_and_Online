package view;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/*
 * Page "Jouer en ligne" : adresse du serveur, pseudo, création d'une partie et liste des parties à rejoindre.
 */
public class LobbyPage {
    private static final String STYLE_TITRE = "-fx-font-size: 20;" + "-fx-font-weight: bold;" + "-fx-text-fill: #F0FFF0;" + Styles.OMBRE;
    private static final String STYLE_TEXTE = "-fx-font-size: 16;" + "-fx-font-weight: bold;" + "-fx-text-fill: #F0FFF0;" + Styles.OMBRE;
    private static final String STYLE_CHAMP = "-fx-font-size: 15;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;" + "-fx-text-fill: Black;";
    private static final String STYLE_BOUTON = "-fx-font-size: 16;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;" + "-fx-text-fill: Black;";

    private TextField serveur, pseudo, missiles;
    private ComboBox<String> mode, premier;
    private Button creer, annuler, actualiser, rejoindre, retour;
    private ListView<String> parties;
    private Label statut;

    public LobbyPage() {
        initWidgets();
    }

    private void initWidgets() {
        serveur = new TextField();
        serveur.setPromptText("localhost:8765");
        pseudo = new TextField();
        pseudo.setPromptText("votre pseudo");
        missiles = new TextField();
        missiles.setPromptText("défaut");
        missiles.setPrefWidth(90);
        missiles.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                missiles.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        mode = new ComboBox<>(FXCollections.observableArrayList("mode 1", "mode 2"));
        mode.getSelectionModel().select(0);
        premier = new ComboBox<>(FXCollections.observableArrayList("Moi", "Mon adversaire", "Au hasard"));
        premier.getSelectionModel().select(0);
        creer = new Button("Créer une partie");
        annuler = new Button("Annuler");
        actualiser = new Button("Actualiser");
        rejoindre = new Button("Rejoindre");
        retour = new Button("Retour");
        parties = new ListView<>();
        parties.setPrefHeight(130);
        parties.setPlaceholder(new Label("Aucune partie en attente"));
        statut = new Label("");
        statut.setWrapText(true);
        statut.setMaxWidth(860);
    }

    public void placeWidgets(ShipRootPane root) {
        root.setStyle("-fx-alignment: center");

        Label titre = new Label("Jouer en ligne");
        titre.setStyle("-fx-font-size: 30;" + "-fx-font-weight: bold;" + "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);

        // connexion
        Label lServeur = new Label("Serveur");
        Label lPseudo = new Label("Pseudo");
        lServeur.setStyle(STYLE_TEXTE);
        lPseudo.setStyle(STYLE_TEXTE);
        serveur.setStyle(STYLE_CHAMP);
        pseudo.setStyle(STYLE_CHAMP);
        serveur.setPrefWidth(300);
        HBox hConnexion = new HBox(15, lServeur, serveur, lPseudo, pseudo);
        hConnexion.setAlignment(Pos.CENTER_LEFT);

        // création
        Label lCreer = new Label("Créer une partie");
        lCreer.setStyle(STYLE_TITRE);
        Label lMode = new Label("Mode");
        Label lPremier = new Label("Premier tireur");
        Label lMissiles = new Label("Missiles");
        lMode.setStyle(STYLE_TEXTE);
        lPremier.setStyle(STYLE_TEXTE);
        lMissiles.setStyle(STYLE_TEXTE);
        mode.setStyle(STYLE_CHAMP);
        premier.setStyle(STYLE_CHAMP);
        missiles.setStyle(STYLE_CHAMP);
        creer.setStyle(STYLE_BOUTON);
        annuler.setStyle(STYLE_BOUTON);
        HBox hOptions = new HBox(15, lMode, mode, lPremier, premier, lMissiles, missiles);
        hOptions.setAlignment(Pos.CENTER_LEFT);
        HBox hCreer = new HBox(15, creer, annuler);
        hCreer.setAlignment(Pos.CENTER_LEFT);

        // parties disponibles
        Label lRejoindre = new Label("Rejoindre une partie");
        lRejoindre.setStyle(STYLE_TITRE);
        actualiser.setStyle(STYLE_BOUTON);
        rejoindre.setStyle(STYLE_BOUTON);
        HBox hBoutons = new HBox(15, actualiser, rejoindre);

        statut.setStyle("-fx-font-size: 16;" + "-fx-font-weight: bold;" + "-fx-text-fill: #FFD700;" + Styles.OMBRE);
        retour.setStyle(STYLE_BOUTON);

        VBox v = new VBox(14, titre, hConnexion, lCreer, hOptions, hCreer, lRejoindre, parties, hBoutons, statut, retour);
        v.setMinSize(900, 600);
        v.setMaxWidth(900);
        v.setStyle("-fx-padding: 20px 20px 20px 20px;");

        root.getChildren().add(Styles.avecFond(v, "/Images/background/imageoptionmenu.png"));
    }

    public TextField getServeur() { return serveur; }
    public TextField getPseudo() { return pseudo; }
    public TextField getMissiles() { return missiles; }
    public ComboBox<String> getMode() { return mode; }
    public ComboBox<String> getPremier() { return premier; }
    public Button getCreer() { return creer; }
    public Button getAnnuler() { return annuler; }
    public Button getActualiser() { return actualiser; }
    public Button getRejoindre() { return rejoindre; }
    public Button getRetour() { return retour; }
    public ListView<String> getParties() { return parties; }
    public Label getStatut() { return statut; }
}
