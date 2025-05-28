import javax.swing.*;

public class PacMan extends AbstractCharacter {

    public PacMan(int row, int col, ImageIcon[][] frames) {
        super(row, col, frames);
    }

    @Override
    public void move(BoardModel model) {
        // Ruch sterowany przez gracza – logika w BoardModel lub kontrolerze
    }
}
