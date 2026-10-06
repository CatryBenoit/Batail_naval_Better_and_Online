package TestReseau;

import org.junit.jupiter.api.Test;
import reseau.Json;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUnitTest {

    @Test
    public void testAllerRetour() {
        Map<String, Object> m = Json.objet("type", "RESULTAT", "ligne", 3, "touche", true, "rien", null,
                "cases", List.of(new int[]{1, 2}, new int[]{1, 3}), "texte", "a \"b\" \\ é\n");
        Map<String, Object> lu = Json.lireObjet(Json.ecrire(m));
        assertEquals("RESULTAT", Json.texte(lu, "type", ""));
        assertEquals(3, Json.entier(lu, "ligne", -1));
        assertTrue(Json.booleen(lu, "touche"));
        assertTrue(lu.containsKey("rien"));
        assertNull(lu.get("rien"));
        assertEquals("a \"b\" \\ é\n", Json.texte(lu, "texte", ""));
        List<?> cases = (List<?>) lu.get("cases");
        assertEquals(2, cases.size());
        assertEquals(List.of(1L, 3L), cases.get(1));
    }

    @Test
    public void testLectureDivers() {
        Map<String, Object> lu = Json.lireObjet(" { \"a\" : [ ] , \"b\" : { } , \"c\" : -1.5e2 , \"d\" : \"\\u0041\" } ");
        assertEquals(List.of(), lu.get("a"));
        assertEquals(Map.of(), lu.get("b"));
        assertEquals(-150.0, lu.get("c"));
        assertEquals("A", lu.get("d"));
    }

    @Test
    public void testValeursParDefaut() {
        Map<String, Object> m = Json.objet("n", "pas un nombre");
        assertEquals(7, Json.entier(m, "n", 7));
        assertEquals(7, Json.entier(m, "absent", 7));
        assertEquals("x", Json.texte(m, "absent", "x"));
        assertFalse(Json.booleen(m, "absent"));
    }

    @Test
    public void testJsonInvalide() {
        assertThrows(IllegalArgumentException.class, () -> Json.lire("{\"a\": }"));
        assertThrows(IllegalArgumentException.class, () -> Json.lire("{\"a\": 1"));
        assertThrows(IllegalArgumentException.class, () -> Json.lire("[1, 2] en trop"));
        assertThrows(IllegalArgumentException.class, () -> Json.lireObjet("[1, 2]"));
        assertThrows(IllegalArgumentException.class, () -> Json.lire("\"pas fini"));
    }
}
