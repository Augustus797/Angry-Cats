import java.awt.*;

public abstract class Entity {

    protected static final int LEFT_WALL   = 10;
    protected static final int RIGHT_WALL  = 750;
    protected static final int TOP_WALL    = 10;
    protected static final int BOTTOM_WALL = 550;

    protected double x;
    protected double y;
    protected Sprite sprite; 
    protected double dx;     
    protected double dy;    
    protected double angle; 

    protected boolean rotateSprite = false;

    protected boolean visible = true;

    private Rectangle me = new Rectangle();  //this hitbox
    private Rectangle him = new Rectangle(); //other hitbox

    public Entity(String r, int newX, int newY) {
        x = newX;
        y = newY;
        sprite = (SpriteStore.get()).getSprite(r);
    } // constructor

    public void move(long delta) {
        x += (delta * dx) / 1000;
        y += (delta * dy) / 1000;
    } // move

    public void setHorizontalMovement(double newDX) {
        dx = newDX;
    } // setHorizontalMovement

    public void setVerticalMovement(double newDY) {
        dy = newDY;
    } // setVerticalMovement

    public int getX() {
        return (int) x;
    } // getX

    public int getY() {
        return (int) y;
    } // getY

    public double getCenterX() {
        return x + sprite.getWidth() / 2.0;
    } // getCenterX

    public double getCenterY() {
        return y + sprite.getHeight() / 2.0;
    } // getCenterY

    public void setCenter(double cx, double cy) {
        x = cx - sprite.getWidth() / 2.0;
        y = cy - sprite.getHeight() / 2.0;
    } // setCenter

    public void setSprite(String r) {
        sprite = (SpriteStore.get()).getSprite(r);
    } // setSprite

    public void setVisible(boolean v) {
        visible = v;
    } // setVisible

    protected boolean xOutOfBounds() {
        return ((dx < 0) && (x < LEFT_WALL)) || ((dx > 0) && (x > RIGHT_WALL));
    } // xOutOfbounds

    protected boolean yOutOfBounds() {
        return ((dy < 0) && (y < TOP_WALL)) || ((dy > 0) && (y > BOTTOM_WALL));
    } // yOutOfBounds
    
    public void draw(Graphics g) {
        if (!visible) {
            return;
        } // if

        if (rotateSprite && angle != 0) {
            //rotate the sprite to always face the right direction
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.rotate(-angle, x + sprite.getWidth() / 2.0, y + sprite.getHeight() / 2.0);
            sprite.draw(g2d, (int) x, (int) y);
            g2d.dispose();
        } else {
            sprite.draw(g, (int) x, (int) y);
        } // else
    } // draw

    public boolean collidesWith(Entity other) {
        me.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
        him.setBounds(other.getX(), other.getY(), other.sprite.getWidth(), other.sprite.getHeight());
        return me.intersects(him);
    } // collidesWith

    public abstract void collidedWith(Entity other);
} // Entity class
