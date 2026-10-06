package view;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;

public class SelectionPage {
    private TextField missiles;
    private Button valid;
    private ComboBox<String> mode, j1, j2;
    private CheckBox modifier;
    private RadioButton sj1, sj2, srd;
    private ToggleGroup fp;

    public SelectionPage() {
        initWidget();
    }

    private void initWidget() {
        fp = new ToggleGroup();
        missiles = new TextField();
        modifier = new CheckBox("Yes");
        modifier.setSelected(false);
        valid = new Button("start");
        sj1=new RadioButton("Player 1 ");
        sj2=new RadioButton("Player 2 ");
        srd=new RadioButton("Random ");
        mode = new ComboBox(FXCollections.observableArrayList(new String[]{"mode 1","mode2"}));
        mode.setValue("mode 1");
        j1 = new ComboBox(FXCollections.observableArrayList(new String[]{"human", "easy AI", "difficult AI"}));
        j1.setValue("human");
        j2 = new ComboBox(FXCollections.observableArrayList(new String[]{"human", "easy AI", "difficult AI"}));
        j2.setValue("human");
        sj1.setSelected(true);
        ToggleGroup group = new ToggleGroup();
        sj1.setToggleGroup(group);
        sj2.setToggleGroup(group);
        srd.setToggleGroup(group);

    }

    public void placeWidgets(ShipRootPane root) {
        root.setStyle("-fx-alignment: center");
        Label l = new Label("");
        //select mode de jeux:
        HBox hboxmode = new HBox();
        Label labelmode = new Label("Game Mode ");
        hboxmode.getChildren().addAll(labelmode,mode);
        mode.setStyle("-fx-text-fill: #F0FFF0;"+"-fx-font-size: 15 ;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;");
        hboxmode.setSpacing(20);
        labelmode.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);

        //bot ou humain
        VBox vboxj1 = new VBox();
        VBox vboxj2 = new VBox();
        vboxj1.setSpacing(7);
        vboxj2.setSpacing(7);
        VBox vboxt = new VBox();
        Label labelIAoujoueur = new Label("Player or AI");
        Label labelj1 = new Label("Player 1");
        Label labelj2 = new Label("Player 2");
        labelIAoujoueur.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        labelj1.setStyle("-fx-font-size: 18;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        labelj2.setStyle("-fx-font-size: 18;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        vboxj1.getChildren().addAll(labelj1,j1);
        vboxj2.getChildren().addAll(labelj2,j2);
        j1.setPrefSize(150,33);
        j2.setPrefSize(150,33);
        j1.setStyle("-fx-text-fill: #F0FFF0;"+"-fx-font-size: 15 ;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;" + "-fx-text-fill: Black;");
        j2.setStyle("-fx-text-fill: #F0FFF0;"+"-fx-font-size: 15 ;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;" +  "-fx-text-fill: Black;");

        HBox hboxselect = new HBox();
        hboxselect.getChildren().addAll(vboxj1,vboxj2);
        vboxt.getChildren().addAll(labelIAoujoueur,hboxselect);
        hboxselect.setSpacing(20);
        vboxt.setSpacing(10);


        //qui joue en premier
        Label labelfirstplayer = new Label("first player");
        labelfirstplayer.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        HBox hboxfirstplayer = new HBox();
        VBox vselcter = new VBox();
        vselcter.getChildren().addAll(sj1,sj2,srd);

        hboxfirstplayer.getChildren().addAll(labelfirstplayer,vselcter);
        sj1.setStyle("-fx-font-size: 16;" + "-fx-font-weight: bold;" + "-fx-text-fill: #F0FFF0" + Styles.OMBRE);
        sj2.setStyle("-fx-font-size: 16;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        srd.setStyle("-fx-font-size: 16;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        hboxfirstplayer.setSpacing(20);
        vselcter.setSpacing(10);

        //missile
        Label missile = new Label("modify the number of missiles");
        missile.setStyle("-fx-font-size: 20;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        missiles.setStyle("-fx-text-fill: #F0FFF0;"+"-fx-font-size: 15 ;" + "-fx-background-color: lightgrey;" + "-fx-font-weight: bold;" + "-fx-text-fill: Black;");
        missiles.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                missiles.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        HBox hboxmissile = new HBox();
        modifier.setStyle("-fx-font-size: 18;" + "-fx-font-weight: bold;"+ "-fx-text-fill: #F0FFF0;" + Styles.OMBRE);
        hboxmissile.getChildren().addAll(missile,modifier,missiles);
        hboxmissile.setSpacing(20);

        FlowPane flowpane = new FlowPane();
        flowpane.getChildren().addAll(valid);
        flowpane.setAlignment(Pos.CENTER);
        valid.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-background-color: lightgrey;" +
                        "-fx-font-weight: bold;" + "-fx-text-fill: Black;"
        );

        VBox vboxtotal = new VBox();
        vboxtotal.getChildren().addAll(l,hboxmode,vboxt,hboxfirstplayer,hboxmissile,flowpane);
        vboxtotal.setMinSize(900,600);
        vboxtotal.setSpacing(50);
        vboxtotal.setStyle("-fx-padding: 0px 0px 0px 15px;");

        root.getChildren().add(Styles.avecFond(vboxtotal, "/Images/background/imageoptionmenu.png"));
    }

    public void setButtonListener(EventHandler<ActionEvent> handler) {
        valid.setOnAction(handler);
        sj1.setOnAction(handler);
        srd.setOnAction(handler);
        sj2.setOnAction(handler);
        j1.setOnAction(handler);
        j2.setOnAction(handler);
        mode.setOnAction(handler);
        modifier.setOnAction(handler);
    }

    public void setTextListener(ChangeListener<String> listener) {
        missiles.textProperty().addListener(listener);
    }

    public ComboBox<String> getJ1() {
        return j1;
    }

    public ComboBox<String> getJ2() {
        return j2;
    }

    public CheckBox getModifier() {
        return modifier;
    }

    public ComboBox<String> getMode() {
        return mode;
    }

    public RadioButton getSj1() {
        return sj1;
    }

    public RadioButton getSj2() {
        return sj2;
    }

    public RadioButton getSrd() {
        return srd;
    }

    public TextField getMissiles() {
        return missiles;
    }

    public Button getValid() {
        return valid;
    }
}
