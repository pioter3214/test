import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class YellowButton extends JButton {
    public YellowButton(String text){
        super(text);
        setBackground(Color.BLACK);
        setOpaque(true);
        setForeground(Color.YELLOW);
        Border yellowBorder = BorderFactory.createLineBorder(Color.YELLOW, 3);
        setBorder(yellowBorder);
    }
}
