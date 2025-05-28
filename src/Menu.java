import javax.swing.*;

public class Menu extends JFrame {
    private int gameWidth;
    private int gameHeight;
    Menu(){
        JPanel panel = new JPanel();

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            try {
                gameWidth = Integer.parseInt(JOptionPane.showInputDialog("Podaj szerokość mapy od 10 do 100"));
                if (gameWidth < 10 || gameWidth > 100) throw new WrongNumberException();
                gameHeight = Integer.parseInt(JOptionPane.showInputDialog("Podaj wysokość mapy od 10 do 100"));
                if (gameHeight < 10 || gameHeight > 100) throw new WrongNumberException();
                SwingUtilities.invokeLater(() -> new Game(gameWidth,gameHeight));
            }catch (WrongNumberException | NumberFormatException ex){
                JOptionPane.showMessageDialog(null, "Proszę wprowadzić poprawną liczbę", "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        });

        JButton highScoreButton = new JButton("High Scores");

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));


        panel.add(newGameButton);
        panel.add(highScoreButton);
        panel.add(exitButton);


        add(panel);
        setSize(500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
