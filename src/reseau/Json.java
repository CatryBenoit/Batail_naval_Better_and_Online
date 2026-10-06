package reseau;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Mini lecteur / écrivain JSON (pour ne dépendre d'aucune bibliothèque).
 * Objets -> Map<String,Object>, tableaux -> List<Object>, nombres -> Long ou Double,
 * plus String, Boolean et null.
 */
public final class Json {

    private Json() { }

    // ---------------------------------------------------------------- écriture

    public static String ecrire(Object valeur) {
        StringBuilder sb = new StringBuilder();
        ecrire(valeur, sb);
        return sb.toString();
    }

    private static void ecrire(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            ecrireTexte((String) v, sb);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map) {
            sb.append('{');
            boolean premier = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!premier) sb.append(',');
                premier = false;
                ecrireTexte(String.valueOf(e.getKey()), sb);
                sb.append(':');
                ecrire(e.getValue(), sb);
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            boolean premier = true;
            for (Object o : (Iterable<?>) v) {
                if (!premier) sb.append(',');
                premier = false;
                ecrire(o, sb);
            }
            sb.append(']');
        } else if (v instanceof int[]) {
            List<Object> liste = new ArrayList<>();
            for (int i : (int[]) v) liste.add(i);
            ecrire(liste, sb);
        } else {
            ecrireTexte(v.toString(), sb);
        }
    }

    private static void ecrireTexte(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    // ---------------------------------------------------------------- lecture

    public static Object lire(String texte) {
        Lecteur l = new Lecteur(texte);
        Object v = l.valeur();
        l.espaces();
        if (l.pos != texte.length()) throw new IllegalArgumentException("JSON invalide : texte en trop");
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> lireObjet(String texte) {
        Object v = lire(texte);
        if (!(v instanceof Map)) throw new IllegalArgumentException("JSON invalide : objet attendu");
        return (Map<String, Object>) v;
    }

    private static final class Lecteur {
        private final String s;
        private int pos;

        Lecteur(String s) { this.s = s; }

        void espaces() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        char suivant() {
            espaces();
            if (pos >= s.length()) throw new IllegalArgumentException("JSON invalide : fin inattendue");
            return s.charAt(pos);
        }

        void attendre(char c) {
            if (suivant() != c) throw new IllegalArgumentException("JSON invalide : '" + c + "' attendu en " + pos);
            pos++;
        }

        Object valeur() {
            char c = suivant();
            if (c == '{') return objet();
            if (c == '[') return tableau();
            if (c == '"') return texte();
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            return nombre();
        }

        Map<String, Object> objet() {
            Map<String, Object> m = new LinkedHashMap<>();
            attendre('{');
            if (suivant() == '}') { pos++; return m; }
            while (true) {
                if (suivant() != '"') throw new IllegalArgumentException("JSON invalide : clé attendue en " + pos);
                String cle = texte();
                attendre(':');
                m.put(cle, valeur());
                char c = suivant();
                pos++;
                if (c == '}') return m;
                if (c != ',') throw new IllegalArgumentException("JSON invalide : ',' ou '}' attendu en " + pos);
            }
        }

        List<Object> tableau() {
            List<Object> l = new ArrayList<>();
            attendre('[');
            if (suivant() == ']') { pos++; return l; }
            while (true) {
                l.add(valeur());
                char c = suivant();
                pos++;
                if (c == ']') return l;
                if (c != ',') throw new IllegalArgumentException("JSON invalide : ',' ou ']' attendu en " + pos);
            }
        }

        String texte() {
            attendre('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) throw new IllegalArgumentException("JSON invalide : texte non terminé");
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                if (pos >= s.length()) throw new IllegalArgumentException("JSON invalide : échappement");
                char e = s.charAt(pos++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > s.length()) throw new IllegalArgumentException("JSON invalide : \\u");
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: throw new IllegalArgumentException("JSON invalide : échappement \\" + e);
                }
            }
        }

        Object nombre() {
            int debut = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String n = s.substring(debut, pos);
            if (n.isEmpty()) throw new IllegalArgumentException("JSON invalide en " + debut);
            if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
            return Long.parseLong(n);
        }
    }

    // ---------------------------------------------------------------- aides

    // construit un objet : Json.objet("type", "TIR", "ligne", 3)
    public static Map<String, Object> objet(Object... clesValeurs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < clesValeurs.length; i += 2) m.put(String.valueOf(clesValeurs[i]), clesValeurs[i + 1]);
        return m;
    }

    public static int entier(Map<String, Object> m, String cle, int defaut) {
        Object v = m.get(cle);
        return (v instanceof Number) ? ((Number) v).intValue() : defaut;
    }

    public static String texte(Map<String, Object> m, String cle, String defaut) {
        Object v = m.get(cle);
        return (v instanceof String) ? (String) v : defaut;
    }

    public static boolean booleen(Map<String, Object> m, String cle) {
        return Boolean.TRUE.equals(m.get(cle));
    }
}
