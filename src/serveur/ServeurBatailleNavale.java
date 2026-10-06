package serveur;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import reseau.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Serveur de la bataille navale en ligne : lobby + relais des messages entre les 2 joueurs.
 * Il ne contient aucune règle du jeu : chaque joueur garde sa flotte, le serveur se contente
 * de transmettre les messages (tirs, résultats...) d'un joueur à l'autre.
 *
 * API (JSON) :
 *   POST   /games                 {pseudo, mode, missiles, premier}  -> {id, token, premier}
 *   GET    /games                 -> {parties: [{id, nom, pseudo, mode, missiles}]}  (parties en attente)
 *   POST   /games/{id}/join       {pseudo}  -> {token, mode, missiles, premier, pseudoAdversaire}
 *   POST   /games/{id}/messages   (en-tête X-Token) message -> transmis à l'adversaire
 *   GET    /games/{id}/messages?apres=N   (en-tête X-Token) -> {messages, suivant, fermee}
 *          attend jusqu'à 20 s qu'un message arrive (long-polling)
 *   DELETE /games/{id}            (en-tête X-Token) quitter la partie
 *
 * Lancement : java serveur.ServeurBatailleNavale [port]   (sinon variable PORT, sinon 8765)
 */
public class ServeurBatailleNavale {

    static final long DELAI_ATTENTE_MS = 20_000;   // durée max d'une requête de long-polling
    static final long DELAI_INACTIF_MS = 45_000;   // joueur considéré parti s'il n'a rien demandé depuis
    static final int MAX_PARTIES = 200;
    static final int TAILLE_MAX_CORPS = 16 * 1024;

    private final Map<String, Partie> parties = new ConcurrentHashMap<>();
    private final SecureRandom aleatoire = new SecureRandom();
    private HttpServer serveur;

