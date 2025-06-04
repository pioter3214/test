import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighScoreManager {
    private static final String SCORES_FILE = "highscores.txt";
    private static List<ScoreEntry> scores = new ArrayList<>();

    static {
        loadScores();
    }

    public static void addScore(String player, int points, long timeMillis) {
        scores.add(new ScoreEntry(player, points, timeMillis));
        saveScores();
    }

    public static List<ScoreEntry> getTopScores(int limit) {
        List<ScoreEntry> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.comparingInt(ScoreEntry::getPoints).reversed());
        return sorted.size() <= limit ? sorted : sorted.subList(0, limit);
    }

    private static void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadScores() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) {
            scores = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            scores = (List<ScoreEntry>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            scores = new ArrayList<>();
        }
    }
}
