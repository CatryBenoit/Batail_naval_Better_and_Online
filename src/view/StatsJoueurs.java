package view;

import boardifier.model.ContainerElement;
import boardifier.model.GameElement;
import boardifier.model.TextElement;
import boardifier.view.ContainerLook;
import model.BattleShipStageModel;
import model.Ship;

public class StatsJoueurs {


    /** indice     |     donnée
     *    [0]      |     nombre max de parties de bateau j1
     *    [1]      |     nombre de parties de bateau restantes j1
     *    [2]      |     nombre de parties de bateau max j2
     *    [3]      |     nombre de partie de bateau restante j2**/
    public static int[] findStats(BattleShipStageModel model){


        int[] results=new int[4];
        int j1ShipTotal=0;
        int j2ShipTotal=0;
        int j1PartRestantes=0;
        int j2PartRestantes=0;
        TextElement j1 = model.getPlayer1Name();
        TextElement j2 = model.getPlayer2Name();
        Ship[] shipj1 = model.getShipsPlayer1();
        Ship[] shipj2 = model.getShipsPlayer2();
        for (int i=0; i<shipj1.length; i++){
            j1ShipTotal+=shipj1[i].getTaille();
            j1PartRestantes+=shipj1[i].getTaille()-shipj1[i].nbdepartcouler();
        }

        for (int i=0; i<shipj2.length; i++){
            j2ShipTotal+=shipj2[i].getTaille();
            j2PartRestantes+=shipj2[i].getTaille()-shipj2[i].nbdepartcouler();
        }

        results[0]=j1ShipTotal;
        results[1]=j1PartRestantes;
        results[2]=j2ShipTotal;
        results[3]=j2PartRestantes;
        return results;
    }

    public static String showStats(int[] stats, TextElement j1, TextElement j2){
        return j1 + " : "+ stats[1]+"/"+stats[0]+"   |   "+j2+" : "+stats[3]+"/"+stats[2];
        }
    }

