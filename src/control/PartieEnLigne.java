package control;

import reseau.ClientReseau;
import reseau.Json;

import java.util.List;
import java.util.Map;

/*
 * Une partie en ligne contre un adversaire distant.
 *
 * Chaque joueur garde sa flotte pour lui : seuls les messages suivants passent par le serveur
 *   PRET                                        la flotte est placée
 *   TIR      {ligne, colonne}                   je tire sur cette case
 *   RESULTAT {ligne, colonne, touche, coule, cases}   réponse au tir (cases du bateau s'il est coulé)
 *   ADVERSAIRE_PARTI                            envoyé par le serveur quand l'autre joueur s'en va
 *
 * Toutes les méthodes sont appelées sur le thread JavaFX (le lobby y renvoie les messages reçus).
 */
public class PartieEnLigne {

    private final ClientReseau client;
    final int monId;          // 0 = hôte (joueur 1), 1 = invité (joueur 2)
    final int idAdversaire;
    final String pseudoAdversaire;
    private final BattleShipControler control;
    private final Runnable surTerminee;

    // j'ai tiré et j'attends la réponse de l'adversaire
    private boolean attenteResultat;
    private int[] dernierTir;
    // tir adverse reçu avant que ce soit vraiment son tour chez moi (mon dernier tir est encore en cours d'affichage)
    private Map<String, Object> tirEnAttente;
    // résultat affiché : les messages suivants (départ de l'adversaire...) sont ignorés
    private boolean finie;
    private boolean terminee;

    public PartieEnLigne(ClientReseau client, int monId, String pseudoAdversaire, BattleShipControler control, Runnable surTerminee) {
        this.client = client;
        this.monId = monId;
        this.idAdversaire = 1 - monId;
        this.pseudoAdversaire = pseudoAdversaire;
        this.control = control;
        this.surTerminee = surTerminee;
    }


    // ================================================================ envois

    void envoyerPret() {
        client.envoyer(Json.objet("type", "PRET"));
    }

    void tirer(int ligne, int colonne) {
        attenteResultat = true;
        dernierTir = new int[]{ligne, colonne};
        client.envoyer(Json.objet("type", "TIR", "ligne", ligne, "colonne", colonne));
    }

    boolean attendResultat() {
        return attenteResultat;
    }


    // ================================================================ réception

    public void recevoir(Map<String, Object> message) {
        if (terminee) return;
        try {
            switch (Json.texte(message, "type", "")) {
                case "PRET":
                    control.adversairePret();
                    break;
                case "TIR":
                    tirEnAttente = message;
                    traiterTirEnAttente();
                    break;
                case "RESULTAT":
                    recevoirResultat(message);
                    break;
                case "ADVERSAIRE_PARTI":
                    connexionPerdue(pseudoAdversaire + " a quitté la partie.");
                    break;
                default:
                    // message inconnu : ignoré
            }
        } catch (RuntimeException e) {
            // un message mal formé ne doit pas faire planter le jeu
            e.printStackTrace();
        }
    }

    // traite le tir adverse reçu, si c'est bien son tour (appelé aussi à chaque changement de tour)
    void traiterTirEnAttente() {
        if (tirEnAttente == null || !control.tourDeLAdversaire()) return;
        Map<String, Object> tir = tirEnAttente;
        tirEnAttente = null;
        Map<String, Object> resultat = control.recevoirTir(Json.entier(tir, "ligne", -1), Json.entier(tir, "colonne", -1));
        if (resultat != null) client.envoyer(resultat);
    }

    private void recevoirResultat(Map<String, Object> message) {
        int ligne = Json.entier(message, "ligne", -1);
        int colonne = Json.entier(message, "colonne", -1);
        if (!attenteResultat || ligne != dernierTir[0] || colonne != dernierTir[1]) return;
        attenteResultat = false;
        int[][] cases = null;
        if (Json.booleen(message, "coule") && message.get("cases") instanceof List) {
            List<?> liste = (List<?>) message.get("cases");
            cases = new int[liste.size()][];
            for (int i = 0; i < liste.size(); i++) {
                List<?> c = (List<?>) liste.get(i);
                cases[i] = new int[]{((Number) c.get(0)).intValue(), ((Number) c.get(1)).intValue()};
            }
        }
        control.appliquerResultat(ligne, colonne, Json.booleen(message, "touche"), cases);
    }


    // ================================================================ fin

    // le résultat de la partie est affiché : on n'interrompt plus rien
    void marquerFinie() {
        finie = true;
    }

    // connexion perdue ou adversaire parti
    public void connexionPerdue(String raison) {
        if (terminee || finie) return;
        quitter();
        control.partieInterrompue(raison);
    }

    // quitte la partie sur le serveur (l'adversaire est prévenu)
    void quitter() {
        if (terminee) return;
        terminee = true;
        client.quitter();
        surTerminee.run();
    }
}
