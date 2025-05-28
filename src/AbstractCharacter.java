import javax.swing.*;

public abstract class AbstractCharacter {
    protected int row, col;
    protected ImageIcon[][] frames; // [kierunek][klatka]
    protected int direction; // 0: lewo, 1: prawo, 2: góra, 3: dół
    protected int currentFrame;

    public AbstractCharacter(int row, int col, ImageIcon[][] frames) {
        this.row = row;
        this.col = col;
        this.frames = frames;
        this.direction = 1; // domyślnie w prawo
        this.currentFrame = 0;
    }

    public abstract void move(BoardModel model);

    public ImageIcon getCurrentIcon() {
        return frames[direction][currentFrame];
    }

    public void nextFrame() {
        currentFrame = (currentFrame + 1) % frames[direction].length;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public void setRow(int r) { this.row = r; }
    public void setCol(int c) { this.col = c; }
    public void setDirection(int dir) { this.direction = dir; }
    public int getDirection() { return direction; }
}
