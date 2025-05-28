import javax.swing.*;
import java.util.Random;

public class Ghost extends AbstractCharacter {
    private Random random = new Random();

    public Ghost(int row, int col, ImageIcon[][] frames) {
        super(row, col, frames);
    }

    @Override
    public void move(BoardModel model) {
        // Prosty ruch losowy ducha
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        int newRow, newCol;
        int tries = 0;

        do {
            direction = random.nextInt(4);
            newRow = row + dy[direction];
            newCol = col + dx[direction];
            tries++;
        } while ((newRow < 0 || newRow >= model.getRowCount() ||
                newCol < 0 || newCol >= model.getColumnCount() ||
                model.getElementAt(newRow, newCol) == MapElement.WALL) && tries < 10);

        if (tries < 10) {
            row = newRow;
            col = newCol;
        }
    }
}
