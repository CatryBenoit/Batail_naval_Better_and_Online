package reseau;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/*
 * Client de l'API du serveur (voir serveur.ServeurBatailleNavale).
 * Les méthodes du lobby sont bloquantes : à appeler hors du thread JavaFX.
 * Les messages reçus sont donnés au listener sur le thread d'écoute (pas le thread JavaFX).
 */
public class ClientReseau {

    private static final int MAX_ECHECS = 3;

    // parties en cours : si le jeu est fermé brutalement (menu Quit, fenêtre fermée),
    // on prévient quand même le serveur pour que l'adversaire ne reste pas bloqué
    private static final Set<ClientReseau> OUVERTS = ConcurrentHashMap.newKeySet();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ClientReseau c : OUVERTS) c.envoyerDepart();
        }));
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String urlServeur;
    // les envois se font dans l'ordre, un par un, sans bloquer l'appelant
    private final ExecutorService envois = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "envoi-reseau");
        t.setDaemon(true);
        return t;
    });

    private String idPartie;
    private String token;
    private volatile boolean actif;
    private Consumer<String> surFin = s -> { };

    public ClientReseau(String urlServeur) {
        String url = urlServeur.trim();
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        this.urlServeur = url;
    }

    public String getIdPartie() {
        return idPartie;
    }


    // ================================================================ lobby

    // crée une partie ; renvoie la réponse du serveur ({id, token, premier})
    public Map<String, Object> creerPartie(String pseudo, int mode, int missiles, int premier) throws IOException {
        Map<String, Object> r = requete("POST", "/games",
                Json.objet("pseudo", pseudo, "mode", mode, "missiles", missiles, "premier", premier), 10);
        idPartie = Json.texte(r, "id", null);
        token = Json.texte(r, "token", null);
        OUVERTS.add(this);
        return r;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listerParties() throws IOException {
        Object parties = requete("GET", "/games", null, 10).get("parties");
        List<Map<String, Object>> liste = new ArrayList<>();
        if (parties instanceof List) {
            for (Object o : (List<Object>) parties) {
                if (o instanceof Map) liste.add((Map<String, Object>) o);
            }
        }
        return liste;
    }

    // rejoint une partie ; renvoie la configuration ({token, mode, missiles, premier, pseudoAdversaire})
    public Map<String, Object> rejoindre(String id, String pseudo) throws IOException {
        Map<String, Object> r = requete("POST", "/games/" + id + "/join", Json.objet("pseudo", pseudo), 10);
        idPartie = id;
        token = Json.texte(r, "token", null);
        OUVERTS.add(this);
        return r;
    }


    // ================================================================ partie

    /*
     * Démarre l'écoute des messages de l'adversaire (long-polling) dans un thread.
     * surMessage : appelé pour chaque message reçu
     * surFin : appelé une seule fois si la connexion est perdue ou la partie fermée par le serveur
     */
    public void ecouter(Consumer<Map<String, Object>> surMessage, Consumer<String> surFin) {
        this.surFin = surFin;
        actif = true;
        Thread ecoute = new Thread(() -> boucleEcoute(surMessage), "ecoute-reseau");
        ecoute.setDaemon(true);
        ecoute.start();
    }

    @SuppressWarnings("unchecked")
    private void boucleEcoute(Consumer<Map<String, Object>> surMessage) {
        int suivant = 0;
        int echecs = 0;
        while (actif) {
            try {
                Map<String, Object> r = requete("GET", "/games/" + idPartie + "/messages?apres=" + suivant, null, 30);
                echecs = 0;
                Object messages = r.get("messages");
                if (messages instanceof List) {
                    for (Object m : (List<Object>) messages) {
                        if (actif && m instanceof Map) surMessage.accept((Map<String, Object>) m);
                    }
                }
                suivant = Json.entier(r, "suivant", suivant);
                if (Json.booleen(r, "fermee")) {
                    terminer("La partie a été fermée.");
                    return;
                }
            } catch (IOException e) {
                echecs++;
                if (echecs >= MAX_ECHECS) {
                    terminer("Connexion au serveur perdue (" + e.getMessage() + ").");
                    return;
                }
                attendre(2000);
            } catch (Exception e) {
                // message illisible : on l'ignore plutôt que d'arrêter la partie
                e.printStackTrace();
                attendre(1000);
            }
        }
    }

    // envoie un message à l'adversaire (sans bloquer)
    public void envoyer(Map<String, Object> message) {
        envois.submit(() -> {
            try {
                requete("POST", "/games/" + idPartie + "/messages", message, 10);
            } catch (IOException e) {
                terminer("Impossible d'envoyer un message au serveur (" + e.getMessage() + ").");
            }
        });
    }

    // quitte la partie (sans bloquer) ; l'adversaire est prévenu par le serveur
    public void quitter() {
        actif = false;
        if (idPartie == null || token == null) return;
        envois.submit(this::envoyerDepart);
    }

    private void envoyerDepart() {
        if (!OUVERTS.remove(this)) return; // déjà fait
        try {
            requete("DELETE", "/games/" + idPartie, null, 3);
        } catch (IOException e) {
            // tant pis : le serveur fermera la partie tout seul après un délai d'inactivité
        }
    }

    private synchronized void terminer(String raison) {
        if (!actif) return;
        actif = false;
        surFin.accept(raison);
    }


    // ================================================================ HTTP

    private Map<String, Object> requete(String methode, String chemin, Map<String, Object> corps, int timeoutSecondes) throws IOException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(urlServeur + chemin))
                .timeout(Duration.ofSeconds(timeoutSecondes))
                .header("Content-Type", "application/json");
        if (token != null) b.header("X-Token", token);
        HttpRequest.BodyPublisher contenu = (corps == null) ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(Json.ecrire(corps), StandardCharsets.UTF_8);
        b.method(methode, contenu);
        HttpResponse<String> reponse;
        try {
            reponse = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("requête interrompue");
        } catch (IllegalArgumentException e) {
            throw new IOException("adresse du serveur invalide");
        } catch (java.net.ConnectException e) {
            throw new IOException("serveur injoignable");
        } catch (java.net.http.HttpTimeoutException e) {
            throw new IOException("le serveur ne répond pas");
        }
        Map<String, Object> json;
        try {
            json = reponse.body().isBlank() ? Json.objet() : Json.lireObjet(reponse.body());
        } catch (IllegalArgumentException e) {
            throw new IOException("réponse du serveur illisible (code " + reponse.statusCode() + ")");
        }
        if (reponse.statusCode() >= 400) {
            throw new IOException(Json.texte(json, "erreur", "erreur " + reponse.statusCode()));
        }
        return json;
    }

    private static void attendre(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
