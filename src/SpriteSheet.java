import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteSheet {
    private BufferedImage sheet;

    public SpriteSheet(String resourceName) throws IOException {
        var is = getClass().getResourceAsStream("/" + resourceName);
        if (is == null) throw new IOException("Missing sprite file in resources: " + resourceName);
        sheet = ImageIO.read(is);
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
        ImageIcon[][] frames = new ImageIcon[4][3];
        frames[1][0] = new ImageIcon(sheet.getSubimage(0, 0, tileSize, tileSize));
        frames[1][1] = new ImageIcon(sheet.getSubimage(tileSize, 0, tileSize, tileSize));
        frames[1][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0, tileSize, tileSize));
        frames[0][0] = new ImageIcon(sheet.getSubimage(0, tileSize, tileSize, tileSize));
        frames[0][1] = new ImageIcon(sheet.getSubimage(tileSize, tileSize, tileSize, tileSize));
        frames[0][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0, tileSize, tileSize));
        frames[2][0] = new ImageIcon(sheet.getSubimage(0, 2 * tileSize, tileSize, tileSize));
        frames[2][1] = new ImageIcon(sheet.getSubimage(tileSize, 2 * tileSize, tileSize, tileSize));
        frames[2][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0, tileSize, tileSize));
        frames[3][0] = new ImageIcon(sheet.getSubimage(0, 3 * tileSize, tileSize, tileSize));
        frames[3][1] = new ImageIcon(sheet.getSubimage(tileSize, 3 * tileSize, tileSize, tileSize));
        frames[3][2] = new ImageIcon(sheet.getSubimage(2 * tileSize, 0, tileSize, tileSize));
        return frames;
    }

    public ImageIcon[][] getGhostFrames(int ghostIndex) {
        int tileSize = 32;
        ImageIcon[][] frames = new ImageIcon[4][2];
        int y = (4 + ghostIndex) * tileSize;
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
        for (int i = 0; i < 4; i++) {
            int x = (2 + i) * tileSize;
            int y = 3 * tileSize;
            BufferedImage img = sheet.getSubimage(x, y, tileSize, tileSize);
            fruits[i] = new ImageIcon(img);
        }
        return fruits;
    }
}
