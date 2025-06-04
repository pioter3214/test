import java.io.Serializable;

public class ScoreEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String player;
    private int points;
    private long timeMillis;

    public ScoreEntry(String player, int points, long timeMillis) {
        this.player = player;
        this.points = points;
        this.timeMillis = timeMillis;
    }

    public String getPlayer() {
        return player;
    }
    public int getPoints() {
        return points;
    }
    public long getTimeMillis() {
        return timeMillis;
    }

    @Override
    public String toString() {
        return "Name: " + player + " | Score: " + points + " | Time: " + timeMillis;
    }
}
