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
        int tileSize = 32; // zmienione na 16, bo sprite wydaje się mniejszy
        int x = frameIndex * tileSize;
        int y = 0;
        BufferedImage frame = sheet.getSubimage(x, y, tileSize, tileSize);
        return new ImageIcon(frame);
    }

    public ImageIcon[][] getPacmanFrames() {
        int tileSize = 32; // dostosowane do sprite sheeta
        ImageIcon[][] frames = new ImageIcon[4][3]; // 4 kierunki, 3 klatki

        // RIGHT (kierunek 1) - wiersz 1, kolumny 1,2,3
        frames[1][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 0 * tileSize, tileSize, tileSize)); // otwarta buzia
        frames[1][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 0 * tileSize, tileSize, tileSize)); // średnio otwarta
        frames[1][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize)); // zamknięta buzia

        // LEFT (kierunek 0) - wiersz 2, kolumny 1,2 + wiersz 1, kolumna 3 (zamknięta buzia)
        frames[0][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 1 * tileSize, tileSize, tileSize)); // otwarta w lewo
        frames[0][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 1 * tileSize, tileSize, tileSize)); // średnio otwarta w lewo
        frames[0][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize)); // zamknięta buzia

        // UP (kierunek 2) - wiersz 3, kolumny 1,2 + wiersz 1, kolumna 3 (zamknięta buzia)
        frames[2][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 2 * tileSize, tileSize, tileSize)); // otwarta w górę
        frames[2][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 2 * tileSize, tileSize, tileSize)); // średnio otwarta w górę
        frames[2][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize)); // zamknięta buzia

        // DOWN (kierunek 3) - wiersz 4, kolumny 1,2 + wiersz 1, kolumna 3 (zamknięta buzia)
        frames[3][0] = new ImageIcon(sheet.getSubimage(0 * tileSize, 3 * tileSize, tileSize, tileSize)); // otwarta w dół
        frames[3][1] = new ImageIcon(sheet.getSubimage(1 * tileSize, 3 * tileSize, tileSize, tileSize)); // średnio otwarta w dół
        frames[3][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0 * tileSize, tileSize, tileSize)); // zamknięta buzia

        return frames;
    }

    public ImageIcon[][] getGhostFrames(int ghostIndex) {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][2]; // 4 kierunki, 2 klatki

        // Duchy zaczynają się od wiersza 5 (indeks 4)
        int ghostRow = 4 + ghostIndex;

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 2; frame++) {
                int x = (dir * 2 + frame) * tileSize;
                int y = ghostRow * tileSize;
                BufferedImage img = sheet.getSubimage(x, y, tileSize, tileSize);
                frames[dir][frame] = new ImageIcon(img);
            }
        }
        return frames;
    }
}
