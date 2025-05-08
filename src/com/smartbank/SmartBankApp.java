package com.smartbank;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main application class for SmartBank
 */
public class SmartBankApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(SmartBankApp.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        setupLogging();
        LOGGER.info("Starting SmartBank Application");
        
        // Load the login view first
        javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/com/smartbank/view/LoginView.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/smartbank/view/application.css").toExternalForm());
        
        // Configure the primary stage
        primaryStage.setTitle("SmartBank - Login");
        primaryStage.setMinWidth(800);  // Set minimum width
        primaryStage.setMinHeight(600); // Set minimum height
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Main method to launch the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        setupLogging();
        launch(args);
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.setLevel(Level.INFO);
        rootLogger.addHandler(handler);
    }
}
