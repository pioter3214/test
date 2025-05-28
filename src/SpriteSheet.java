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

    public ImageIcon[][] getPacmanFrames() {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][3]; // 4 kierunki, 3 klatki

        // RIGHT (kierunek 1) - wiersz 1, kolumny 1,2,3
        frames[1][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 0 * tileSize, tileSize, tileSize));
        frames[1][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 0 * tileSize, tileSize, tileSize));
        frames[1][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize));

        // LEFT (kierunek 0) - wiersz 2, kolumny 1,2 + wiersz 1, kolumna 3
        frames[0][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 1 * tileSize, tileSize, tileSize));
        frames[0][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 1 * tileSize, tileSize, tileSize));
        frames[0][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize));

        // UP (kierunek 2) - wiersz 3, kolumny 1,2 + wiersz 1, kolumna 3
        frames[2][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 2 * tileSize, tileSize, tileSize));
        frames[2][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 2 * tileSize, tileSize, tileSize));
        frames[2][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize));

        // DOWN (kierunek 3) - wiersz 4, kolumny 1,2 + wiersz 1, kolumna 3
        frames[3][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 3 * tileSize, tileSize, tileSize));
        frames[3][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 3 * tileSize, tileSize, tileSize));
        frames[3][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize));

        return frames;
    }

    public ImageIcon[][] getGhostFrames(int ghostIndex) {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][2]; // 4 kierunki, 2 klatki

        // Duchy zaczynają się od wiersza 5 (indeks 4)
        int y = (4 + ghostIndex) * tileSize; // wiersz ducha

        ImageIcon ghostFrame1 = new ImageIcon(sheet.getSubimage(0, y, tileSize, tileSize));
        ImageIcon ghostFrame2 = new ImageIcon(sheet.getSubimage(tileSize, y, tileSize, tileSize));

        for (int dir = 0; dir < 4; dir++) {
            frames[dir][0] = ghostFrame1;
            frames[dir][1] = ghostFrame2;
        }

        return frames;
    }

    public ImageIcon[] getFruitIcons() {
        int tileSize = 32;
        ImageIcon[] fruits = new ImageIcon[4];

        // Owoce w wierszu 4 (indeks 3), kolumny 3,4,5,6 (indeksy 2,3,4,5)
        for (int i = 0; i < 4; i++) {
            int x = (2 + i) * tileSize; // kolumny 3,4,5,6
            int y = 3 * tileSize;       // wiersz 4
            BufferedImage img = sheet.getSubimage(x, y, tileSize, tileSize);
            fruits[i] = new ImageIcon(img);
        }
        return fruits;
    }
}
