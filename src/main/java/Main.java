import java.util.Arrays;

import app.App;
import gui.TerminalGUI;

public class Main {
    public static void main(String[] args) {
        if (args.length > 1) {
            TerminalGUI.printLnError("Mais argumentos do que o necessário. É necessário apenas o timeout das enquetes em minutos.");
        }
        new App(Long.parseLong(args[0]));
    }
}
