import java.awt.*;

public class Button {

    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);

    private Rectangle bounds;
    private String label;      
    private Sprite icon;
    private boolean selected = false;

    public Button(String text, String spriteRef, int x, int y, int w, int h) {
        label = text;
        bounds = new Rectangle(x, y, w, h);
        if (spriteRef != null && !spriteRef.equals("")) {
            icon = (SpriteStore.get()).getSprite(spriteRef);
        } // if
    } // constructor

    //check if mouse is on the button
    public boolean contains(int px, int py) {
        return bounds.contains(px, py);
    } // contains

    public void setSelected(boolean s) {
        selected = s;
    } // setSelected

    public void draw(Graphics2D g, boolean hover) {
        // old when selected, light when hovered, else dark
        if (selected) {
            g.setColor(new Color(255, 205, 40, 235));
        } else if (hover) {
            g.setColor(new Color(255, 255, 255, 200));
        } else {
            g.setColor(new Color(0, 0, 0, 150));
        } // else
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);

        // border
        g.setColor(selected ? Color.WHITE : new Color(255, 255, 255, 160));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);

        // icon (36x36)
        if (icon != null) {
            g.drawImage(icon.image, bounds.x + (bounds.width - 36) / 2, bounds.y + 6, 36, 36, null);
        } // if

        g.setFont(LABEL_FONT);
        g.setColor((selected || hover) ? Color.BLACK : Color.WHITE);
        int textW = g.getFontMetrics().stringWidth(label);
        g.drawString(label, bounds.x + (bounds.width - textW) / 2, bounds.y + bounds.height - 9);
    } // draw
} // Button
