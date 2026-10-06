package view;

import boardifier.model.ContainerElement;
import boardifier.view.ClassicBoardLook;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/*
 * Grille de jeu sur fond de mer : la texture de mer est répétée sous toutes les cases, sans coupure
 * d'une case à l'autre, et les lignes de la grille sont claires pour rester visibles sur l'eau.
 * Sans image de mer, on garde le damier d'origine.
 */
public class BoardMerLook extends ClassicBoardLook {

    // taille d'un motif de mer à l'écran (la texture se répète tous les 5 cases)
    private static final int TAILLE_MOTIF = 250;
    private static final Color LIGNES = Color.rgb(255, 255, 255, 0.35);

    public BoardMerLook(int cellSize, ContainerElement element, int depth, Color evenColor, Color oddColor,
                        int borderWidth, Color borderColor, int frameWidth, Color frameColor, boolean showCoords) {
        super(cellSize, element, depth, evenColor, oddColor, borderWidth, borderColor, frameWidth, frameColor, showCoords);
    }

    @Override
    protected void render() {
        super.render();
        Image mer = Sprites.charger(Sprites.MER);
        if (mer == null) return;
        // même motif pour toutes les cases, accroché au coin de la grille : la mer est continue
        ImagePattern motif = new ImagePattern(mer, gapXToCells, gapYToCells, TAILLE_MOTIF, TAILLE_MOTIF, false);
        for (Rectangle[] ligne : cells) {
            for (Rectangle cellule : ligne) {
                cellule.setFill(motif);
            }
        }
        dessinerLignes();
    }

    @Override
    public void onFaceChange() {
        super.onFaceChange();
        dessinerLignes();
    }

    private void dessinerLignes() {
        if (cells == null || Sprites.charger(Sprites.MER) == null) return;
        for (Rectangle[] ligne : cells) {
            for (Rectangle cellule : ligne) {
                cellule.setStroke(LIGNES);
                cellule.setStrokeWidth(1);
                cellule.setStrokeType(StrokeType.INSIDE);
            }
        }
    }
}
