import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;

public class SnakeGame extends Application {
    private final static int GRID_HEIGHT = 8;
    private final static int GRID_WIDTH = 16;
    private final static int TILE_SIZE = 64;
    private final static int MAX_SCORE = GRID_WIDTH * GRID_WIDTH;
    private final static int GRID_SPACING = 2;
    private final ArrayList<ArrayList<Tile>> tiles = new ArrayList<>();
    private GridPane grid = new GridPane();
    private Scene scene;
    private Stage stage;
    private MenuBar menuBar;
    private Tile[] body;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.stage.setResizable(false);
        this.stage.setHeight(GRID_HEIGHT * TILE_SIZE + ((GRID_HEIGHT - 1) * GRID_SPACING) + 39 + 25);
        this.stage.setWidth(GRID_WIDTH * TILE_SIZE + ((GRID_WIDTH) * GRID_SPACING) + 14);
        this.stage.setTitle("Snake");
        this.stage.show();

        this.initGame();
        this.showStartScreen();
    }

    private void showStartScreen() {
        if (menuBar == null)
            this.createMenu();

        Label label = new Label("Snake game!");
        Button startButton = new Button("Start");

        VBox vbox = new VBox(label, startButton);
        vbox.setAlignment(Pos.CENTER);
        this.scene = new Scene(new VBox(this.menuBar, new BorderPane(vbox)));
        this.stage.setScene(this.scene);

        startButton.setOnAction(event -> this.startGame());
    }

    private void showGameScreen() {
        this.scene = new Scene(new VBox(this.menuBar, this.grid));
        this.stage.setScene(this.scene);
    }

    private void createMenu() {
        Menu menu = new Menu("Settings");
        MenuItem menuStart = new MenuItem("Start");
        MenuItem menuStop = new MenuItem("Stop");
        MenuItem menuReset = new MenuItem("Reset");

        menu.getItems().addAll(menuStart, menuStop, menuReset);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menu);

        menuStart.setOnAction(event -> {
            System.out.println("Start");
            this.updateGrid();
        });

        menuStop.setOnAction(event -> {
            System.out.println("Stop");
            this.showStartScreen();
        });

        menuReset.setOnAction(event -> {
            System.out.println("Reset");
            this.reset();
        });

        this.menuBar = menuBar;
    }

    private void initGame() {
        this.grid = createGrid();
        this.reset();
    }

    private void startGame() {
        this.showGameScreen();

        this.scene.setOnKeyReleased(event -> {
            if (event.getText().chars().count() != 1)
                return;

            switch (event.getText().charAt(0)) {
                case 'w':
                    this.move(Direction.TOP);
                    break;
                case 'a':
                    this.move(Direction.LEFT);
                    break;
                case 's':
                    this.move(Direction.BOTTOM);
                    break;
                case 'd':
                    this.move(Direction.RIGHT);
                    break;
                case ' ':
                    break;
                case 'p':
                    this.reset();
                    return;
                default:
                    return;
            }
            this.updateGrid();
        });
    }

    private GridPane createGrid() {
        this.grid.getChildren().clear();
        this.grid.setHgap(GRID_SPACING);
        this.grid.setVgap(GRID_SPACING);
        this.tiles.clear();
        for (int x = 0; x < GRID_WIDTH; x++) {
            this.tiles.add(new ArrayList<>());

            for (int y = 0; y < GRID_HEIGHT; y++) {
                Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE, SnakeGame.randomColor());
                this.tiles.get(x).add(new Tile(square, x, y));
                this.grid.add(square, x, y);
            }
        }

        return grid;
    }

    private void reset() {
        this.grid = this.createGrid();
        this.body = new Tile[1];
        this.setStart();
        this.updateGrid();
    }

    private void setStart() {
        this.setRandomTile(TileState.FRUIT);
        this.body[0] = this.setRandomTile(TileState.SNAKE_HEAD);
    }

    private void endGame() {
        new Alert(Alert.AlertType.INFORMATION, "Score: " + this.body.length).show();
        this.reset();
    }

    private Tile setRandomTile(TileState state) {
        int totalFreeTiles = 0;
        for (ArrayList<Tile> tileList : tiles) {
            for (Tile tile : tileList) {
                if (tile.canChange())
                    totalFreeTiles++;
            }
        }

        int randomPosition = (int) (Math.random() * totalFreeTiles);
        int pos = 0;

        for (ArrayList<Tile> tileList : tiles) {
            for (Tile tile : tileList) {
                if (!tile.canChange())
                    continue;

                if (randomPosition <= pos) {
                    tile.setNextState(state);
                    return tile;
                }
                pos++;
            }
        }
        throw new RuntimeException();
    }

    private void updateGrid() {
        for (ArrayList<Tile> tileList : tiles) {
            for (Tile tile : tileList) {
                tile.update();
            }
        }
    }

    private void move(int direction) {
        Tile head = this.body[0];

        Tile[] adjecentTiles = this.getAdjacentTiles(head);
        Tile target = adjecentTiles[direction];

        if (target == null || target.isAnObstacle()) {
            this.endGame();
            return;
        }

        if (target.isFruit()) {
            if (this.body.length + 1 == MAX_SCORE) {
                this.endGame();
                return;
            }
            this.setRandomTile(TileState.FRUIT);
            this.moveBodyElementsBack();
        } else {
            this.body[this.body.length - 1].setNextState(TileState.BACKGROUND);
            System.arraycopy(this.body, 0, this.body, 1, this.body.length - 1);
        }
        this.body[0] = target;
        this.body[0].setNextState(TileState.SNAKE_HEAD);

        if (this.body.length > 1)
            this.body[1].setNextState(TileState.SNAKE_BODY);
    }

    private void moveBodyElementsBack() {
        Tile[] newBody = new Tile[this.body.length + 1];
        for (int i = 0; i < this.body.length; i++) {
            newBody[i + 1] = this.body[i];
        }
        this.body = newBody;
    }

    public Tile[] getAdjacentTiles(Tile tile) {
        int tileX = tile.getX();
        int tileY = tile.getY();

        Tile[] adjacentTiles = new Tile[4];

        int counter = -1;
        for (int i = -1; i <= 1; i += 2) {
            counter++;
            if (!((i + tileX) < 0 || (i + tileX) >= GRID_WIDTH))
                adjacentTiles[counter] = this.tiles.get(tileX + i).get(tileY);

            counter++;
            if ((i + tileY) < 0 || (i + tileY) >= GRID_HEIGHT) {
                continue;
            }
            adjacentTiles[counter] = this.tiles.get(tileX).get(tileY + i);
        }

        // [0] = LEFT
        // [1] = TOP
        // [2] = RIGHT
        // [3] = BOTTOM

        return adjacentTiles;
    }

    private static Color randomColor() {
        return Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    public static Color getColor(TileState state) {
        switch (state) {
            case BACKGROUND:
                return Color.GRAY;
            case FRUIT:
                return Color.SALMON;
            case SNAKE_HEAD:
                return Color.DARKGREEN;
            case SNAKE_BODY:
                return Color.MEDIUMSEAGREEN;
            default:
                return SnakeGame.randomColor();
        }
    }
}
