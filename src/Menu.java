import javax.swing.*;
import java.awt.*;

public class Menu extends JFrame {
    private int gameWidth;
    private int gameHeight;

    Menu(){
        setTitle("PacMan");
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        panel.setBackground(Color.black);

        JButton newGameButton = new YellowButton("New Game");
        newGameButton.addActionListener(e -> {
            try {
                gameWidth = Integer.parseInt(JOptionPane.showInputDialog("Select number of cols fom 10 to 100"));
                if (gameWidth < 10 || gameWidth > 100) throw new WrongNumberException();
                gameHeight = Integer.parseInt(JOptionPane.showInputDialog("Select number of rows from 10 to 100"));
                if (gameHeight < 10 || gameHeight > 100) throw new WrongNumberException();
                dispose();
                SwingUtilities.invokeLater(() -> new Game(gameHeight, gameWidth));
            } catch (WrongNumberException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Select correct number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton highScoreButton = new YellowButton("High Scores");
        highScoreButton.addActionListener(e -> new HighScoreWindow());

        JButton exitButton = new YellowButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(newGameButton);
        panel.add(highScoreButton);
        panel.add(exitButton);

        add(panel);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
