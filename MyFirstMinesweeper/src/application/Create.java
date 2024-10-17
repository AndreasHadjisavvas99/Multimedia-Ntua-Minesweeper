package application;

import java.io.IOException;
import java.io.PrintWriter;

import application.Load.InvalidDescriptionException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Create {
    
    private String scenarioId;
    private int difficulty;
    private int numOfBombs;
    private int time;
    private int superbomb;
    
    public void display() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Create Scenario");
        popup.setResizable(false);
        
        Label label1 = new Label("Scenario-ID:");
        Label label2 = new Label("Difficulty:");
        Label label3 = new Label("Number of Bombs:");
        Label label4 = new Label("Time:");
        Label label5 = new Label("Super-Bomb:");
        TextField textField1 = new TextField();
        TextField textField2 = new TextField();
        TextField textField3 = new TextField();
        TextField textField4 = new TextField();
        TextField textField5 = new TextField();
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
        	if (textField1.getText().isEmpty() || textField2.getText().isEmpty() || textField3.getText().isEmpty()
                    || textField4.getText().isEmpty() || textField5.getText().isEmpty()) {
                throw new InvalidDescriptionException("One or more fields are empty");
            }      	
        	
            scenarioId = textField1.getText();
            System.out.println(scenarioId);
            difficulty = Integer.parseInt(textField2.getText());
            numOfBombs = Integer.parseInt(textField3.getText());
            time = Integer.parseInt(textField4.getText());
            superbomb = Integer.parseInt(textField5.getText());
            String directory = System.getProperty("user.dir") + "/src/application/medialab/";
            String fileName = directory + "SCENARIO-" + scenarioId + ".txt";
            try {
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
                writer.println(difficulty);
                writer.println(numOfBombs);
                writer.println(time);
                writer.print(superbomb);
                writer.close();
                System.out.println("File created: " + fileName);
            } catch (IOException ex) {
                System.out.println("Error creating file: " + fileName);
            } catch (InvalidDescriptionException ex) {
                System.out.println(ex.getMessage());
            }
            
            popup.close();
        });
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.add(label1, 0, 0);
        grid.add(textField1, 1, 0);
        grid.add(label2, 0, 1);
        grid.add(textField2, 1, 1);
        grid.add(label3, 0, 2);
        grid.add(textField3, 1, 2);
        grid.add(label4, 0, 3);
        grid.add(textField4, 1, 3);
        grid.add(label5, 0, 4);
        grid.add(textField5, 1, 4);
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(grid, submitButton);
        Scene scene = new Scene(vbox);
        popup.setScene(scene);
        popup.showAndWait();
    }
    public class InvalidDescriptionException extends RuntimeException {
        public InvalidDescriptionException(String message) {
            super(message);
        }
    }
    
    public String getScenarioId() {
        return scenarioId;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public int getNumOfBombs() {
        return numOfBombs;
    }
    
    public int getTime() {
        return time;
    }
    public int getSuperbomb() {
        return superbomb;
    }
}
