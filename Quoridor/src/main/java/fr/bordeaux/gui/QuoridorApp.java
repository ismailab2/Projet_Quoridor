package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class QuoridorApp extends Application {

  @Override
  public void start(Stage stage) {
    ConfigManager.getInstance().load();

    GameState state = new GameState(new Board(9), Player.createDefaultPlayers());
    GameEngine engine = new GameEngine(state);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    ShortcutManager.getInstance().setActionDispatcher(dispatcher);

    BorderPane borderPane = new BorderPane();
    MainMenuBar menuBar = new MainMenuBar(dispatcher);
    borderPane.setTop(menuBar);

    /*
    BackgroundImage background = getBackgroundImage();
    borderPane.setBackground(new Background(background));

     */
    BoardView boardView = new BoardView(engine);
    borderPane.setCenter(boardView);
    DragAndDrop.install(engine, boardView);

    Scene scene = new Scene(borderPane, 900, 700);
    ShortcutManager.getInstance().register(scene);

    boardView.prefWidthProperty().bind(borderPane.widthProperty());
    boardView
        .prefHeightProperty()
        .bind(borderPane.heightProperty().subtract(menuBar.heightProperty()));

    stage.setTitle("Quoridor");
    stage.setScene(scene);
    stage.show();
  }

  private BackgroundImage getBackgroundImage() {
    Image bgImage =
        new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/background.png")));
    BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, true, false);
    return new BackgroundImage(
        bgImage,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER,
        bgSize);
  }

  @Override
  public void stop() {
    ConfigManager.getInstance().save();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
