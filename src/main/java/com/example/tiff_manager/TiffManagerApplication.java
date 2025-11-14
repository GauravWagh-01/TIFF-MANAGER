package com.example.tiff_manager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TiffManagerApplication extends Application {
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;

    public static void main(String[] args) {
        // Launch JavaFX application
        Application.launch(TiffManagerApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        // Initialize Spring Boot context
        springContext = SpringApplication.run(TiffManagerApplication.class);

        // Load FXML with Spring context
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("TIFF Manager - Desktop Application");

        Scene scene = new Scene(rootNode, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set up proper shutdown
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            springContext.close();
        });
    }

    @Override
    public void stop() throws Exception {
        // Close Spring context when JavaFX application stops
        if (springContext != null) {
            springContext.close();
        }
    }
}