package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import model.shipPart;



/*
 * Une partie de bateau : le morceau d'image du bateau qui correspond à cette case (Images/navires),
 * tourné d'un quart de tour quand le bateau est vertical. Si l'image manque, on affiche un rond.
 * Quand le bateau est coulé, l'image devient une épave sombre (sans image : un anneau vert).
 */
public class ShipPartLook extends ElementLook {

    // bateau coulé : image assombrie et grisée (épave)
    private static final ColorAdjust EPAVE = new ColorAdjust(0, -0.7, -0.55, 0);

    private ImageView vue;   // morceau d'image du bateau
    private ImageView marque; // petite explosion sur l'épave d'un bateau coulé
    private Circle circle;   // rond de secours (pas d'image) et anneau vert quand le bateau est coulé
    private int radius;

    public ShipPartLook(int radius,GameElement element) {
        super(element);
        this.radius = radius;
        render();
    }

    @Override
    public void onSelectionChange() {
        shipPart pawn = (shipPart)getElement();
        if (pawn.isSelected()) {
            circle.setStrokeWidth(3);
            circle.setStrokeMiterLimit(10);
            circle.setStrokeType(StrokeType.CENTERED);
            circle.setStroke(Color.valueOf("0x333333"));
        }
        else {
            circle.setStrokeWidth(0);
        }
    }

    // appelé quand la partie change d'aspect (bateau coulé, bateau posé à l'horizontale...)
    @Override
    public void onFaceChange() {
        majApparence();
    }

    public void render(){
        // render() peut être appelé plusieurs fois (constructeur puis framework) : on ne crée les formes qu'une fois
        if (circle == null) {
            vue = new ImageView();
            vue.setFitWidth(2 * radius);
            vue.setFitHeight(2 * radius);
            vue.setSmooth(true);
            // centrée sur (0, 0), comme le cercle
            vue.setX(-radius);
            vue.setY(-radius);
            getGroup().getChildren().add(vue);
            marque = new ImageView();
            marque.setFitWidth(1.2 * radius);
            marque.setFitHeight(1.2 * radius);
            marque.setX(-0.6 * radius);
            marque.setY(-0.6 * radius);
            getGroup().getChildren().add(marque);
            circle = new Circle();
            addShape(circle);
        }
        circle.setRadius(radius);
        majApparence();
    }

    private void majApparence(){
        shipPart shipPart = (shipPart)element;
        Image image = image(shipPart);
        vue.setImage(image);
        // les images sont dessinées à l'horizontale (poupe à gauche) : un quart de tour pour un bateau vertical
        vue.setRotate(shipPart.isVertical() ? 90 : 0);

        boolean coule = shipPart.getColor() == 2;
        vue.setEffect(coule ? EPAVE : null);

        circle.setStroke(null);
        circle.setRadius(radius);
        marque.setImage(null);
        if (coule && image != null) {
            // toutes les cases d'un bateau coulé ont été touchées : l'épave porte elle-même la marque de l'impact
            // (en ligne, le bateau adverse n'est révélé qu'à la fin et serait sinon dessiné par-dessus les missiles)
            Image explosion = Sprites.charger(Sprites.EXPLOSION);
            if (explosion != null) {
                marque.setImage(explosion);
                circle.setFill(Color.TRANSPARENT);
            } else {
                circle.setRadius(radius * 0.6);
                circle.setFill(Color.RED);
            }
        } else if (coule){ // COULER sans image : un anneau vert, pour voir le missile qui l'a touché (dessous ou dessus)
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.GREEN);
            circle.setStrokeWidth(7);
            circle.setStrokeType(StrokeType.INSIDE);
        } else if (image != null) {
            circle.setFill(Color.TRANSPARENT);
        } else if (shipPart.getIdplayer() == 1) {
            circle.setFill(Color.NAVY); // pas rouge : le rouge indique un missile qui a touché
        } else {
            circle.setFill(Color.BLACK);
        }
    }

    private static Image image(shipPart part) {
        if (part.getApparence() == null) return null;
        return Sprites.charger("/Images/navires/" + part.getApparence() + "_" + (part.getIndexDansBateau() + 1) + ".png");
    }
}
