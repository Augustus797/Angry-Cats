public class PlayerEntity extends Entity {

    private Main game;      
    private double pullX = 0;  // how far the cat is pulled back from the sling
    private double pullY = 0;

    public PlayerEntity(Main g, String r, int newX, int newY) {
        super(r, newX, newY); 
        game = g;
    } // constructor

    public void setPull(double px, double py) {
        pullX = px;
        pullY = py;
    } // setPull


    @Override
    public void move(long delta) {
        if (xOutOfBounds() || yOutOfBounds()) {
            return;
        } // if
        super.move(delta);
    } // move

    @Override
    public void draw(java.awt.Graphics g) {
        if (!visible) {
            return;
        } // if
        sprite.draw(g, (int) (x + pullX), (int) (y + pullY));
    } // draw

    public void collidedWith(Entity other) {
        if (other instanceof DogEntity) {
            game.notifyDeath();
        } // if
    } // collidedWith
}