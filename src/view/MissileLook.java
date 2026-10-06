package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import model.Missille;

import java.util.Random;

/*
 * Un missile :
 *   - dans le stock : une bombe
 *   - quand il arrive sur une grille : la bombe tombe (elle rétrécit en tournant), puis l'explosion (touché)
 *     ou le plouf (dans l'eau) apparaît et reste sur la case pour marquer le tir.
 * Sans images, on garde les ronds : orange = dans l'eau, rouge = touché.
 */
public class MissileLook extends ElementLook {

    // taille d'affichage des images (une case fait 50 px)
    private static final int TAILLE = 48;
    private static final double ECHELLE_STOCK = 0.85;
    private static final Random HASARD = new Random();

    private Circle circle;
    private ImageView vue;
    private int radius;
    // l'impact a déjà été affiché (animation jouée ou en cours)
    private boolean impactAffiche;
    private Timeline animation;


    public MissileLook( int radius, GameElement element) {
        super(element);

        this.radius = radius;
        render();

    }

    @Override
    public void onSelectionChange() {
        Missille missille = (Missille)getElement();
        if (missille.isSelected()) {
            circle.setStrokeWidth(3);
            circle.setStrokeMiterLimit(10);
            circle.setStrokeType(StrokeType.CENTERED);
            circle.setStroke(Color.valueOf("0x333333"));
        }
        else {
            circle.setStrokeWidth(0);
        }
    }

    // appelé quand le missile change (tir réussi, arrivée sur la grille)
    @Override
    public void onFaceChange() {
        majApparence(true);
    }

    protected void render() {
        // render() peut être appelé plusieurs fois (constructeur puis framework) : on ne crée les formes qu'une fois
        if (circle == null) {
            vue = new ImageView();
            vue.setFitWidth(TAILLE);
            vue.setFitHeight(TAILLE);
            vue.setSmooth(true);
            vue.setX(-TAILLE / 2.0);
            vue.setY(-TAILLE / 2.0);
            getGroup().getChildren().add(vue);
            circle = new Circle();
            addShape(circle);
        }
        circle.setRadius(radius);
        majApparence(false);
    }

    private void majApparence(boolean animer) {
        Missille missille = (Missille) element;
        boolean touche = missille.getColor() == 2;
        Image impact = Sprites.charger(touche ? Sprites.EXPLOSION : Sprites.PLOUF);
        Image bombe = Sprites.charger(Sprites.BOMBE);

        if (impact == null || bombe == null) {
            // pas d'images : les ronds d'origine
            vue.setImage(null);
            circle.setFill(touche ? Color.RED : Color.ORANGE);
            return;
        }
        circle.setFill(Color.TRANSPARENT);

        if (!missille.isTire()) {
            // dans le stock
            vue.setImage(bombe);
            vue.setScaleX(ECHELLE_STOCK);
            vue.setScaleY(ECHELLE_STOCK);
            vue.setRotate(0);
            vue.setOpacity(1);
        } else if (!impactAffiche) {
            impactAffiche = true;
            if (animer) {
                jouerImpact(bombe, impact, touche);
            } else {
                afficherImpact(impact, touche, HASARD.nextInt(360));
            }
        } else if (animation == null) {
            // déjà tombé : on met juste l'image à jour (sans relancer l'animation)
            vue.setImage(impact);
        }
    }

    // position finale : l'explosion ou le plouf, tourné au hasard pour que les cases ne se ressemblent pas toutes
    private void afficherImpact(Image impact, boolean touche, double angle) {
        vue.setImage(impact);
        vue.setScaleX(1);
        vue.setScaleY(1);
        vue.setRotate(angle);
        vue.setOpacity(touche ? 1 : 0.85);
    }

    /*
     * La bombe tombe : vue du ciel, elle rétrécit en tournant (0,26 s),
     * puis l'impact jaillit, dépasse un peu sa taille et se pose (0,42 s).
     */
    private void jouerImpact(Image bombe, Image impact, boolean touche) {
        double angle = HASARD.nextInt(360);
        vue.setImage(bombe);
        vue.setOpacity(1);
        animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(vue.scaleXProperty(), 1.7), new KeyValue(vue.scaleYProperty(), 1.7),
                        new KeyValue(vue.rotateProperty(), 0)),
                new KeyFrame(Duration.millis(260), e -> {
                            vue.setImage(impact);
                            vue.setRotate(angle);
                        },
                        new KeyValue(vue.scaleXProperty(), 0.35, Interpolator.EASE_IN),
                        new KeyValue(vue.scaleYProperty(), 0.35, Interpolator.EASE_IN),
                        new KeyValue(vue.rotateProperty(), 220, Interpolator.EASE_IN)),
                new KeyFrame(Duration.millis(480),
                        new KeyValue(vue.scaleXProperty(), 1.25, Interpolator.EASE_OUT),
                        new KeyValue(vue.scaleYProperty(), 1.25, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(680),
                        new KeyValue(vue.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(vue.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(vue.opacityProperty(), touche ? 1 : 0.85)));
        animation.setOnFinished(e -> {
            animation = null;
            afficherImpact(impact, touche, angle);
        });
        animation.play();
    }
}
