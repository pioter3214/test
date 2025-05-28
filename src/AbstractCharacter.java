import javax.swing.*;

public abstract class AbstractCharacter {
    protected int row, col;
    protected ImageIcon[][] frames;
    protected int direction;
    protected int currentFrame;

    public AbstractCharacter(int row, int col, ImageIcon[][] frames) {
        this.row = row;
        this.col = col;
        this.frames = frames;
        this.direction = 1; // domyślnie prawo
        this.currentFrame = 0;
    }

    public abstract void move(BoardModel model);

    public ImageIcon getCurrentIcon() {
        return frames[direction][currentFrame];
    }

    public void nextFrame() {
        currentFrame = (currentFrame + 1) % frames[direction].length;
    }

    // Gettery i settery
    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
    public void setDirection(int direction) { this.direction = direction; }
    public int getDirection() { return direction; }
}
