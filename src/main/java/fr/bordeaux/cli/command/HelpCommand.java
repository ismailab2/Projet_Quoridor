package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class HelpCommand implements Command {

  private String[] tokens;

  public HelpCommand() {
    this.tokens = null;
  }

  public HelpCommand(String[] t) {
    this.tokens = t;
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens == null) {
      System.out.println("Pas de commande\n");
    } else if (tokens.length == 1) {
      helpCli();
    } else {
      helpFetch();
    }
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("Rien");
  }

  public void helpCli() {
    System.out.println("/=== Quoridor Command Line Help ===/\n");
    System.out.println("Commands :\n");
    System.out.println("new [ARGS] :          Start a new game with the given args");
    System.out.println("help [CMD] :          If no command is given, displays this message.");
    System.out.println(
        "                      Otherwise, displays a more detailed help for a specific command.");
    System.out.println("help move :           Shows you how to play a move.");
    System.out.println(
        "quit :                Quit the current game. It'll ask you if you want to save it before quitting.");
    System.out.println("load [filename] :     Load a game from the given file \"filename\".");
    System.out.println("save [filename] :     Save the current game to a file \"filename\".");
    System.out.println(
        "pause :               Pause the timer if you're in a blitz game, else it does nothing.");
    System.out.println("hint :                Suggest you a move if you don't know what to play.");
    System.out.println(
        "undo [N] :            Unplays the N last move played. Just once if no N given.");
    System.out.println(
        "redo [N] :            Replays the N undone move played. Just once if no N given.");
    System.out.println("show board :          Displays the board of the current game.");
    System.out.println(
        "show history :        Displays all the moves that were played during the game. From older to the more recent.");
    System.out.println(
        "show time :           Displays the time you and your opponent(s) each remain.");
    System.out.println("show configuration :  Displays the configurations set on your game.");
    System.out.println(
        "set PARAM=VALUE :     Changing the value of a given parameters PARAM to VALUE");
  }

  /* public void helpMove() {
    ;
  } */

  public void helpFetch() {
    if (this.tokens.length <= 1) System.out.println("Un truc à expliquer");
    else {
      Command cmd = null;
      switch (tokens[1]) {
        case "move":
          {
            cmd = new MoveCommand();
            break;
          }
        case "quit":
          {
            cmd = new QuitCommand();
            break;
          }
        case "new":
          {
            cmd = new NewCommand();
            break;
          }
        case "load":
          {
            cmd = new LoadCommand(tokens);
            break;
          }
        case "save":
          {
            cmd = new SaveCommand(tokens);
            break;
          }
        case "pause":
          {
            cmd = new PauseCommand();
            break;
          }
        case "undo":
          {
            cmd = new UndoCommand();
            break;
          }
        case "redo":
          {
            cmd = new RedoCommand();
            break;
          }
        case "show":
          {
            cmd = new ShowCommand();
            break;
          }
        case "set":
          {
            cmd = new SetCommand();
            break;
          }
        default:
          {
            cmd = null;
          }
      }
      if (cmd != null) {
        cmd.helpText();
      }
    }
  }
}
