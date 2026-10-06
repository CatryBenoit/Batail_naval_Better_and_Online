package control;

import boardifier.model.Model;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import view.SelectionPage;

public class TextController implements ChangeListener<String> {

    private Model model;
    private SelectionPage sp;
    private static String missiles;

    public TextController(Model model, SelectionPage sp) {
        this.model = model;
        this.sp = sp;
        sp.setTextListener(this);  // Set the text listener in the SelectionPage
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        missiles = newValue;
    }



    // Static method to get the current missiles value (-1 if the field is empty or invalid)
    public static int getMissiles() {
        if (missiles == null || missiles.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(missiles);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + missiles);
            return -1;
        }
    }
}
