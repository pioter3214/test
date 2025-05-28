import java.io.Serializable;

public class ScoreEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private int timeSeconds;
    private long date;

    public ScoreEntry(String playerName, int score, int timeSeconds) {
        this.playerName = playerName;
        this.score = score;
        this.timeSeconds = timeSeconds;
        this.date = System.currentTimeMillis();
    }

    // Gettery
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getTimeSeconds() { return timeSeconds; }
    public long getDate() { return date; }

    @Override
    public String toString() {
        return String.format("%s - %d pts (%d:%02d)",
                playerName, score, timeSeconds / 60, timeSeconds % 60);
    }
}
