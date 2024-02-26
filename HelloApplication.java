package com.example.m06_programming2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class HelloApplication extends Application {


    private TextArea outputTextArea;

    Connection connection = null;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        //Top section with connect button
        Button connectButton = new Button("Connect to Database");
        connectButton.setOnAction(e -> showDatabaseConnectionDialog());

        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.getChildren().add(connectButton);
        root.setTop(topBox);

        //Middle section with output text area
        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        root.setCenter(outputTextArea);

        //Bottom section with update buttons
        Button batchUpdateButton = new Button("Batch Update");
        batchUpdateButton.setOnAction(e -> batchUpdateAction());

        Button nonBatchUpdateButton = new Button("Non-Batch Update");
        nonBatchUpdateButton.setOnAction(e -> nonBatchUpdateAction());

        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.getChildren().addAll(batchUpdateButton, nonBatchUpdateButton);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER);
        root.setBottom(bottomBox);


        Scene scene = new Scene(root, 600, 400);

        // Set up the stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Database Connection App");
        primaryStage.show();
    }

    private void showDatabaseConnectionDialog() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Database Connection");
        dialog.setHeaderText("Enter Database Information");


        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField jdbcDriverField = new TextField();
        jdbcDriverField.setPromptText("JDBC Driver");
        TextField urlField = new TextField();
        urlField.setPromptText("URL");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        content.getChildren().addAll(new Label("JDBC Driver:"), jdbcDriverField, new Label("URL:"), urlField, new Label("Username:"), usernameField, new Label("Password:"), passwordField);

        // Set the dialog buttons
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Handle connect button
        dialog.setResultConverter(buttonType -> {
            if (buttonType == connectButtonType) {

                String jdbcDriver = jdbcDriverField.getText();
                String url = urlField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();


                try {
                    Class.forName(jdbcDriver);
                } catch (ClassNotFoundException e) {
                    System.out.println("JDBC driver not found");
                    e.printStackTrace();
                }


                try{
                    connection = DriverManager.getConnection(url, username, password);
                }
                catch(SQLException e){
                    e.printStackTrace();
                }


            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> outputTextArea.appendText(result + "\n"));
    }

    public void batchUpdateAction() {

        long elapsedTime = System.currentTimeMillis();

        String query = "INSERT INTO temp (num1, num2, num3) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            for(int j = 0; j < 10; j++){
                for (int i = 0; i < 100; i++) {


                    statement.setDouble(1, Math.random());
                    statement.setDouble(2, Math.random());
                    statement.setDouble(3, Math.random());

                    statement.addBatch();
                }

                // Execute the batch
                statement.executeBatch();
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        elapsedTime = System.currentTimeMillis() - elapsedTime;

        outputTextArea.appendText("Batch Elapsed Time(Millis): " + Long.toString(elapsedTime) + "\n");


    }

    public void nonBatchUpdateAction() {

        long elapsedTime = System.currentTimeMillis();

        String query = "INSERT INTO temp (num1, num2, num3) VALUES (?, ?, ?)";

        for(int i = 0; i < 1000; i++){


            try(PreparedStatement statement = connection.prepareStatement(query)){

                statement.setDouble(1, Math.random());
                statement.setDouble(2, Math.random());
                statement.setDouble(3, Math.random());


                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        elapsedTime = System.currentTimeMillis() - elapsedTime;

        outputTextArea.appendText("Non-Batch Elapsed Time(Millis): " + Long.toString(elapsedTime) + "\n");

    }

    public static void main(String[] args) {
        launch();
    }
}