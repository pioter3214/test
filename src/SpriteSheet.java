import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpriteSheet {
    private BufferedImage sheet;

    public SpriteSheet(String path) throws IOException {
        sheet = ImageIO.read(new File(path));
    }

    public ImageIcon getPacmanFrame(int frameIndex) {
        int tileSize = 32;
        int x = frameIndex * tileSize;
        int y = 0;
        BufferedImage frame = sheet.getSubimage(x, y, tileSize, tileSize);
        return new ImageIcon(frame);
    }

    // DODANE - ładowanie PacMana dla wszystkich kierunków
    public ImageIcon[][] getPacmanFrames() {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][3]; // 4 kierunki, 3 klatki

        // Na podstawie sprite sheeta - dostosuj pozycje
        int[] rowOffsets = {1, 0, 2, 3}; // lewo, prawo, góra, dół

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 3; frame++) {
                int x = frame * tileSize;
                int y = rowOffsets[dir] * tileSize;
                BufferedImage img = sheet.getSubimage(x, y, tileSize, tileSize);
                frames[dir][frame] = new ImageIcon(img);
            }
        }
        return frames;
    }

    // DODANE - ładowanie duchów
    public ImageIcon[][] getGhostFrames(int ghostIndex) {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][2]; // 4 kierunki, 2 klatki

        // Duchy są w dolnej części sprite sheeta
        int startRow = 4 + ghostIndex; // zaczynamy od wiersza 4

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 2; frame++) {
                int x = (dir * 2 + frame) * tileSize;
                int y = startRow * tileSize;
                BufferedImage img = sheet.getSubimage(x, y, tileSize, tileSize);
                frames[dir][frame] = new ImageIcon(img);
            }
        }
        return frames;
    }
}
