import javax.swing.*;

public class PacMan extends AbstractCharacter {
    public PacMan(int row, int col, ImageIcon[][] frames) {
        super(row, col, frames);
    }

    @Override
    public void move(BoardModel model) {
        // Ruch sterowany z klawiatury, obsługiwany w modelu, więc tutaj może być pusto
    }
}
