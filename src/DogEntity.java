public class DogEntity extends Entity {

    public DogEntity(String r, int newX, int newY) {
        super(r, newX, newY);
    } // constructor


    public void collidedWith(Entity other) {
        // collisions with dogs are handled in CatEntity and PlayerEntity
    } // collidedWith
} // DogEntity class
