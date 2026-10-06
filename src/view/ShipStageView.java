package view;

import boardifier.control.Logger;
import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.model.TextElement;
import boardifier.view.*;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.BattleShipStageModel;
import model.Ship;
import model.shipPart;


public class ShipStageView extends GameStageView {



    public ShipStageView(String name, GameStageModel gameStageModel){
        super(name,gameStageModel);
    }
    @Override
    public void createLooks(){
        BattleShipStageModel model = (BattleShipStageModel) gameStageModel;


        addLook(new BoardMerLook(50,model.getBoardPlayer1(),1,Color.WHEAT,Color.AQUA,5,Color.BLACK,5,Color.BLACK,true));


        addLook(new BoardMerLook(50,model.getBoardPlayer2(),1,Color.WHEAT,Color.AQUA,5,Color.BLACK,5,Color.BLACK,true));


        for (int i=0; i<model.getShipsPlayer1().length; i++){
            //System.out.println(i + "bateau");
            addLook(new Shiplook(50, 50,model.getShipsPlayer1()[i]));
            addLook(new Shiplook(50, 50,model.getShipsPlayer2()[i]));
        }


        //on ajoute les partie de bateau
        for (int i=0; i<model.getShipsPlayer1().length; i++){
            for (int j = 0; j < model.getShipsPlayer1()[i].shipParts.length; j++){
                addLook(new ShipPartLook(25,model.getShipsPlayer1()[i].getshippart()[j]));
                addLook(new ShipPartLook(25,model.getShipsPlayer2()[i].getshippart()[j]));
            }
        }

        /*for(int i=0; i < model.getMissileJoueur1().length; i++){
            addLook(new MissileLook(model.getMissileJoueur1()[i]));
            addLook(new MissileLook(model.getMissileJoueur2()[i]));
        }*/



        addLook(new StockMissileLook(40,40,model.getStockMissileJ1()));
        addLook(new StockMissileLook(40,40,model.getStockMissileJ2()));

        for (int i=0; i<model.getMissileJoueur2().length; i++){
            addLook(new MissileLook(15,model.getMissileJoueur2()[i]));
            addLook(new MissileLook(15,model.getMissileJoueur1()[i]));

        }

        addLook(new TextLook(22,"0x000000",model.getInfoPartie()));





    }
}
