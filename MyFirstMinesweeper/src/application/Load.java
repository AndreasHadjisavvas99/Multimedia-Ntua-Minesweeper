package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Load {
    private String scenarioId;
    private int difficulty;
    private int numOfBombs;
    private int time;
    private int superbomb;
    private boolean succeded=true;

    public Load() {}

    public void display() {
        Stage window = new Stage();
        window.setTitle("Load Scenario");

        // GridPane layout
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(8);
        layout.setHgap(10);

        // Scenario ID input
        Label scenarioIdLabel = new Label("Scenario ID:");
        GridPane.setConstraints(scenarioIdLabel, 0, 0);
        TextField scenarioIdInput = new TextField();
        GridPane.setConstraints(scenarioIdInput, 1, 0);
        //Warning Label
        Label warningLabel = new Label("");
        GridPane.setConstraints(warningLabel, 1, 2);
        // Load button
        Button loadButton = new Button("Load");
        GridPane.setConstraints(loadButton, 1, 1);

        loadButton.setOnAction(e -> {
            scenarioId = scenarioIdInput.getText();
            String directory = System.getProperty("user.dir") + "/src/application/medialab/";
            String fileName = directory + "SCENARIO-" + scenarioId + ".txt";
            File file = new File(fileName);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    difficulty = Integer.parseInt(br.readLine());
                    numOfBombs = Integer.parseInt(br.readLine());
                    time = Integer.parseInt(br.readLine());
                    superbomb = Integer.parseInt(br.readLine());
                    if(br.readLine() != null) {
                        succeded = false;
                        warningLabel.setText("Invalid number of lines in the file.");
                        throw new InvalidDescriptionException("Invalid number of lines in the file.");
                    }                    
                    if (difficulty != 1 && difficulty != 2) {
                    	succeded = false;
                    	warningLabel.setText("Invalid difficulty!");
                        throw new InvalidValueException("Invalid difficulty!");
                    }
                    if (difficulty == 1) {
                    	if(!((numOfBombs >=9 && numOfBombs <=11) && (time >=120 && time <=180) && (superbomb == 0))) {
                    		succeded = false;
                    		warningLabel.setText("Invalid scenario description!");
                    		throw new InvalidValueException("InvalidValueException: Invalid scenario description!");
                    	}
                    }
                    if (difficulty == 2 && (numOfBombs >=35 && numOfBombs <=45) && (time >=240 && time <=360)) {
                    	if(!((numOfBombs >=35 && numOfBombs <=45) && (time >=240 && time <=360)))
                    	{
                    		succeded = false;
                    		warningLabel.setText("Invalid scenario description!");
                    		throw new InvalidValueException("InvalidValueException: Invalid scenario description!");
                    	}
                    }
                    if (!(superbomb >=0 && superbomb <=1)) {
                    	succeded = false;
                    	warningLabel.setText("Superbomb must be either 0 or 1!");
                    	throw new InvalidValueException("Invalid scenario description! Superbomb must be either 0 or 1");
                    }
                    System.out.println("Scenario loaded: " + scenarioId);
                    window.close();
                } catch (IOException ex) {
                    System.out.println("Error reading file: " + file.getName());
                } catch (NumberFormatException ex) {
                    System.out.println("Error parsing scenario details: " + file.getName());
                } catch (InvalidValueException ex) {
                    System.out.println(ex.getMessage());
                } catch (InvalidDescriptionException ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                System.out.println("Scenario not found: " + scenarioId);
            }
        });

        // Add controls to layout
        layout.getChildren().addAll(scenarioIdLabel, scenarioIdInput, loadButton,warningLabel);

        // Create scene and show window
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
    public class InvalidValueException extends Exception {
		public InvalidValueException(String message) {
            super(message);
        }
    }
    public class InvalidDescriptionException extends Exception {
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
    public boolean getSucceded() {
    	return succeded;
    }

    public int getTime() {
        return time;
    }
    public int getSuperbomb() {
        return superbomb;
    }
}