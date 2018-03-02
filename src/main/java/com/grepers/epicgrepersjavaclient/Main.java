package com.grepers.epicgrepersjavaclient;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        new WSClient().start(primaryStage);

        primaryStage.setTitle("Epic Grepers");
        Group root = new Group();
        Scene scene = new Scene(root, 300, 275);
        root.getChildren().add(new Text(10, 50, "Test Text"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
