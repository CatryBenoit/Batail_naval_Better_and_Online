package control;

import boardifier.control.StageFactory;
import boardifier.model.GameException;
import boardifier.model.Model;
import model.ConfigPartie;
import view.HomePage;
import view.LobbyPage;
import view.SelectionPage;
import view.ShipRootPane;

public class PageControl {
    ShipRootPane root;
    SelectionPage sp;

    BattleShipControler control;

    Model model;
    public PageControl(ShipRootPane root,BattleShipControler control, Model model){
        this.control=control;

        this.root=root;
        this.model=model;

    }
    public void hp(HomePage homePage){
        root.getChildren().clear();
        homePage.placeWidgets(root);
        ajusterFenetre();
    }
    public void sp(SelectionPage selectionPage){
        root.getChildren().clear();
        selectionPage.placeWidgets(root);
        ajusterFenetre();
    }

    // après une partie, la zone d'affichage est découpée à la taille du jeu : on l'enlève pour les pages de menu
    private void ajusterFenetre(){
        root.setClip(null);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            root.getScene().getWindow().sizeToScene();
        }
    }
    public void lobby(LobbyPage lobbyPage){
        root.getChildren().clear();
        lobbyPage.placeWidgets(root);
        ajusterFenetre();
    }

    public void quit(){
        root.getChildren().clear();
        System.exit(0);
    }

    public void play() throws GameException {
        int[] buttons = ButtonController.returnValues();
        ConfigPartie.mode = buttons[3];
        // nombre de missiles personnalisé seulement si la case est cochée
        ConfigPartie.nbMissiles = (buttons[4] == 1) ? TextController.getMissiles() : -1;
        ConfigPartie.premierTireur = buttons[2];
        ConfigPartie.niveauIA = new int[]{buttons[0], buttons[1]};

        // on repart d'une liste vide (sinon chaque clic sur "start" ajoutait 2 joueurs de plus)
        model.getPlayers().clear();
        ajouterJoueur(1, buttons[0]);
        ajouterJoueur(2, buttons[1]);

        control.setEnLigne(null);
        control.startGame();
    }

    /*
     * Lance une partie en ligne (la configuration a déjà été mise dans ConfigPartie par le lobby).
     * L'adversaire distant est un joueur "ordinateur" pour le framework : ses tours arrivent par le réseau.
     */
    public void playEnLigne(PartieEnLigne partie, String monPseudo) throws GameException {
        model.getPlayers().clear();
        String[] noms = new String[2];
        noms[partie.monId] = "Joueur " + (partie.monId + 1) + " (" + monPseudo + ")";
        noms[partie.idAdversaire] = "Joueur " + (partie.idAdversaire + 1) + " (" + partie.pseudoAdversaire + ")";
        for (int i = 0; i < 2; i++) {
            if (i == partie.monId) model.addHumanPlayer(noms[i]);
            else model.addComputerPlayer(noms[i]);
        }
        control.setEnLigne(partie);
        control.startGame();

        



    }

    private void ajouterJoueur(int numero, int type){
        String nom = "Joueur " + numero;
        if (type == 1) {
            model.addComputerPlayer(nom + " (IA facile)");
        } else if (type == 2) {
            model.addComputerPlayer(nom + " (IA difficile)");
        } else {
            model.addHumanPlayer(nom + " (humain)");
        }
    }

    public void option(){
        root.getChildren().clear();
    }
}
