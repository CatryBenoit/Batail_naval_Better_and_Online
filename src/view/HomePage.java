package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomePage {
    private Button start;
    private Button online;
    private Button quit;
    private Button options;
    static Label gameName;

    public HomePage() {
        initWidgets();
    }

    private void initWidgets() {
        start = new Button("Start Game");
        online = new Button("Play Online");
        quit = new Button("Quit Game");
        options = new Button("Options");
        gameName = new Label("Battle Ship");
    }

    public void placeWidgets(ShipRootPane root) {
        FlowPane fp0 = new FlowPane();
        fp0.setAlignment(Pos.CENTER);
        gameName.setStyle(
                "-fx-font-size: 80;" +
                        "-fx-text-fill: #FFFFFF ;" +
                        "-fx-font-weight: bold;" + Styles.OMBRE
        );
        fp0.getChildren().add(gameName);

        FlowPane fp1 = new FlowPane();
        fp1.setAlignment(Pos.CENTER);
        fp1.getChildren().add(start);
        start.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-background-color: lightgrey;" +
                        "-fx-font-weight: bold;"
        );
        start.setPrefSize(350,46);


        FlowPane fpOnline = new FlowPane();
        fpOnline.setAlignment(Pos.CENTER);
        fpOnline.getChildren().add(online);
        online.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-background-color: lightgrey;" +
                        "-fx-font-weight: bold;"
        );
        online.setPrefSize(350,46);

        FlowPane fp2 = new FlowPane();
        fp2.setAlignment(Pos.CENTER);
        fp2.getChildren().add(options);
        options.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-background-color: lightgrey;" +
                        "-fx-font-weight: bold;"
        );

        options.setPrefSize(350,46);


        FlowPane fp3 = new FlowPane();
        fp3.setAlignment(Pos.CENTER);
        fp3.getChildren().add(quit);
        quit.setPrefSize(350,46);
        quit.setStyle(
                "-fx-font-size: 24;" +
                        "-fx-background-color: lightgrey;" +
                        "-fx-font-weight: bold;"
        );



        HBox h = new HBox();
        VBox v = new VBox();
        h.setStyle("-fx-background-image: url('/Images/background/imageBattelShip.png');" + "-fx-background-size: cover;" + "-fx-background-position: center center;");
        v.getChildren().addAll(fp0,fp1, fpOnline, fp2, fp3);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(35);
        h.getChildren().add(v);
        h.setAlignment(Pos.CENTER);
        h.setMinSize(900,600);


        root.getChildren().add(h);
    }

    public void setButtonListener(EventHandler<ActionEvent> handler) {
        start.setOnAction(handler);
        quit.setOnAction(handler);
        options.setOnAction(handler);
    }

    public Button getStart() {
        return start;
    }

    public Button getOnline() {
        return online;
    }

    public Button getQuit() {
        return quit;
    }

    public Button getOptions() {
        return options;
    }
}