package fr.bordeaux.gui;

import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {

  GameController controller = new GameController();
  ShortcutConfig shortcuts = new ShortcutConfig();

  @Override
  public void start(Stage stage) throws Exception {
    MenuBar menuBar = new MenuBar();

    // file menu
    Menu fileMenu = new Menu("File");

    MenuItem newGame = new MenuItem("New Game");
    MenuItem loadGame = new MenuItem("Load Game");
    MenuItem saveGame = new MenuItem("Save Game");
    MenuItem config = new MenuItem("Configuration");
    MenuItem info = new MenuItem("Info");
    MenuItem quit = new MenuItem("Quit");

    fileMenu
        .getItems()
        .addAll(
            newGame,
            loadGame,
            saveGame,
            new SeparatorMenuItem(),
            config,
            info,
            new SeparatorMenuItem(),
            quit);

    // file Game
    Menu gameMenu = new Menu("Game");
    MenuItem undo = new MenuItem("Undo");
    MenuItem redo = new MenuItem("Redo");
    MenuItem pause = new MenuItem("Pause");
    MenuItem hint = new MenuItem("Hint");
    gameMenu.getItems().addAll(undo, redo, pause, hint);

    menuBar.getMenus().addAll(fileMenu, gameMenu);

    // les actions clicables sur le graphiques
    newGame.setOnAction(e -> controller.newGame());
    loadGame.setOnAction(e -> controller.loadGame());
    saveGame.setOnAction(e -> controller.saveGame());
    config.setOnAction(e -> shortcuts.showConfigDialog(stage));
    info.setOnAction(e -> controller.showInfo());
    quit.setOnAction(e -> Platform.exit());

    undo.setOnAction(e -> controller.undo());
    redo.setOnAction(e -> controller.redo());
    pause.setOnAction(e -> controller.pause());
    hint.setOnAction(e -> controller.hint());

    BorderPane borderPane = new BorderPane();
    borderPane.setTop(menuBar);
    BackgroundImage background = getBackgroundImage();
    borderPane.setBackground(new Background(background));
    Scene scene = new Scene(borderPane, 800, 600);

    // ==== RACCOURCIS GLOBAUX ====
    scene.setOnKeyPressed(
        event -> {
          if (event.isControlDown()) {
            switch (event.getCode()) {
              case N -> controller.newGame();
              case L -> controller.loadGame();
              case S -> controller.saveGame();
              case U -> controller.undo();
              case R -> controller.redo();
              case P -> controller.pause();
              case H -> controller.hint();
              case Q -> Platform.exit();
              case COMMA -> shortcuts.showConfigDialog(stage);
              case I -> controller.showInfo();
            }
          }
        });

    stage.setScene(scene);
    stage.setTitle("Quoridor");
    stage.setResizable(true);
    stage.show();
  }

  private BackgroundImage getBackgroundImage() {
    Image bgImage =
        new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/background.png")));

    BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, true, false);

    BackgroundImage background =
        new BackgroundImage(
            bgImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            bgSize);
    return background;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
