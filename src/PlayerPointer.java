public class PlayerPointer extends Entity {

    public PlayerPointer(Main g, String r, int newX, int newY) {
        super(r, newX, newY);
        rotateSprite = true;
    } // constructor

    @Override
    public void collidedWith(Entity other) {
        // the pointer is purely visual
    } // collidedWith
} // PlayerPointer
