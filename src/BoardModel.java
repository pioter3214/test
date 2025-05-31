import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BoardModel extends AbstractTableModel {
    private int rows;
    private int cols;
    private Object[][] elements;
    private int score = 0;
    private int lives = 3;
    private long startTime = System.currentTimeMillis();
    private boolean gameRunning = true;

    private PacMan pacman;
    private List<Ghost> ghosts = new ArrayList<>();
    private List<Upgrade> upgrades = new ArrayList<>();
    private UpgradeManager upgradeManager = new UpgradeManager();
    private ImageIcon[] fruitIcons;
    private Random random = new Random();

    public BoardModel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.elements = new Object[rows][cols];

        // Debug: wypisz ścieżki do plików
        System.out.println("Szukam sprite-sheet-pacman.jpg w: " + new File("sprite-sheet-pacman.jpg").getAbsolutePath());
        System.out.println("Szukam point.png w: " + new File("point.png").getAbsolutePath());

        try {
            SpriteSheet spriteSheet = new SpriteSheet("/Users/piotr/Downloads/pacmangui/src/sprite-sheet-pacman.jpg");
            ImageIcon[][] pacmanFrames = spriteSheet.getPacmanFrames();
            pacman = new PacMan(rows / 2, cols / 2, pacmanFrames);

            for (int i = 0; i < 4; i++) {
                ImageIcon[][] ghostFrames = spriteSheet.getGhostFrames(i);
                Ghost ghost = new Ghost(2 + i, 2 + i, ghostFrames);
                ghosts.add(ghost);
            }

            fruitIcons = spriteSheet.getFruitIcons();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Nie można załadować pliku sprite-sheet-pacman.jpg lub point.png!\n" +
                            "Sprawdź, czy pliki są w katalogu projektu.\nSzczegóły: " + e.getMessage(),
                    "Błąd ładowania obrazka", JOptionPane.ERROR_MESSAGE);
            pacman = null;
        }

        initializeMap();
        startAnimationThreads();
        startUpgradeGenerationThread();
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColumnCount() {
        return cols;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return ImageIcon.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (pacman == null) return null; // zabezpieczenie przed NullPointerException

        for (Upgrade upgrade : upgrades) {
            if (upgrade.getRow() == row && upgrade.getCol() == col) {
                return upgrade.getIcon();
            }
        }
        if (pacman.getRow() == row && pacman.getCol() == col) {
            return pacman.getCurrentIcon();
        }
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) {
                return ghost.getCurrentIcon();
            }
        }
        if (elements[row][col] == MapElement.DOT) {
            return ImageResources.DOT_ICON;
        }
        if (elements[row][col] == MapElement.WALL) {
            return ImageResources.WALL_ICON;
        } else if (elements[row][col] == MapElement.EMPTY) {
            return ImageResources.EMPTY_ICON;
        }
        return null;
    }

    private void initializeMap() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 || j == 0 || i == rows - 1 || j == cols - 1) {
                    elements[i][j] = MapElement.WALL;
                } else {
                    elements[i][j] = MapElement.DOT;
                }
            }
        }
        elements[rows / 2][cols / 2] = MapElement.EMPTY;
    }

    private void startAnimationThreads() {
        Thread pacmanAnimationThread = new Thread(() -> {
            while (gameRunning) {
                try {
                    Thread.sleep(upgradeManager.isSpeedBoostActive() ? 125 : 250);
                } catch (InterruptedException e) {
                    break;
                }
                if (pacman != null) {
                    pacman.nextFrame();
                    fireTableCellUpdated(pacman.getRow(), pacman.getCol());
                }
            }
        });
        pacmanAnimationThread.setDaemon(true);
        pacmanAnimationThread.start();

        for (Ghost ghost : ghosts) {
            Thread ghostThread = new Thread(() -> {
                while (gameRunning) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        break;
                    }
                    int oldRow = ghost.getRow();
                    int oldCol = ghost.getCol();
                    ghost.move(this);
                    ghost.nextFrame();
                    checkGhostCollisions();
                    fireTableCellUpdated(oldRow, oldCol);
                    fireTableCellUpdated(ghost.getRow(), ghost.getCol());
                }
            });
            ghostThread.setDaemon(true);
            ghostThread.start();
        }
    }

    private void startUpgradeGenerationThread() {
        Thread upgradeThread = new Thread(() -> {
            while (gameRunning) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
                if (random.nextDouble() < 0.25) {
                    generateRandomUpgrade();
                }
                upgrades.removeIf(upgrade -> {
                    if (upgrade.isExpired()) {
                        fireTableCellUpdated(upgrade.getRow(), upgrade.getCol());
                        return true;
                    }
                    return false;
                });
            }
        });
        upgradeThread.setDaemon(true);
        upgradeThread.start();
    }

    private void generateRandomUpgrade() {
        List<int[]> emptySpaces = new ArrayList<>();
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (elements[i][j] == MapElement.EMPTY && !isOccupied(i, j)) {
                    emptySpaces.add(new int[]{i, j});
                }
            }
        }
        if (!emptySpaces.isEmpty()) {
            int[] pos = emptySpaces.get(random.nextInt(emptySpaces.size()));
            UpgradeType type = UpgradeType.values()[random.nextInt(UpgradeType.values().length)];
            ImageIcon icon = fruitIcons[random.nextInt(fruitIcons.length)];
            Upgrade upgrade = new Upgrade(pos[0], pos[1], type, icon);
            upgrades.add(upgrade);
            fireTableCellUpdated(pos[0], pos[1]);
        }
    }

    private boolean isOccupied(int row, int col) {
        if (pacman != null && pacman.getRow() == row && pacman.getCol() == col) return true;
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) return true;
        }
        for (Upgrade upgrade : upgrades) {
            if (upgrade.getRow() == row && upgrade.getCol() == col) return true;
        }
        return false;
    }

    private synchronized void checkGhostCollisions() {
        if (!gameRunning || upgradeManager.isInvincibilityActive() || pacman == null) {
            return;
        }
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == pacman.getRow() && ghost.getCol() == pacman.getCol()) {
                loseLife();
                return;
            }
        }
    }

    public synchronized void movePacman(int dx, int dy) {
        if (!gameRunning || pacman == null) return;
        int newRow = pacman.getRow() + dy;
        int newCol = pacman.getCol() + dx;

        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                && elements[newRow][newCol] != MapElement.WALL) {

            int oldRow = pacman.getRow();
            int oldCol = pacman.getCol();

            // Zbieranie punktu
            if (elements[newRow][newCol] == MapElement.DOT) {
                elements[newRow][newCol] = MapElement.EMPTY;
                addScore(10);
            }

            checkUpgradeCollision(newRow, newCol);

            pacman.setRow(newRow);
            pacman.setCol(newCol);
            pacman.setDirection(getDirectionFromDelta(dx, dy));
            checkGhostCollisions();

            fireTableCellUpdated(oldRow, oldCol);
            fireTableCellUpdated(newRow, newCol);
        }
    }

    private void checkUpgradeCollision(int row, int col) {
        Iterator<Upgrade> iterator = upgrades.iterator();
        while (iterator.hasNext()) {
            Upgrade upgrade = iterator.next();
            if (upgrade.getRow() == row && upgrade.getCol() == col) {
                applyUpgrade(upgrade.getType());
                iterator.remove();
                fireTableCellUpdated(row, col);
                break;
            }
        }
    }

    private void applyUpgrade(UpgradeType type) {
        switch (type) {
            case SPEED_BOOST:
                upgradeManager.activateUpgrade(type);
                addScore(upgradeManager.isDoublePointsActive() ? 200 : 100);
                break;
            case INVINCIBILITY:
                upgradeManager.activateUpgrade(type);
                addScore(upgradeManager.isDoublePointsActive() ? 300 : 150);
                break;
            case DOUBLE_POINTS:
                upgradeManager.activateUpgrade(type);
                addScore(upgradeManager.isDoublePointsActive() ? 400 : 200);
                break;
            case GHOST_FREEZE:
                upgradeManager.activateUpgrade(type);
                addScore(upgradeManager.isDoublePointsActive() ? 500 : 250);
                break;
            case TELEPORT:
                teleportPacman();
                addScore(upgradeManager.isDoublePointsActive() ? 600 : 300);
                break;
        }
    }

    private void teleportPacman() {
        List<int[]> emptySpaces = new ArrayList<>();
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (elements[i][j] == MapElement.EMPTY && !isOccupied(i, j)) {
                    emptySpaces.add(new int[]{i, j});
                }
            }
        }
        if (!emptySpaces.isEmpty()) {
            int[] pos = emptySpaces.get(random.nextInt(emptySpaces.size()));
            int oldRow = pacman.getRow();
            int oldCol = pacman.getCol();
            pacman.setRow(pos[0]);
            pacman.setCol(pos[1]);
            checkGhostCollisions();
            fireTableCellUpdated(oldRow, oldCol);
            fireTableCellUpdated(pos[0], pos[1]);
        }
    }

    private int getDirectionFromDelta(int dx, int dy) {
        if (dx == -1) return 0; // left
        if (dx == 1) return 1;  // right
        if (dy == -1) return 2; // up
        if (dy == 1) return 3;  // down
        return pacman.getDirection();
    }

    public MapElement getElementAt(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return MapElement.WALL;
        }
        return (MapElement) elements[row][col];
    }

    public synchronized void stopGame() {
        gameRunning = false;
    }

    public int getElapsedTime() {
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }

    public int getScore() { return score; }
    public int getLives() { return lives; }
    public void addScore(int pts) { score += pts; }
    public synchronized void loseLife() {
        if (!gameRunning) return;
        lives--;
        if (lives <= 0) {
            gameRunning = false;
        }
    }
    public boolean isGameRunning() { return gameRunning && lives > 0; }
    public UpgradeManager getUpgradeManager() { return upgradeManager; }
}
