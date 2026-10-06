package model;

import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;

public class Ship extends ContainerElement {
    private int taille;
    private boolean sens ; // true = V et false = H
    public shipPart[] shipParts;
    private int shipID;
    private static int nbShip = 1;
    private int playerID;
    private boolean couler = false;


    public Ship(int x, int y, int taille, GameStageModel gameStageModel) {
        super("ship", x, y, taille, 1 , gameStageModel);
        this.taille =taille;
        this.shipID = nbShip;
        nbShip++;

    }


    public void setSens(boolean sens) {
        this.sens = sens;
    }


    public void setShipParts(GameStageModel gameStageModel){
        this.shipParts = new shipPart[taille];


    }

    public shipPart[] getshippart(){
        return shipParts;
    }

    public int getTaille() {return taille;}

    public boolean getSens() {return sens;}

    public boolean setCordonnerShip(int y , int x, char sens){
        if(sens == 'V'){
            if (x>9 || y+taille-1>9 || 0>y || 0>x){
                System.out.println("imposible de placer le bateau");
                return false;
            }else{
                for(int i = 0 ; i < taille ; i++){
                    shipParts[i].setCordoner(x, y+i);
                }
                orienterParties();
                return true;
            }
        } else if (sens=='H') {
            if (x+taille-1>9 || y>9 || 0>y || 0>x){
                System.out.println("imposible de placer le bateau");
                return false;
            }else {
                for(int i = 0 ; i < taille ; i++){
                    shipParts[i].setCordoner(x+i, y);
                }
                orienterParties();
                return true;

            }
        }
        return false;

    }

    // indique à chaque partie si le bateau est vertical, d'après leurs coordonnées (pour tourner l'image)
    public void orienterParties(){
        boolean vertical = taille > 1 && shipParts[0].getcordonneX() == shipParts[1].getcordonneX();
        sens = vertical;
        for (shipPart part : shipParts) {
            part.setVertical(vertical);
        }
    }

    public void verifcouler(){
        int verif = 0;
        for (int i = 0; i < taille; i++) {
            if( shipParts[i].esttoucher() == true){
                verif++;
            }
        }
        if(verif==taille){
            for(int i = 0 ; i < taille ; i++){
                shipParts[i].setColors(2);
                // un bateau coulé est révélé à l'adversaire
                shipParts[i].setVisible(true);
            }
            couler = true;
        }
    }



    public int nbdepartcouler(){
        int nb = 0;
        for (int i = 0; i < taille; i++) {
            if( shipParts[i].esttoucher() == true){
                nb++;
            }
        }
    return nb;
    }

    public int getPartCordonneX(int numeroDeLaPart){
        return shipParts[numeroDeLaPart].getcordonneX();
    }

    public int getPartCordonneY(int numeroDeLaPart){
        return shipParts[numeroDeLaPart].getcordonneY();
    }

    public boolean getcouler() {return couler;}


    public void setidplayer(int id){
        playerID =id;
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getShipID() {
        return shipID;
    }
}
