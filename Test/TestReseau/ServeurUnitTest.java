package TestReseau;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reseau.ClientReseau;
import reseau.Json;
import serveur.ServeurBatailleNavale;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// démarre un vrai serveur sur un port libre et le pilote avec deux clients
public class ServeurUnitTest {

    private ServeurBatailleNavale serveur;
    private String url;

    @BeforeEach
    public void setUp() throws IOException {
        int port;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        serveur = new ServeurBatailleNavale();
        serveur.demarrer(port);
        url = "localhost:" + port;
    }

    @AfterEach
    public void tearDown() {
        serveur.arreter();
    }

    @Test
    public void testCreerListerRejoindre() throws IOException {
        ClientReseau hote = new ClientReseau(url);
        Map<String, Object> creation = hote.creerPartie("Alice", 1, 40, 1);
        assertEquals(1, Json.entier(creation, "premier", -1));

        List<Map<String, Object>> parties = new ClientReseau(url).listerParties();
        assertEquals(1, parties.size());
        assertEquals("Partie de Alice", Json.texte(parties.get(0), "nom", ""));

        Map<String, Object> config = new ClientReseau(url).rejoindre(hote.getIdPartie(), "Bob");
        assertEquals(1, Json.entier(config, "mode", -1));
        assertEquals(40, Json.entier(config, "missiles", -1));
        assertEquals(1, Json.entier(config, "premier", -1));
        assertEquals("Alice", Json.texte(config, "pseudoAdversaire", ""));

        // la partie est complète : elle n'est plus listée et on ne peut plus la rejoindre
        assertTrue(new ClientReseau(url).listerParties().isEmpty());
        assertThrows(IOException.class, () -> new ClientReseau(url).rejoindre(hote.getIdPartie(), "Eve"));
    }

    @Test
    public void testRelaisDesMessagesEtDepart() throws Exception {
        ClientReseau hote = new ClientReseau(url);
        ClientReseau invite = new ClientReseau(url);
        hote.creerPartie("Alice", 0, -1, 0);
        BlockingQueue<Map<String, Object>> recusHote = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> recusInvite = new LinkedBlockingQueue<>();
        BlockingQueue<String> finInvite = new LinkedBlockingQueue<>();
        hote.ecouter(recusHote::add, raison -> { });
        invite.rejoindre(hote.getIdPartie(), "Bob");
        invite.ecouter(recusInvite::add, finInvite::add);

        Map<String, Object> m = recusHote.poll(5, TimeUnit.SECONDS);
        assertNotNull(m);
        assertEquals("REJOINT", Json.texte(m, "type", ""));
        assertEquals("Bob", Json.texte(m, "pseudo", ""));

        invite.envoyer(Json.objet("type", "TIR", "ligne", 3, "colonne", 4));
        m = recusHote.poll(5, TimeUnit.SECONDS);
        assertNotNull(m);
        assertEquals("TIR", Json.texte(m, "type", ""));
        assertEquals(4, Json.entier(m, "colonne", -1));

        hote.envoyer(Json.objet("type", "RESULTAT", "touche", true));
        m = recusInvite.poll(5, TimeUnit.SECONDS);
        assertNotNull(m);
        assertTrue(Json.booleen(m, "touche"));

        // l'hôte s'en va : l'invité est prévenu, puis son écoute s'arrête
        hote.quitter();
        m = recusInvite.poll(5, TimeUnit.SECONDS);
        assertNotNull(m);
        assertEquals("ADVERSAIRE_PARTI", Json.texte(m, "type", ""));
        assertNotNull(finInvite.poll(5, TimeUnit.SECONDS));
    }

    @Test
    public void testRequetesInvalides() throws IOException {
        assertThrows(IOException.class, () -> new ClientReseau(url).creerPartie("X", 5, -1, 0));    // mode
        assertThrows(IOException.class, () -> new ClientReseau(url).creerPartie("X", 0, 500, 0));   // missiles
        assertThrows(IOException.class, () -> new ClientReseau(url).rejoindre("inconnue", "X"));
        // serveur absent
        IOException e = assertThrows(IOException.class, () -> new ClientReseau("localhost:1").listerParties());
        assertEquals("serveur injoignable", e.getMessage());
    }

    @Test
    public void testAnnulerUnePartieEnAttente() throws IOException, InterruptedException {
        ClientReseau hote = new ClientReseau(url);
        hote.creerPartie("Alice", 0, -1, 0);
        assertEquals(1, new ClientReseau(url).listerParties().size());
        hote.quitter();
        // le départ est envoyé en arrière-plan
        for (int i = 0; i < 50 && !new ClientReseau(url).listerParties().isEmpty(); i++) Thread.sleep(50);
        assertTrue(new ClientReseau(url).listerParties().isEmpty());
    }
}
