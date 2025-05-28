import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoardModel extends AbstractTableModel {
    private int rows;
    private int cols;
    private Object[][] elements;
    private int score = 0;
    private int lives = 3;
    private long startTime = System.currentTimeMillis();

    private PacMan pacman;
    private List<Ghost> ghosts = new ArrayList<>();

    public BoardModel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.elements = new Object[rows][cols];

        try {
            SpriteSheet spriteSheet = new SpriteSheet("/Users/piotr/Downloads/PACMAN/src/sprite-sheet-pacman.jpg");

            // Załaduj klatki animacji PacMana
            ImageIcon[][] pacmanFrames = spriteSheet.getPacmanFrames();
            pacman = new PacMan(rows / 2, cols / 2, pacmanFrames);

            // Załaduj klatki animacji duchów i dodaj duchy
            for (int i = 0; i < 4; i++) {
                ImageIcon[][] ghostFrames = spriteSheet.getGhostFrames(i);
                Ghost ghost = new Ghost(2 + i, 2 + i, ghostFrames);
                ghosts.add(ghost);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeMap();
        startAnimationThreads();
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
        // Najpierw sprawdź postacie
        if (pacman.getRow() == row && pacman.getCol() == col) {
            return pacman.getCurrentIcon();
        }
        for (Ghost ghost : ghosts) {
            if (ghost.getRow() == row && ghost.getCol() == col) {
                return ghost.getCurrentIcon();
            }
        }

        // Potem elementy mapy
        if (elements[row][col] == MapElement.WALL) {
            return ImageResources.WALL_ICON;
        } else if(elements[row][col] == MapElement.EMPTY) {
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
                    elements[i][j] = MapElement.EMPTY;
                }
            }
        }
    }

    private void startAnimationThreads() {
        // Wątek animacji PacMana
        Thread pacmanAnimationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
                pacman.nextFrame();
                fireTableCellUpdated(pacman.getRow(), pacman.getCol());
            }
        });
        pacmanAnimationThread.setDaemon(true);
        pacmanAnimationThread.start();

        // Wątki dla duchów
        for (Ghost ghost : ghosts) {
            Thread ghostThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        break;
                    }
                    int oldRow = ghost.getRow();
                    int oldCol = ghost.getCol();

                    ghost.move(this);
                    ghost.nextFrame();

                    // Odśwież stare i nowe pole
                    fireTableCellUpdated(oldRow, oldCol);
                    fireTableCellUpdated(ghost.getRow(), ghost.getCol());
                }
            });
            ghostThread.setDaemon(true);
            ghostThread.start();
        }
    }

    public void movePacman(int dx, int dy) {
        int newRow = pacman.getRow() + dy;
        int newCol = pacman.getCol() + dx;

        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                && elements[newRow][newCol] != MapElement.WALL) {

            int oldRow = pacman.getRow();
            int oldCol = pacman.getCol();

            pacman.setRow(newRow);
            pacman.setCol(newCol);
            pacman.setDirection(getDirectionFromDelta(dx, dy));

            fireTableCellUpdated(oldRow, oldCol);
            fireTableCellUpdated(newRow, newCol);
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

    public int getElapsedTime() {
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }

    public int getScore() { return score; }
    public int getLives() { return lives; }
    public void addScore(int pts) { score += pts; }
    public void loseLife() { lives--; }
}
