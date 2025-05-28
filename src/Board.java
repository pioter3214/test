import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JTable {
    private BoardModel boardModel;

    Board(BoardModel model){
        super(model);
        this.boardModel = model;

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(32);
        }
        setRowHeight(32);

        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Skrót Ctrl+Shift+Q - powrót do menu
                if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_Q) {
                    boardModel.stopGame();
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(Board.this);
                    if (parentFrame != null) {
                        parentFrame.dispose();
                        SwingUtilities.invokeLater(() -> new Menu());
                    }
                    return;
                }

                // Sterowanie Pac-Manem
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) {
                    boardModel.movePacman(-1, 0);
                } else if (code == KeyEvent.VK_RIGHT) {
                    boardModel.movePacman(1, 0);
                } else if (code == KeyEvent.VK_UP) {
                    boardModel.movePacman(0, -1);
                } else if (code == KeyEvent.VK_DOWN) {
                    boardModel.movePacman(0, 1);
                }
            }
        });

        Thread repaintThread = new Thread(() -> {
            while (boardModel.isGameRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
                repaint();
            }
        });
        repaintThread.setDaemon(true);
        repaintThread.start();
    }
}
