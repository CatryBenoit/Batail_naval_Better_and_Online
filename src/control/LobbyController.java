package control;

import boardifier.model.GameException;
import javafx.application.Platform;
import model.ConfigPartie;
import reseau.ClientReseau;
import reseau.Json;
import view.HomePage;
import view.LobbyPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/*
 * Page "Jouer en ligne" : créer une partie (et attendre un adversaire) ou en rejoindre une.
 * Les appels réseau se font dans un thread à part, les résultats sont renvoyés sur le thread JavaFX.
 * Une fois la partie lancée, les messages reçus sont transmis à la PartieEnLigne.
 */
public class LobbyController {

    private static final String SERVEUR_PAR_DEFAUT = "localhost:8765";

    private final LobbyPage page;
    private final PageControl pageControl;
    private final BattleShipControler control;
    // le serveur et le pseudo sont retenus d'une fois sur l'autre
    private final Preferences prefs = Preferences.userNodeForPackage(LobbyController.class);

    private List<Map<String, Object>> partiesAffichees = new ArrayList<>();
    // numéro de la dernière demande de liste : les réponses des demandes précédentes sont ignorées
    private int numeroListe;
    // client de la partie créée ou rejointe (null sinon)
    private ClientReseau client;
    private PartieEnLigne partie;
    // configuration de la partie que j'héberge, en attendant un adversaire
    private int modeHote, missilesHote, premierHote;

