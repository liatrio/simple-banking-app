package com.smartbank;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for SmartBank
 */
public class SmartBankApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/smartbank/view/MainView.fxml"));
        Scene scene = new Scene(root, 800, 600);
        
        // Load CSS stylesheet
        String css = getClass().getResource("/com/smartbank/view/application.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        primaryStage.setTitle("SmartBank - Banking Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Main method to launch the application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
