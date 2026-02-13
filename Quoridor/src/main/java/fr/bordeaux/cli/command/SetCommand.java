package main.java.fr.bordeaux.cli.command;

<<<<<<< HEAD
=======
import main.java.fr.bordeaux.cli.command.*;
import fr.bordeaux.cli.command.*;
>>>>>>> 367e03691fa6ff5ce404f541f003c8b4f07feecd
import fr.bordeaux.core.GameEngine;

public class SetCommand implements Command {

    public SetCommand() { System.out.println("To implement"); }

    @Override
    public GameEngine execute(GameEngine engine) { System.out.println("To implement"); return engine; }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