    public LobbyController(LobbyPage page, HomePage homePage, PageControl pageControl, BattleShipControler control) {
        this.page = page;
        this.pageControl = pageControl;
        this.control = control;

        homePage.getOnline().setOnAction(e -> ouvrir());
        page.getCreer().setOnAction(e -> creer());
        page.getAnnuler().setOnAction(e -> annulerAttente());
        page.getActualiser().setOnAction(e -> actualiser());
        page.getRejoindre().setOnAction(e -> rejoindre());
        page.getParties().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) rejoindre();
        });
        page.getRetour().setOnAction(e -> {
            annulerAttente();
            pageControl.hp(homePage);
        });
        etatAttente(false);
    }

    public void ouvrir() {
        page.getServeur().setText(prefs.get("serveur", SERVEUR_PAR_DEFAUT));
        page.getPseudo().setText(prefs.get("pseudo", ""));
        pageControl.lobby(page);
        actualiser();
    }


    // ================================================================ actions

    private void actualiser() {
        String url = serveur();
        int numero = ++numeroListe;
        statut("Recherche des parties...");
        Thread t = new Thread(() -> {
            try {
                List<Map<String, Object>> liste = new ClientReseau(url).listerParties();
                Platform.runLater(() -> {
                    if (numero == numeroListe) afficherParties(liste);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    if (numero != numeroListe) return;
                    page.getParties().getItems().clear();
                    partiesAffichees = new ArrayList<>();
                    if (client == null) statut("Impossible de récupérer les parties : " + e.getMessage());
                });
            }
        }, "lobby-liste");
        t.setDaemon(true);
        t.start();
    }

    private void afficherParties(List<Map<String, Object>> liste) {
        partiesAffichees = liste;
        List<String> lignes = new ArrayList<>();
        for (Map<String, Object> p : liste) {
            int missiles = Json.entier(p, "missiles", -1);
            lignes.add(Json.texte(p, "nom", "Partie") + "   —   mode " + (Json.entier(p, "mode", 0) + 1)
                    + "   —   missiles : " + (missiles > 0 ? missiles : "par défaut"));
        }
        page.getParties().getItems().setAll(lignes);
        if (client == null) {
            statut(liste.isEmpty() ? "Aucune partie en attente : créez-en une !" : liste.size() + " partie(s) en attente.");
        }
    }

    private void creer() {
        if (client != null) return;
        memoriser();
        String pseudo = pseudo();
        int mode = page.getMode().getSelectionModel().getSelectedIndex();
        int missiles = missiles();
        // "Moi" = l'hôte (joueur 1), "Mon adversaire" = l'invité (joueur 2), "Au hasard" = tiré par le serveur
        int premier = page.getPremier().getSelectionModel().getSelectedIndex();
        ClientReseau nouveau = new ClientReseau(serveur());
        statut("Création de la partie...");
        enArrierePlan(() -> {
            Map<String, Object> r = nouveau.creerPartie(pseudo, mode, missiles, premier);
            Platform.runLater(() -> {
                client = nouveau;
                modeHote = mode;
                missilesHote = missiles;
                premierHote = Json.entier(r, "premier", 0);
                etatAttente(true);
                statut("Partie créée : en attente d'un adversaire...");
                ecouter(nouveau);
            });
        }, "Impossible de créer la partie");
    }

    private void rejoindre() {
        if (client != null) return;
        int index = page.getParties().getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= partiesAffichees.size()) {
            statut("Choisissez d'abord une partie dans la liste.");
            return;
        }
        memoriser();
        String id = Json.texte(partiesAffichees.get(index), "id", "");
        String pseudo = pseudo();
        ClientReseau nouveau = new ClientReseau(serveur());
        statut("Connexion à la partie...");
        enArrierePlan(() -> {
            Map<String, Object> r = nouveau.rejoindre(id, pseudo);
            Platform.runLater(() -> {
                client = nouveau;
                lancerPartie(1, Json.texte(r, "pseudoAdversaire", "Adversaire"), Json.entier(r, "mode", 0),
                        Json.entier(r, "missiles", -1), Json.entier(r, "premier", 0));
                ecouter(nouveau);
            });
        }, "Impossible de rejoindre la partie");
    }

    private void annulerAttente() {
        if (client != null && partie == null) {
            client.quitter();
            client = null;
            etatAttente(false);
            statut("Partie annulée.");
        }
    }


    // ================================================================ partie

    private void ecouter(ClientReseau c) {
        // tout est renvoyé sur le thread JavaFX, dans l'ordre d'arrivée
        c.ecouter(m -> Platform.runLater(() -> recevoir(c, m)),
                raison -> Platform.runLater(() -> finConnexion(c, raison)));
    }

    private void recevoir(ClientReseau c, Map<String, Object> message) {
        if (c != client) return; // message d'une ancienne partie
        if (partie != null) {
            partie.recevoir(message);
        } else if ("REJOINT".equals(Json.texte(message, "type", ""))) {
            // un adversaire a rejoint la partie que j'héberge : c'est parti
            lancerPartie(0, Json.texte(message, "pseudo", "Adversaire"), modeHote, missilesHote, premierHote);
        }
    }

    private void finConnexion(ClientReseau c, String raison) {
        if (c != client) return;
        if (partie != null) {
            partie.connexionPerdue(raison);
        } else {
            client = null;
            etatAttente(false);
            statut(raison);
        }
    }

    // monId : 0 = hôte (joueur 1), 1 = invité (joueur 2)
    private void lancerPartie(int monId, String pseudoAdversaire, int mode, int missiles, int premier) {
        ConfigPartie.mode = mode;
        ConfigPartie.nbMissiles = missiles;
        ConfigPartie.premierTireur = premier;
        ConfigPartie.niveauIA = new int[]{0, 0};
        partie = new PartieEnLigne(client, monId, pseudoAdversaire, control, this::partieTerminee);
        try {
            pageControl.playEnLigne(partie, pseudo());
        } catch (GameException e) {
            partie.connexionPerdue("Impossible de lancer la partie : " + e.getMessage());
        }
    }

    // appelé quand la partie en ligne est finie, abandonnée ou interrompue
    private void partieTerminee() {
        partie = null;
        client = null;
        etatAttente(false);
        statut("");
    }


    // ================================================================ outils

    private String serveur() {
        String s = page.getServeur().getText().trim();
        return s.isEmpty() ? SERVEUR_PAR_DEFAUT : s;
    }

    private String pseudo() {
        String s = page.getPseudo().getText().trim();
        return s.isEmpty() ? "Joueur" : s;
    }

    // -1 = nombre de missiles par défaut du mode
    private int missiles() {
        try {
            int n = Integer.parseInt(page.getMissiles().getText().trim());
            return (n >= 1) ? Math.min(n, 100) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void memoriser() {
        prefs.put("serveur", serveur());
        prefs.put("pseudo", page.getPseudo().getText().trim());
    }

    private void statut(String texte) {
        page.getStatut().setText(texte);
    }

    // pendant l'attente d'un adversaire, on ne peut que annuler
    private void etatAttente(boolean attente) {
        page.getCreer().setDisable(attente);
        page.getRejoindre().setDisable(attente);
        page.getActualiser().setDisable(attente);
        page.getServeur().setDisable(attente);
        page.getPseudo().setDisable(attente);
        page.getMode().setDisable(attente);
        page.getPremier().setDisable(attente);
        page.getMissiles().setDisable(attente);
        page.getAnnuler().setVisible(attente);
        page.getAnnuler().setManaged(attente);
    }

    private interface TacheReseau {
        void executer() throws IOException;
    }

    private void enArrierePlan(TacheReseau tache, String messageErreur) {
        Thread t = new Thread(() -> {
            try {
                tache.executer();
            } catch (IOException e) {
                Platform.runLater(() -> statut(messageErreur + " : " + e.getMessage()));
            }
        }, "lobby-reseau");
        t.setDaemon(true);
        t.start();
    }
}
