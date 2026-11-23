package org.example.disarium_number;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DisariumApplication extends Application {
    private DisariumController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(DisariumApplication.class.getResource("disarium-view.fxml"));
        Scene scene = new Scene(loader.load(), 760, 640);
        controller = loader.getController();
        stage.setTitle("Disarium Number");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
}
