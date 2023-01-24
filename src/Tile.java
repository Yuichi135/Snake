import javafx.scene.shape.Rectangle;
public class Tile {
    final private Rectangle square;
    final private int x;
    final private int y;
    private TileState currentState;
    private TileState nextState;
    private boolean initialUpdate;

    public Tile(Rectangle square, int x, int y) {
        this.square = square;
        this.x = x;
        this.y = y;
        this.currentState = TileState.BACKGROUND;
        this.nextState = this.currentState;
    }

    public boolean canChange() {
        return this.nextState == TileState.BACKGROUND;
    }

    public Tile forceState(TileState currentState) {
        this.currentState = currentState;
        return this;
    }

    public void setNextState(TileState nextState) {
        this.nextState = nextState;
    }

    public boolean isFruit() {
        return this.currentState == TileState.FRUIT;
    }

    public boolean isAnObstacle() {
        return this.currentState == TileState.SNAKE_BODY || this.currentState == TileState.SNAKE_HEAD;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void update() {
        if (this.currentState == this.nextState && this.initialUpdate) return;

        this.initialUpdate = true;
        this.currentState = this.nextState;
        this.square.setFill(SnakeGame.getColor(this.currentState));
    }

    @Override
    public String toString() {
        return "Tile\t\tX:" + this.x + "\tY:\t" + this.y + "\tcurrent state:\t" + this.currentState + "\t next state:\t" + nextState;
    }
}
