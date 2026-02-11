package fr.bordeaux.gui;


import fr.bordeaux.config.ConfigManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class QuoridorApp extends Application {

    @Override
    public void start(Stage stage) {
        ConfigManager.getInstance().load();

        BorderPane borderPane = new BorderPane();
        MainMenuBar menuBar = new MainMenuBar();

        borderPane.setTop(menuBar);
        BackgroundImage background = getBackgroundImage();
        borderPane.setBackground(new Background(background));


        Scene scene = new Scene(borderPane, 900, 700);
        ShortcutManager.getInstance().register(scene);

        stage.setTitle("Quoridor");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        ConfigManager.getInstance().save();
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
