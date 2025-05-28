import javax.swing.*;

public class Upgrade {
    private int row, col;
    private UpgradeType type;
    private ImageIcon icon;
    private long spawnTime;
    private static final long LIFESPAN = 10000; // 10 sekund życia

    public Upgrade(int row, int col, UpgradeType type, ImageIcon icon) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.icon = icon;
        this.spawnTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > LIFESPAN;
    }

    // Gettery
    public int getRow() { return row; }
    public int getCol() { return col; }
    public UpgradeType getType() { return type; }
    public ImageIcon getIcon() { return icon; }
}
