import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {
    private BoardModel model;
    private volatile boolean gameEndHandled = false;

    Game(int rows, int cols){
        model = new BoardModel(rows, cols);
        Board board = new Board(model);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel scoreLabel = new JLabel("Score: 0");
        JLabel timeLabel = new JLabel("Time: 0");
        JLabel livesLabel = new JLabel("Lives: 3");
        JLabel effectsLabel = new JLabel("Effects: None");
        JPanel statusPanel = new JPanel(new FlowLayout());

        statusPanel.add(scoreLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(timeLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(livesLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(effectsLabel);

        setLayout(new BorderLayout());
        add(statusPanel, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);

        board.requestFocusInWindow();

        int cellSize = 32;
        int width = cols * cellSize + 50;
        int height = rows * cellSize + 150;
        setSize(width, height);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);

        new Thread(() -> {
            while (model.isGameRunning()) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
            if (!gameEndHandled) {
                SwingUtilities.invokeLater(this::handleGameEnd);
            }
        }).start();

        new Thread(() -> {
            while (model.isGameRunning()) {
                SwingUtilities.invokeLater(() -> {
                    if (model.isGameRunning()) {
                        model.checkGhostCollisions();
                    }
                    scoreLabel.setText("Score: " + model.getScore());
                    timeLabel.setText("Time: " + model.getElapsedTime());
                    livesLabel.setText("Lives: " + model.getLives());

                    if (model.getLives() <= 0 && !gameEndHandled) {
                        handleGameEnd();
                    }

                    java.util.List<String> effects = model.getUpgradeManager().getActiveEffects();
                    if (effects.isEmpty()) {
                        effectsLabel.setText("Effects: None");
                    } else {
                        effectsLabel.setText("Effects: " + String.join(", ", effects));
                    }
                });
                try { Thread.sleep(100); } catch (InterruptedException e) { break; }
            }
        }).start();
    }

    private synchronized void handleGameEnd() {
        if (gameEndHandled) return;
        gameEndHandled = true;

        model.stopGame();

        String playerName = JOptionPane.showInputDialog(this,
                "Game Over! Final Score: " + model.getScore() + "\nEnter your name for high score:",
                "Game Finished",
                JOptionPane.PLAIN_MESSAGE);

        if (playerName != null && !playerName.trim().isEmpty()) {
            ScoreEntry entry = new ScoreEntry(playerName.trim(), model.getScore(), model.getElapsedTime());
            HighScoreManager.addScore(entry.getPlayer(),entry.getPoints(),entry.getTimeMillis());
        }

        dispose();
        SwingUtilities.invokeLater(() -> new Menu());
    }
}
