import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {
    private BoardModel model;

    Game(int rows, int cols){
        model = new BoardModel(rows, cols);
        Board board = new Board(model);

        JLabel scoreLabel = new JLabel("Score: 0");
        JLabel timeLabel = new JLabel("Time: 0");
        JLabel livesLabel = new JLabel("Lives: 3");
        JPanel statusPanel = new JPanel();

        statusPanel.add(scoreLabel);
        statusPanel.add(timeLabel);
        statusPanel.add(livesLabel);

        setLayout(new BorderLayout());
        add(statusPanel, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);

        board.requestFocusInWindow();

        int cellSize = 32;
        int width = cols * cellSize + 50;
        int height = rows * cellSize + 150;
        setSize(width, height);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        // Wątek aktualizacji statusu
        new Thread(() -> {
            while (true) {
                SwingUtilities.invokeLater(() -> {
                    scoreLabel.setText("Score: " + model.getScore());
                    timeLabel.setText("Time: " + model.getElapsedTime());
                    livesLabel.setText("Lives: " + model.getLives());
                });
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        }).start();
    }
}
