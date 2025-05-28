import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final String SCORES_FILE = "highscores.dat";
    private static List<ScoreEntry> scores = new ArrayList<>();

    static {
        loadScores();
    }

    public static void addScore(ScoreEntry entry) {
        scores.add(entry);
        Collections.sort(scores, (a, b) -> Integer.compare(b.getScore(), a.getScore()));
        saveScores();
    }

    public static List<ScoreEntry> getTopScores(int limit) {
        return scores.size() <= limit ? new ArrayList<>(scores) :
                new ArrayList<>(scores.subList(0, limit));
    }

    private static void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            scores = (List<ScoreEntry>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            scores = new ArrayList<>();
        }
    }
}
