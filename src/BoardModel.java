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
        // Inicjalizacja - wszystko jako ściany
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                elements[i][j] = MapElement.WALL;
            }
        }

        // Generuj prawdziwy labirynt
        generatePerfectMaze();

        // Eliminuj ślepe uliczki
        eliminateDeadEnds();

        // Dodaj kropki w przejściach
        placeDots();

        // Zapewnij bezpieczną pozycję startową dla PacMana
        ensureSafePacmanStart();
    }

    private void generatePerfectMaze() {
        // Generator oparty na Recursive Backtracking z modyfikacjami
        boolean[][] visited = new boolean[rows][cols];
        Stack<int[]> stack = new Stack<>();

        // Rozpocznij od centrum (lub najbliższego nieparzystego punktu)
        int startRow = rows / 2;
        int startCol = cols / 2;
        if (startRow % 2 == 0) startRow--;
        if (startCol % 2 == 0) startCol--;

        // Upewnij się, że start jest w granicach
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

            // Znajdź nieodwiedzonych sąsiadów (oddalonych o 2 komórki)
            List<int[]> neighbors = getUnvisitedNeighbors(row, col, visited);

            if (!neighbors.isEmpty()) {
                // Wybierz losowego sąsiada
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                int nextRow = next[0];
                int nextCol = next[1];

                // Wykuj ścieżkę do sąsiada
                carvePath(row, col, nextRow, nextCol);
                visited[nextRow][nextCol] = true;
                stack.push(new int[]{nextRow, nextCol});
            } else {
                // Backtrack
                stack.pop();
            }
        }
    }

    private void carvePath(int fromRow, int fromCol, int toRow, int toCol) {
        // Wykuj komórkę docelową
        elements[toRow][toCol] = MapElement.EMPTY;

        // Wykuj ścianę między komórkami (dokładnie w połowie)
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

                // Z 40% prawdopodobieństwem dodaj połączenie
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
        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}}; // prawo, dół, lewo, góra

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

        // Stwórz 3x3 obszar bezpieczny dla PacMana
        for (int i = centerRow - 1; i <= centerRow + 1; i++) {
            for (int j = centerCol - 1; j <= centerCol + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < cols) {
                    elements[i][j] = MapElement.EMPTY;
                }
            }
        }
    }

    private void addInternalWalls() {
        // 1. Create maze with walls that don't create dead ends
        for (int i = 2; i < rows - 2; i += 2) {
            for (int j = 2; j < cols - 2; j += 2) {
                if (random.nextInt(100) < 35) { // 35% chance for wall placement
                    // Create a small wall section
                    elements[i][j] = MapElement.WALL;

                    // Randomly extend wall in ONE direction only
                    // This ensures we never create a dead end
                    int direction = random.nextInt(4);
                    if (direction == 0 && i > 2) elements[i-1][j] = MapElement.WALL;
                    else if (direction == 1 && i < rows - 3) elements[i+1][j] = MapElement.WALL;
                    else if (direction == 2 && j > 2) elements[i][j-1] = MapElement.WALL;
                    else if (direction == 3 && j < cols - 3) elements[i][j+1] = MapElement.WALL;
                }
            }
        }

        // 2. Add some horizontal and vertical paths
        createPathways();
    }

    private void createPathways() {
        // Horizontal paths
        for (int i = 4; i < rows - 4; i += 5) {
            // 50% chance to create a horizontal path
            if (random.nextInt(100) < 50) {
                for (int j = 1; j < cols - 1; j++) {
                    if (random.nextInt(100) < 70) { // 70% chance to clear cell
                        // Turn walls into dots to create paths
                        if (elements[i][j] == MapElement.WALL) {
                            elements[i][j] = MapElement.DOT;
                        }
                    }
                }
            }
        }

        // Vertical paths
        for (int j = 4; j < cols - 4; j += 5) {
            // 50% chance to create a vertical path
            if (random.nextInt(100) < 50) {
                for (int i = 1; i < rows - 1; i++) {
                    if (random.nextInt(100) < 70) { // 70% chance to clear cell
                        // Turn walls into dots to create paths
                        if (elements[i][j] == MapElement.WALL) {
                            elements[i][j] = MapElement.DOT;
                        }
                    }
                }
            }
        }

        // Ensure no isolated wall islands by breaking up big wall clusters
        breakUpLargeWallClusters();
    }

    private void breakUpLargeWallClusters() {
        for (int i = 2; i < rows - 2; i += 3) {
            for (int j = 2; j < cols - 2; j += 3) {
                // Check if this is part of a large wall cluster
                if (isLargeWallCluster(i, j)) {
                    // Break it up by adding a path
                    elements[i][j] = MapElement.DOT;
                }
            }
        }
    }

    private boolean isLargeWallCluster(int row, int col) {
        if (elements[row][col] != MapElement.WALL) return false;

        int wallCount = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + i;
                int c = col + j;
                if (r > 0 && r < rows - 1 && c > 0 && c < cols - 1) {
                    if (elements[r][c] == MapElement.WALL) {
                        wallCount++;
                    }
                }
            }
        }

        return wallCount >= 6; // If 6+ of 9 cells in 3x3 area are walls
    }


    // Główna metoda generowania labiryntu - algorytm Recursive Backtracking
    private void generateMaze() {
        // Najpierw wypełnij wszystko ścianami
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                elements[i][j] = MapElement.WALL;
            }
        }

        // Stwórz labirynt zaczynając od pozycji (1,1)
        boolean[][] visited = new boolean[rows][cols];
        carvePath(1, 1, visited);

        // Dodaj zewnętrzne ściany
        for (int i = 0; i < rows; i++) {
            elements[i][0] = MapElement.WALL;
            elements[i][cols - 1] = MapElement.WALL;
        }
        for (int j = 0; j < cols; j++) {
            elements[0][j] = MapElement.WALL;
            elements[rows - 1][j] = MapElement.WALL;
        }
    }

    // Rekurencyjna metoda drążenia ścieżek w labiryncie
    private void carvePath(int row, int col, boolean[][] visited) {
        visited[row][col] = true;
        elements[row][col] = MapElement.EMPTY;

        // Kierunki: góra, dół, lewo, prawo
        int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};

        // Mieszaj kierunki losowo
        shuffleArray(directions);

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            // Sprawdź czy nowa pozycja jest w granicach i nieodwiedzona
            if (newRow > 0 && newRow < rows - 1 &&
                    newCol > 0 && newCol < cols - 1 &&
                    !visited[newRow][newCol]) {

                // Usuń ścianę między obecną a nową pozycją
                int wallRow = row + direction[0] / 2;
                int wallCol = col + direction[1] / 2;
                elements[wallRow][wallCol] = MapElement.EMPTY;

                // Rekurencyjnie drąż dalej
                carvePath(newRow, newCol, visited);
            }
        }
    }

    // Pomocnicza metoda mieszania tablicy kierunków
    private void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int[] temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    // Dodaj kropki do pustych miejsc labiryntu
    private void addDotsToMaze() {
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (elements[i][j] == MapElement.EMPTY) {
                    // 80% szans na kropkę, 20% na puste pole
                    if (random.nextDouble() < 0.8) {
                        elements[i][j] = MapElement.DOT;
                    }
                }
            }
        }
    }

    // Alternatywna metoda - generator labiryntu w stylu PacMana
    private void generatePacmanStyleMaze() {
        // Wypełnij ścianami
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                elements[i][j] = MapElement.WALL;
            }
        }

        // Stwórz główne korytarze
        createMainCorridors();

        // Dodaj pokoje i dodatkowe ścieżki
        createRooms();

        // Dodaj kropki
        addDotsToMaze();
    }

    // Twórz główne korytarze w stylu klasycznego PacMana
    private void createMainCorridors() {
        // Poziomy korytarz w środku
        int midRow = rows / 2;
        for (int j = 1; j < cols - 1; j++) {
            elements[midRow][j] = MapElement.EMPTY;
        }

        // Pionowy korytarz w środku
        int midCol = cols / 2;
        for (int i = 1; i < rows - 1; i++) {
            elements[i][midCol] = MapElement.EMPTY;
        }

        // Dodatkowe korytarze
        for (int i = 2; i < rows - 2; i += 4) {
            for (int j = 1; j < cols - 1; j++) {
                if (random.nextDouble() < 0.7) {
                    elements[i][j] = MapElement.EMPTY;
                }
            }
        }

        for (int j = 2; j < cols - 2; j += 4) {
            for (int i = 1; i < rows - 1; i++) {
                if (random.nextDouble() < 0.7) {
                    elements[i][j] = MapElement.EMPTY;
                }
            }
        }
    }

    // Twórz pokoje w labiryncie
    private void createRooms() {
        int roomSize = 3;

        // Stwórz kilka małych pokojów
        for (int roomCount = 0; roomCount < Math.min(6, (rows * cols) / 100); roomCount++) {
            int startRow = random.nextInt(rows - roomSize - 2) + 1;
            int startCol = random.nextInt(cols - roomSize - 2) + 1;

            // Stwórz pokój
            for (int i = startRow; i < startRow + roomSize; i++) {
                for (int j = startCol; j < startCol + roomSize; j++) {
                    elements[i][j] = MapElement.EMPTY;
                }
            }

            // Połącz pokój z korytarzami
            connectRoomToMaze(startRow, startCol, roomSize);
        }
    }

    // Połącz pokój z głównym labiryntem
    private void connectRoomToMaze(int roomRow, int roomCol, int roomSize) {
        // Znajdź najbliższy korytarz i połącz z nim
        int centerRow = roomRow + roomSize / 2;
        int centerCol = roomCol + roomSize / 2;

        // Spróbuj połączyć w każdym kierunku
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int checkRow = centerRow + dir[0];
            int checkCol = centerCol + dir[1];

            while (checkRow > 0 && checkRow < rows - 1 &&
                    checkCol > 0 && checkCol < cols - 1) {

                elements[checkRow][checkCol] = MapElement.EMPTY;
                checkRow += dir[0];
                checkCol += dir[1];

                // Przerwij po 3-5 krokach lub gdy napotkasz istniejący korytarz
                if (random.nextInt(3) == 0 || elements[checkRow][checkCol] == MapElement.EMPTY) {
                    break;
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

            // NAJPIERW sprawdź kolizję PRZED ruchem
            if (checkCollisionAtPosition(newRow, newCol)) {
                System.out.println("Collision detected BEFORE move!"); // Debug
                loseLife();
                return;
            }

            // Przesuń PacMana
            if (elements[newRow][newCol] == MapElement.DOT) {
                elements[newRow][newCol] = MapElement.EMPTY;
                addScore(upgradeManager.isDoublePointsActive() ? 20 : 10);
            }

            checkUpgradeCollision(newRow, newCol);

            pacman.setRow(newRow);
            pacman.setCol(newCol);
            pacman.setDirection(getDirectionFromDelta(dx, dy));

            // PONOWNIE sprawdź kolizję PO ruchu
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
            case TELEPORT:
                teleportPacman();
                addScore(upgradeManager.isDoublePointsActive() ? 600 : 300);
                break;
        }
    }

    private void teleportPacman() {
        System.out.println("Teleport activated!");

        List<int[]> availableSpaces = new ArrayList<>();

        // Szukaj wszystkich dostępnych miejsc (EMPTY i DOT)
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if ((elements[i][j] == MapElement.EMPTY || elements[i][j] == MapElement.DOT)
                        && !isOccupiedByCharacter(i, j)) {
                    availableSpaces.add(new int[]{i, j});
                }
            }
        }

        if (!availableSpaces.isEmpty()) {
            Random random = new Random();
            int[] newPos = availableSpaces.get(random.nextInt(availableSpaces.size()));

            pacman.setRow(newPos[0]);
            pacman.setCol(newPos[1]);

            // Zbierz kropkę jeśli teleportacja nastąpiła na kropkę
            if (elements[newPos[0]][newPos[1]] == MapElement.DOT) {
//                collectDot(newPos[0], newPos[1]);
            }

            System.out.println("PacMan teleported to: (" + newPos[0] + "," + newPos[1] + ")");
        } else {
            System.out.println("No available spaces for teleport!");
        }
    }

    private boolean isOccupiedByCharacter(int row, int col) {
        // Sprawdź czy PacMan jest na tej pozycji
        if (pacman != null && pacman.getRow() == row && pacman.getCol() == col) {
            return true;
        }

        // Sprawdź czy któryś duch jest na tej pozycji
        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                if (ghost.getRow() == row && ghost.getCol() == col) {
                    return true;
                }
            }
        }

        return false;
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
