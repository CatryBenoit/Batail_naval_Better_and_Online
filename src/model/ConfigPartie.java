package model;

/*
 * Configuration de la prochaine partie, remplie avant de la lancer :
 *   - par la page de sélection pour une partie locale (PageControl.play)
 *   - par le lobby pour une partie en ligne (avec la configuration choisie par l'hôte)
 */
public final class ConfigPartie {

    // 0 = mode 1 (5 bateaux), 1 = mode 2 (10 bateaux)
    public static int mode = 0;
    // nombre de missiles par joueur, -1 = valeur par défaut du mode
    public static int nbMissiles = -1;
    // joueur qui tire en premier : 0 = joueur 1, 1 = joueur 2, 2 = au hasard
    public static int premierTireur = 0;
    // pour chaque joueur : 0 = humain, 1 = IA facile, 2 = IA difficile
    public static int[] niveauIA = {0, 0};

    private ConfigPartie() { }
}
