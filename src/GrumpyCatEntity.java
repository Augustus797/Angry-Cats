public class GrumpyCatEntity extends CatEntity {

    public static final String SPRITE = "sprites/grumpycat.gif";

    private static final int EXPLOSION_RANGE = 75;

    public GrumpyCatEntity(Main g, int centerX, int centerY, double power) {
        super(g, SPRITE, centerX, centerY);
        bounces = 1;
        cost = 2;
        launch(700, power);
    } // constructor

    @Override
    protected void dogHit(DogEntity dog) {
        double radius = sprite.getWidth() / 2.0 + EXPLOSION_RANGE;
        game.explode(getCenterX(), getCenterY(), radius);
        game.removeEntity(this);
    } // dogHit
} // GrumpyCatEntity