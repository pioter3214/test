import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JTable {
    Board(BoardModel model){
        super(model);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(32);
        }
        setRowHeight(32);

        // DODANE - zapewnienie focus
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                BoardModel model = (BoardModel) getModel();
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) {
                    model.movePacman(-1, 0);
                } else if (code == KeyEvent.VK_RIGHT) {
                    model.movePacman(1, 0);
                } else if (code == KeyEvent.VK_UP) {
                    model.movePacman(0, -1);
                } else if (code == KeyEvent.VK_DOWN) {
                    model.movePacman(0, 1);
                }
            }
        });

        Thread repaintThread = new Thread(() -> {
            while (true) {
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
