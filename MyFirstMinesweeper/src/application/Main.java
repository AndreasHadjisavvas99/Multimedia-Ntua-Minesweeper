package application;
	
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Application;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class Main extends Application {
	//LAYOUT
	private BorderPane layout = new BorderPane();
	//GAME CONDITIONS
	private static int turn;
	private static int left_clicks;
	private static int flagsPlaced=0;
	private static int win = 0; //0=undecided, 1=win, 2=lose
	//GAME SETTINGS
	private static int difficulty;
	private static int total_mines; //bombs
	private static int time;
	private static int superbomb;
	private static boolean succeded = false;
	//SCENE LABELS
	private Label labeltime;
	private Label labelbombs;
	private Label labelflags;
	//DIMENSIONS
    private static final int TILE_SIZE = 25;
	private static int W=225;
	private static int H=225;
    private static int X_TILES = W / TILE_SIZE;
	private static int Y_TILES = H / TILE_SIZE;
	private Tile[][] grid = new Tile[X_TILES][Y_TILES];
	//GAME TIME
	private static int TIMER_DURATION;
	private Timeline timeline;
	private int secondsRemaining;
	
	private void startTimer() {
		if (timeline != null) {
	        timeline.stop();
	    }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                timeline.stop();
                labeltime.setText("Time's up!");
                win = 2;
                save_game();
                // Handle game over logic here
            }else {
                labeltime.setText("Time remaining: " + secondsRemaining + " seconds");
            }
        }));
        timeline.setCycleCount(TIMER_DURATION);
        timeline.play();
    }
	public void save_game() {
		System.out.println("total mines: " + total_mines);
		System.out.println("turn: " + left_clicks);
		System.out.println("time: " + (TIMER_DURATION - secondsRemaining));
		System.out.println("win: " + win);
		FileWriter writer = null;
		try {
			writer = new FileWriter("games.txt",true);
			String line = total_mines + "," + left_clicks + "," + (TIMER_DURATION - secondsRemaining) + "," +  win + "\n";
	        writer.write(line); // write parameters to file
	        writer.close();
		} catch (IOException e) {
	        System.out.println("An error occurred while saving the game.");
	        e.printStackTrace();
		}
	}
	public void gameover() {
		timeline.stop();
		for (int i=0; i<Y_TILES; i++) {
			for (int j=0; j<X_TILES; j++) {
				Tile t = grid[i][j];
				t.isOpen = true;
				t.text.setVisible(true);
				t.border.setFill(null);
				if (t.hasBomb) {
					t.border.setFill(Color.RED);
				}
			}
				
		}
	}
	private int getNumBombs() {
	    int numBombs = 0;
	    for (int y=0; y<Y_TILES; y++) {
	        for (int x=0; x<X_TILES; x++) {
	            Tile tile = grid[x][y];
	            if (tile.hasBomb) {
	                numBombs++;
	            }
	        }
	    }
	    return numBombs;
	}
	private int getOpenedTiles() {
		int openedTiles = 0;
	    for (int y = 0; y < Y_TILES; y++) {
	        for (int x = 0; x < X_TILES; x++) {
	            Tile tile = grid[x][y];
	            if (tile.isOpen) {
	                openedTiles++;
	            }
	        }
	    }
	    return openedTiles;
	}
	private Parent createContent(int dif,int total_m,int tm,int sb) {
		TIMER_DURATION = secondsRemaining = tm;
		flagsPlaced=0;
		turn=0;
		left_clicks = 0;
		win=0;
		startTimer();
		
		Pane root=new Pane();		
		root.setPrefSize(W, H);

		FileWriter writer = null;
		try {
			writer = new FileWriter("mines.txt");
			int minesPlaced = 0;
			//create grid
			for (int y=0; y<Y_TILES; y++) {
				for (int x=0; x<X_TILES; x++) {
					Tile tile = new Tile(x,y, false);
					grid[x][y] = tile;
					root.getChildren().add(tile);
				}
			}
			 //place mines and write their coordinates to file
	        while(minesPlaced < total_m) {
	            int x = (int) (Math.random() * X_TILES);
	            int y = (int) (Math.random() * Y_TILES);
	            Tile tile = grid[x][y];
	            if (!tile.hasBomb) {
	            	// check if this is the mine to be turned into a superbomb(the last mine)
	            	if (minesPlaced == sb - 1) {
		                tile.isSuperBomb = true;
		                tile.hasBomb = true;
		            } else {
		                tile.hasBomb = true;
		            }
		            minesPlaced++;
	                //write coordinates to file
	                String line = String.format("%d,%d,%d%n", x, y, tile.isSuperBomb ? 1 : 0);
	                writer.write(line);
	            }
	        }
		} catch (IOException e) {
	        e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//count neighbor mines for each tile
		for (int y=0; y<Y_TILES; y++) {
			for (int x=0; x<X_TILES; x++) {
				Tile tile = grid[x][y];
				
				//skip tiles with bombs
				if (tile.hasBomb)
					continue;
				//calculate bombs
				long bombs = getNeighbors(tile).stream().filter(t -> t.hasBomb).count();	
				//tile.bombs = (int) bombs;
				
				//if 0 bombs dont show number else show
				if (bombs > 0)
					tile.text.setText(String.valueOf(bombs));
			}
		}
		return root;
	}
	private List<Tile> getNeighbors(Tile tile) {
		List<Tile> neighbors = new ArrayList<>();
		
		int [] points = new int[] {
			-1,-1,
			-1,0,
			-1,1,
			0,-1,
			0,1,
			1,-1,
			1,0,
			1,1
		};
		
		for (int i=0; i<points.length; i++) {
			int dx = points[i];
			int dy = points[++i];
			//neighbors coordinates
			int newX = tile.x + dx;
			int newY = tile.y + dy;
			//check if coordinates are within the grid
			if(newX >= 0 && newX < X_TILES 
					&& newY >=0 && newY < Y_TILES) {
				neighbors.add(grid[newX][newY]);
			}
		}
		return neighbors;
	}
	private class Tile extends StackPane{
		private int x,y;
		private boolean hasBomb;
		private boolean isOpen = false;
		private boolean isFlagged = false;
		private boolean isSuperBomb = false;
		
		private Rectangle border = new Rectangle(TILE_SIZE - 2,TILE_SIZE-2);
		private Text text = new Text();
		
		public Tile(int x,int y,boolean hasBomb) {
			this.x = x;
			this.y = y;
			this.hasBomb = hasBomb;
		
			border.setStroke(Color.LIGHTGRAY);
	
			text.setFont(Font.font(17));
			text.setText(hasBomb ? "X" : "");
			text.setVisible(false);
			getChildren().addAll(border, text);
			
			setTranslateX(x*TILE_SIZE);
			setTranslateY(y*TILE_SIZE);
			
			setOnMouseClicked(e -> {
				if (win == 0) {	//win=0=undecided
					if (e.getButton() == MouseButton.PRIMARY) {
						left_clicks++;
						open();
					} else {
						turn++;
						flag();
					}
				}
			});
		}
		public void open() {
			if (isOpen)
				return;
			
			if (hasBomb) { 
				labeltime.setText("Game Over!");
				win = 2;
				save_game();
				timeline.stop();
				gameover();
				return;
			}
			if (isFlagged) {
				flagsPlaced--;
				labelflags.setText("Flags: " + String.valueOf(flagsPlaced));
				isFlagged = false;
			}
			
			isOpen = true;
			text.setVisible(true);
			border.setFill(null);
			
			if (text.getText().isEmpty()) {
				getNeighbors(this).forEach(Tile::open);
			}
			//Win condition: opened all non-bomb tiles
			if (getOpenedTiles() == X_TILES*Y_TILES - getNumBombs()) {
				labeltime.setText("You Win!");
				win = 1;
				save_game();
				timeline.stop();
			}
		}
		
		public void flag() {
			if (isOpen) //do nothing
				return;	
			if (flagsPlaced < total_mines) {
				if (isFlagged==false) { //flag the tile
					flagsPlaced++;
					labelflags.setText("Flags: " + String.valueOf(flagsPlaced));
					isFlagged = true;
					border.setFill(Color.RED);
					if (isSuperBomb && turn<=4) 
						reveal();
					return;
				}
			}
			if (isFlagged==true) {	//un-flag
				isFlagged = false;
				border.setFill(Color.BLACK);
				flagsPlaced--;
				labelflags.setText("Flags: " + String.valueOf(flagsPlaced));
			}
		}
		public void reveal() {
			for (int i=0; i<Y_TILES; i++) {
				Tile t = grid[x][i];
				t.isOpen = true;
				t.text.setVisible(true);
				t.border.setFill(null);
				if (t.hasBomb) { 
					t.border.setFill(Color.ORANGE);
				}
				if (t.isFlagged) {
					flagsPlaced--;
					labelflags.setText("Flags: " + String.valueOf(flagsPlaced));
					t.isFlagged = false;
				}		
				//Win condition: opened all non-bomb tiles
				if (getOpenedTiles() == X_TILES*Y_TILES - getNumBombs()) {
					labeltime.setText("You Win!");
					win = 1;
					save_game();
					timeline.stop();
				}
			}
			for (int i=0; i<X_TILES; i++) {
				Tile t = grid[i][y];
				t.isOpen = true;
				t.text.setVisible(true);
				t.border.setFill(null);
				if (t.hasBomb) { 
					t.border.setFill(Color.ORANGE);
				}
				if (t.isFlagged) {
					flagsPlaced--;
					labelflags.setText("Flags: " + String.valueOf(flagsPlaced));
					t.isFlagged = false;
				}
				//Win condition: opened all non-bomb tiles
				if (getOpenedTiles() == X_TILES*Y_TILES - getNumBombs()) {
					labeltime.setText("You Win!");
					win = 1;
					save_game();
					timeline.stop();
				}
			}
		}
		
	}
	public static void main(String[] args) {
		launch(args);

	}
	
	private void displayLast5Games() {
	    Stage stage = new Stage();
	    stage.initModality(Modality.APPLICATION_MODAL);
	    stage.setTitle("Last 5 games");

	    VBox vbox = new VBox(10);
	    vbox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

	    try (BufferedReader br = new BufferedReader(new FileReader("games.txt"))) {
	        List<String> lines = new LinkedList<>();
	        String line;
	        while ((line = br.readLine()) != null) {
	            lines.add(line);
	        }
	        if (lines.size() > 5) {
	            lines.subList(0, lines.size() - 5).clear();
	        }
	        for (String l : lines) {
	            String[] values = l.split(",");
	            String result = "";
	            switch (Integer.parseInt(values[3])) {
	                case 0:
	                    result = "Undecided";
	                    break;
	                case 1:
	                    result = "Win";
	                    break;
	                case 2:
	                    result = "Lose";
	                    break;
	            }
	            Label label = new Label("Total mines: " + values[0] + " | Turn: " + values[1] +
	                " | Time: " + values[2] + " seconds | Result: " + result);
	            vbox.getChildren().add(label);
	        }

	        // write the latest 5 games back to the file
	        try (BufferedWriter bw = new BufferedWriter(new FileWriter("games.txt"))) {
	            for (String l : lines) {
	                bw.write(l);
	                bw.newLine();
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    Scene scene = new Scene(vbox);
	    stage.setScene(scene);
	    stage.show();
	}	
	
	@Override
	public void start(Stage stage) throws Exception {
		//layout
		//BorderPane layout = new BorderPane();
		
		Menu ApplicationMenu = new Menu("Application");
		//CREATE SCENARIO
		MenuItem createItem = new MenuItem("Create");
		createItem.setOnAction(e -> {
			Create popup = new Create();
		    popup.display();
		});
		ApplicationMenu.getItems().add(createItem);
		//LOAD SCENARIO
		MenuItem loadItem = new MenuItem("Load");
		loadItem.setOnAction(e -> {
			Load popup = new Load();
		    popup.display();
		    succeded = popup.getSucceded();
		    if (succeded) {
		    	difficulty = popup.getDifficulty();
			    total_mines = popup.getNumOfBombs();
			    time = popup.getTime();
			    superbomb = popup.getSuperbomb();
		    }
		});
		ApplicationMenu.getItems().add(loadItem);
		//START NEW GAME
		MenuItem startItem = new MenuItem("Start");
		startItem.setOnAction(e -> {
			if(difficulty == 1) {
				W=H=225;
				stage.setWidth(325);
				stage.setHeight(325);
				if (superbomb == 1)
					System.out.println("has superbomb");
			}
			else if (difficulty==2){
				W=H=400;
				stage.setWidth(420);
				stage.setHeight(490);
			} else return;
			System.out.println("difficulty: " + difficulty);
			X_TILES = W / TILE_SIZE;
			Y_TILES = H / TILE_SIZE;
			grid = new Tile[X_TILES][Y_TILES];
			// Create the grid
		    Pane grid = (Pane) createContent(difficulty, total_mines,time,superbomb);
		    labeltime = new Label("Time remaining: " + secondsRemaining + " seconds");
		    labelflags = new Label("Flags: " + String.valueOf(flagsPlaced));
		    labelbombs =  new Label("Bombs: " + String.valueOf(total_mines));
			HBox hbox =  new HBox(30, labelbombs,labelflags,labeltime);
			VBox vbox = new VBox (10,hbox,grid);
		    // Add the grid to the center of the layout
		    layout.setCenter(vbox);
		    
		});
		ApplicationMenu.getItems().add(startItem);
		//EXIT APP
		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setOnAction(e -> stage.close());
		ApplicationMenu.getItems().add(exitMenuItem);
		//DETAILS MENU
		Menu DetailsMenu = new Menu("Details");
		//ROUNDS
		MenuItem roundsItem = new MenuItem("Rounds");
		roundsItem.setOnAction(e -> {
			//implement code for rounds
			displayLast5Games();
		});
		DetailsMenu.getItems().add(roundsItem);
		//SOLUTION
		MenuItem solutionItem = new MenuItem("Solution");
		solutionItem.setOnAction(e -> {
			if (win == 0) { //win=0=undecided
				win = 2;	//win=2=lose player decided to give up
				save_game();
				gameover();
			}
		});
		DetailsMenu.getItems().add(solutionItem);
				
		//Main Menu Bar
		MenuBar menuBar =  new MenuBar();
		menuBar.getMenus().addAll(ApplicationMenu,DetailsMenu);
		layout.setTop(menuBar);
		
		//Pane root = (Pane) createContent(difficulty,total_mines);
		//layout.setCenter(root);
		Scene scene = new Scene(layout, 360, 415);
		stage.setTitle("MediaLab Minesweeper");
		stage.setScene(scene);
        stage.show();
	}
}
