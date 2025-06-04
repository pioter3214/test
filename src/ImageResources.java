import javax.swing.*;

public class ImageResources {
    public static final ImageIcon WALL_ICON = loadImage("/wall1.png");
    public static final ImageIcon EMPTY_ICON = loadImage("/empty1.png");
    public static final ImageIcon DOT_ICON = loadImage("/point.png");

    private static ImageIcon loadImage(String path) {
        try {
            return new ImageIcon(ImageResources.class.getResource(path));
        } catch (Exception e) {
            System.err.println("Nie można załadować obrazka: " + path);
            return new ImageIcon(); //empty
        }
    }
}
