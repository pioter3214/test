import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HighScoreWindow extends JFrame {

    public HighScoreWindow() {
        setTitle("High Scores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        java.util.List<ScoreEntry> topScores = HighScoreManager.getTopScores(20);

        DefaultListModel<ScoreEntry> listModel = new DefaultListModel<>();
        for (ScoreEntry entry : topScores) {
            listModel.addElement(entry);
        }

        JList<ScoreEntry> scoreList = new JList<>(listModel);
        scoreList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