    public static void main(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0])
                : Integer.parseInt(System.getenv().getOrDefault("PORT", "8765"));
        new ServeurBatailleNavale().demarrer(port);
        System.out.println("Serveur bataille navale démarré sur le port " + port);
    }

    public void demarrer(int port) throws IOException {
        serveur = HttpServer.create(new InetSocketAddress(port), 0);
        serveur.createContext("/games", this::traiter);
        // un thread virtuel par requête : les requêtes de long-polling restent en attente sans coûter cher
        serveur.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        serveur.start();
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nettoyage-parties");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::nettoyer, 5, 5, TimeUnit.SECONDS);
    }

    public void arreter() {
        if (serveur != null) serveur.stop(0);
    }


    // ================================================================ une partie

    static final class Partie {
        final String id;
        final int mode;
        final int missiles;   // -1 = valeur par défaut du mode
        final int premier;    // joueur qui tire en premier : 0 = hôte (joueur 1), 1 = invité (joueur 2)
        final String[] pseudos = new String[2];
        final String[] tokens = new String[2];
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>>[] boites = new List[]{new ArrayList<>(), new ArrayList<>()};
        final long[] dernierContact = new long[2];
        boolean fermee;

        Partie(String id, int mode, int missiles, int premier) {
            this.id = id;
            this.mode = mode;
            this.missiles = missiles;
            this.premier = premier;
        }

        int joueurDuToken(String token) {
            if (token == null) return -1;
            for (int j = 0; j < 2; j++) {
                if (token.equals(tokens[j])) return j;
            }
            return -1;
        }

        boolean enAttente() {
            return !fermee && tokens[1] == null;
        }

        // à appeler en tenant le verrou de la partie
        void deposer(int destinataire, Map<String, Object> message) {
            boites[destinataire].add(message);
            notifyAll();
        }

        // le joueur j quitte la partie : l'autre est prévenu
        void fermer(int j) {
            if (fermee) return;
            fermee = true;
            int autre = 1 - j;
            if (tokens[autre] != null) deposer(autre, Json.objet("type", "ADVERSAIRE_PARTI"));
            notifyAll();
        }
    }


    // ================================================================ routage

    private void traiter(HttpExchange ex) throws IOException {
        try {
            String chemin = ex.getRequestURI().getPath().substring("/games".length());
            String[] morceaux = chemin.split("/");
            // "" -> [""], "/abc/join" -> ["", "abc", "join"]
            String methode = ex.getRequestMethod();
            if (morceaux.length <= 1) {
                if (methode.equals("GET")) lister(ex);
                else if (methode.equals("POST")) creer(ex);
                else repondre(ex, 405, Json.objet("erreur", "méthode non autorisée"));
                return;
            }
            Partie partie = parties.get(morceaux[1]);
            if (partie == null) {
                repondre(ex, 404, Json.objet("erreur", "partie introuvable"));
                return;
            }
            String action = (morceaux.length > 2) ? morceaux[2] : "";
            if (action.isEmpty() && methode.equals("DELETE")) quitter(ex, partie);
            else if (action.equals("join") && methode.equals("POST")) rejoindre(ex, partie);
            else if (action.equals("messages") && methode.equals("POST")) envoyer(ex, partie);
            else if (action.equals("messages") && methode.equals("GET")) recevoir(ex, partie);
            else repondre(ex, 404, Json.objet("erreur", "route inconnue"));
        } catch (IllegalArgumentException e) {
            repondre(ex, 400, Json.objet("erreur", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            repondre(ex, 500, Json.objet("erreur", "erreur interne"));
        } finally {
            ex.close();
        }
    }


    // ================================================================ lobby

    private void lister(HttpExchange ex) throws IOException {
        List<Object> liste = new ArrayList<>();
        for (Partie p : parties.values()) {
            synchronized (p) {
                if (p.enAttente()) {
                    liste.add(Json.objet("id", p.id, "nom", "Partie de " + p.pseudos[0], "pseudo", p.pseudos[0],
                            "mode", p.mode, "missiles", p.missiles));
                }
            }
        }
        repondre(ex, 200, Json.objet("parties", liste));
    }

    private void creer(HttpExchange ex) throws IOException {
        Map<String, Object> corps = lireCorps(ex);
        if (parties.size() >= MAX_PARTIES) {
            repondre(ex, 503, Json.objet("erreur", "trop de parties en cours, réessayez plus tard"));
            return;
        }
        String pseudo = pseudo(corps);
        int mode = Json.entier(corps, "mode", 0);
        int missiles = Json.entier(corps, "missiles", -1);
        int premier = Json.entier(corps, "premier", 0);
        if (mode != 0 && mode != 1) throw new IllegalArgumentException("mode invalide");
        if (missiles != -1 && (missiles < 1 || missiles > 100)) throw new IllegalArgumentException("nombre de missiles invalide (1 à 100)");
        if (premier < 0 || premier > 2) throw new IllegalArgumentException("premier joueur invalide");
        if (premier == 2) premier = aleatoire.nextInt(2);

        String id;
        do {
            id = Integer.toHexString(aleatoire.nextInt(0x10000000) + 0x10000000);
        } while (parties.containsKey(id));
        Partie p = new Partie(id, mode, missiles, premier);
        p.pseudos[0] = pseudo;
        p.tokens[0] = UUID.randomUUID().toString();
        p.dernierContact[0] = System.currentTimeMillis();
        parties.put(id, p);
        System.out.println("partie créée " + id + " par " + pseudo);
        repondre(ex, 201, Json.objet("id", id, "token", p.tokens[0], "premier", premier));
    }

    private void rejoindre(HttpExchange ex, Partie p) throws IOException {
        String pseudo = pseudo(lireCorps(ex));
        Map<String, Object> reponse;
        synchronized (p) {
            if (!p.enAttente()) {
                repondre(ex, 409, Json.objet("erreur", "la partie n'est plus disponible"));
                return;
            }
            p.pseudos[1] = pseudo;
            p.tokens[1] = UUID.randomUUID().toString();
            p.dernierContact[1] = System.currentTimeMillis();
            p.deposer(0, Json.objet("type", "REJOINT", "pseudo", pseudo));
            reponse = Json.objet("token", p.tokens[1], "mode", p.mode, "missiles", p.missiles,
                    "premier", p.premier, "pseudoAdversaire", p.pseudos[0]);
        }
        System.out.println(pseudo + " a rejoint la partie " + p.id);
        repondre(ex, 200, reponse);
    }

    private void quitter(HttpExchange ex, Partie p) throws IOException {
        int j = joueur(ex, p);
        if (j < 0) return;
        synchronized (p) {
            p.fermer(j);
            if (p.tokens[1 - j] == null) parties.remove(p.id); // personne d'autre : on supprime tout de suite
        }
        repondre(ex, 200, Json.objet());
    }


    // ================================================================ relais des messages

    private void envoyer(HttpExchange ex, Partie p) throws IOException {
        int j = joueur(ex, p);
        if (j < 0) return;
        Map<String, Object> message = lireCorps(ex);
        if (!(message.get("type") instanceof String)) throw new IllegalArgumentException("message sans type");
        synchronized (p) {
            p.dernierContact[j] = System.currentTimeMillis();
            if (p.fermee) {
                repondre(ex, 410, Json.objet("erreur", "partie terminée"));
                return;
            }
            if (p.tokens[1 - j] == null) {
                repondre(ex, 409, Json.objet("erreur", "pas encore d'adversaire"));
                return;
            }
            p.deposer(1 - j, message);
        }
        repondre(ex, 200, Json.objet());
    }

    private void recevoir(HttpExchange ex, Partie p) throws IOException, InterruptedException {
        int j = joueur(ex, p);
        if (j < 0) return;
        int apres = 0;
        String requete = ex.getRequestURI().getQuery();
        if (requete != null && requete.startsWith("apres=")) {
            try {
                apres = Math.max(0, Integer.parseInt(requete.substring(6)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("paramètre apres invalide");
            }
        }
        Map<String, Object> reponse;
        synchronized (p) {
            p.dernierContact[j] = System.currentTimeMillis();
            long fin = System.currentTimeMillis() + DELAI_ATTENTE_MS;
            List<Map<String, Object>> boite = p.boites[j];
            while (boite.size() <= apres && !p.fermee) {
                long reste = fin - System.currentTimeMillis();
                if (reste <= 0) break;
                p.wait(reste);
            }
            p.dernierContact[j] = System.currentTimeMillis();
            List<Object> nouveaux = new ArrayList<>(boite.subList(Math.min(apres, boite.size()), boite.size()));
            reponse = Json.objet("messages", nouveaux, "suivant", boite.size(), "fermee", p.fermee);
        }
        repondre(ex, 200, reponse);
    }


    // ================================================================ nettoyage

    // les joueurs qui ne donnent plus de nouvelles (jeu fermé, plantage, coupure réseau) sont considérés partis
    private void nettoyer() {
        long maintenant = System.currentTimeMillis();
        for (Partie p : parties.values()) {
            synchronized (p) {
                for (int j = 0; j < 2; j++) {
                    if (p.tokens[j] != null && !p.fermee && maintenant - p.dernierContact[j] > DELAI_INACTIF_MS) {
                        System.out.println("joueur " + (j + 1) + " inactif, partie " + p.id + " fermée");
                        p.fermer(j);
                    }
                }
                boolean plusPersonne = true;
                for (int j = 0; j < 2; j++) {
                    if (p.tokens[j] != null && maintenant - p.dernierContact[j] < DELAI_INACTIF_MS) plusPersonne = false;
                }
                if (p.fermee && plusPersonne) parties.remove(p.id);
            }
        }
    }


    // ================================================================ outils

    private int joueur(HttpExchange ex, Partie p) throws IOException {
        int j;
        synchronized (p) {
            j = p.joueurDuToken(ex.getRequestHeaders().getFirst("X-Token"));
        }
        if (j < 0) repondre(ex, 403, Json.objet("erreur", "token invalide"));
        return j;
    }

    private static String pseudo(Map<String, Object> corps) {
        String pseudo = Json.texte(corps, "pseudo", "").trim();
        if (pseudo.isEmpty()) pseudo = "Joueur";
        if (pseudo.length() > 20) pseudo = pseudo.substring(0, 20);
        return pseudo;
    }

    private static Map<String, Object> lireCorps(HttpExchange ex) throws IOException {
        InputStream in = ex.getRequestBody();
        byte[] octets = in.readNBytes(TAILLE_MAX_CORPS + 1);
        if (octets.length > TAILLE_MAX_CORPS) throw new IllegalArgumentException("requête trop grosse");
        String texte = new String(octets, StandardCharsets.UTF_8).trim();
        if (texte.isEmpty()) return Json.objet();
        return Json.lireObjet(texte);
    }

    private static void repondre(HttpExchange ex, int code, Object json) throws IOException {
        byte[] octets = Json.ecrire(json).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, octets.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(octets);
        }
    }
}
