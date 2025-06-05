import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.*;

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


        try {
            SpriteSheet spriteSheet = new SpriteSheet("sprite-sheet-pacman.jpg");
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
                    "Cannot load file" + e.getMessage(),
                    "Error loading file", JOptionPane.ERROR_MESSAGE);
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
        //PacMan
        if (pacman != null && pacman.getRow() == row && pacman.getCol() == col) {
            return pacman.getCurrentIcon();
        }
        //Ghost
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) {
                return ghost.getCurrentIcon();
            }
        }
        //Upgrade
        for (Upgrade upgrade : upgrades) {
            if (upgrade.getRow() == row && upgrade.getCol() == col) {
                return upgrade.getIcon();
            }
        }
        //Point
        if (elements[row][col] == MapElement.DOT) {
            return ImageResources.DOT_ICON;
        }
        //Empty
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
                elements[i][j] = MapElement.WALL;
            }
        }

        generatePerfectMaze();

        eliminateDeadEnds();

        placeDots();

        ensureSafePacmanStart();
    }

    private void generatePerfectMaze() {
        boolean[][] visited = new boolean[rows][cols];
        Stack<int[]> stack = new Stack<>();

        int startRow = rows / 2;
        int startCol = cols / 2;
        if (startRow % 2 == 0) startRow--;
        if (startCol % 2 == 0) startCol--;

        startRow = Math.max(1, Math.min(startRow, rows - 2));
        startCol = Math.max(1, Math.min(startCol, cols - 2));

        elements[startRow][startCol] = MapElement.EMPTY;
        visited[startRow][startCol] = true;
        stack.push(new int[]{startRow, startCol});

        Random random = new Random();

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int row = current[0];
            int col = current[1];

            List<int[]> neighbors = getUnvisitedNeighbors(row, col, visited);

            if (!neighbors.isEmpty()) {
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                int nextRow = next[0];
                int nextCol = next[1];

                carvePath(row, col, nextRow, nextCol);
                visited[nextRow][nextCol] = true;
                stack.push(new int[]{nextRow, nextCol});
            } else {
                stack.pop();
            }
        }
    }

    private void carvePath(int fromRow, int fromCol, int toRow, int toCol) {
        elements[toRow][toCol] = MapElement.EMPTY;

        int wallRow = fromRow + (toRow - fromRow) / 2;
        int wallCol = fromCol + (toCol - fromCol) / 2;
        elements[wallRow][wallCol] = MapElement.EMPTY;
    }


    private void eliminateDeadEnds() {
        boolean changed = true;
        Random random = new Random();

        while (changed) {
            changed = false;
            List<int[]> deadEnds = findDeadEnds();

            for (int[] deadEnd : deadEnds) {
                int row = deadEnd[0];
                int col = deadEnd[1];

                if (random.nextDouble() < 0.4) {
                    if (addRandomConnection(row, col)) {
                        changed = true;
                    }
                }
            }
        }
    }

    private List<int[]> findDeadEnds() {
        List<int[]> deadEnds = new ArrayList<>();

        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (elements[i][j] != MapElement.WALL && countOpenNeighbors(i, j) == 1) {
                    deadEnds.add(new int[]{i, j});
                }
            }
        }
        return deadEnds;
    }

    private boolean addRandomConnection(int row, int col) {
        List<int[]> possibleConnections = new ArrayList<>();
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};

        for (int[] dir : directions) {
            int targetRow = row + dir[0];
            int targetCol = col + dir[1];
            int wallRow = row + dir[0] / 2;
            int wallCol = col + dir[1] / 2;

            if (targetRow > 0 && targetRow < rows - 1 &&
                    targetCol > 0 && targetCol < cols - 1 &&
                    elements[targetRow][targetCol] != MapElement.WALL &&
                    elements[wallRow][wallCol] == MapElement.WALL) {
                possibleConnections.add(new int[]{wallRow, wallCol});
            }
        }

        if (!possibleConnections.isEmpty()) {
            Random random = new Random();
            int[] connection = possibleConnections.get(random.nextInt(possibleConnections.size()));
            elements[connection[0]][connection[1]] = MapElement.EMPTY;
            return true;
        }
        return false;
    }

    private int countOpenNeighbors(int row, int col) {
        int count = 0;
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < rows &&
                    newCol >= 0 && newCol < cols &&
                    elements[newRow][newCol] != MapElement.WALL) {
                count++;
            }
        }
        return count;
    }

    private List<int[]> getUnvisitedNeighbors(int row, int col, boolean[][] visited) {
        List<int[]> neighbors = new ArrayList<>();
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow > 0 && newRow < rows - 1 &&
                    newCol > 0 && newCol < cols - 1 &&
                    !visited[newRow][newCol]) {
                neighbors.add(new int[]{newRow, newCol});
            }
        }
        return neighbors;
    }

    private void placeDots() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (elements[i][j] == MapElement.EMPTY) {
                    elements[i][j] = MapElement.DOT;
                }
            }
        }
    }

    private void ensureSafePacmanStart() {
        int centerRow = rows / 2;
        int centerCol = cols / 2;

        for (int i = centerRow - 1; i <= centerRow + 1; i++) {
            for (int j = centerCol - 1; j <= centerCol + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    elements[i][j] = MapElement.EMPTY;
                }
            }
        }
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
                        int sleepTime = upgradeManager.isSpeedBoostActive() ? 200 : 400;
                        Thread.sleep(sleepTime);
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

    synchronized void checkGhostCollisions() {
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

            if (checkCollisionAtPosition(newRow, newCol)) {
                loseLife();
                return;
            }

            if (elements[newRow][newCol] == MapElement.DOT) {
                elements[newRow][newCol] = MapElement.EMPTY;
                addScore(upgradeManager.isDoublePointsActive() ? 20 : 10);
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

    private boolean checkCollisionAtPosition(int row, int col) {
        if (!gameRunning || upgradeManager.isInvincibilityActive()) {
            return false;
        }

        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) {
                return true;
            }
        }

        return false;
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
            case EXTRA_LIFE:
                System.out.println("add extra life");
                if (lives < 4) lives++;
                break;
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

    public int getScore() {
        return score;
    }
    public int getLives() {
        return lives;
    }
    public void addScore(int pts) {
        score += pts;
    }
    public synchronized void loseLife() {
        if (!gameRunning) return;
        lives--;
        if (lives <= 0) {
            gameRunning = false;
        }
    }
    public boolean isGameRunning() {
        return gameRunning && lives > 0;
    }
    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
}
