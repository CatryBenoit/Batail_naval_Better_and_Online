package control;

import boardifier.control.Controller;
import boardifier.control.ControllerAction;
import boardifier.model.GameException;
import boardifier.model.Model;
import boardifier.view.View;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import view.ShipView;

public class ControllerBattleShipAction extends ControllerAction implements EventHandler<ActionEvent> {

    private ShipView shipView;

    public ControllerBattleShipAction(Model model, View view, Controller control) {
        super(model, view, control);
        shipView = (ShipView) view;
        setMenuHandlers();

    }


    private void setMenuHandlers() {

        // set event handler on the MenuStart item
        shipView.getMenuStart().setOnAction(e -> {
            try {
                control.startGame();
            }
            catch(GameException err) {
                System.out.println("v");
                System.err.println(err.getMessage());
                System.exit(1);
            }
        });
        // set event handler on the MenuIntro item : retour à la page d'accueil
        shipView.getMenuIntro().setOnAction(e -> {
            control.stopGame();
            shipView.resetView();
            ((BattleShipControler) control).retourAccueil();
        });
        // set event handler on the MenuQuit item
        shipView.getMenuQuit().setOnAction(e -> {
            System.exit(0);
        });
    }
}
