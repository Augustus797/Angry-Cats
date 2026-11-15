public class HappyCatEntity extends CatEntity {

    public static final String SPRITE = "sprites/happycat.gif";

    public HappyCatEntity(Main g, int centerX, int centerY, double power) {
        super(g, SPRITE, centerX, centerY);
        bounces = 3;
        cost = 2;
        gravity = 700;
        launch(650, power);
    } // constructor
} // HappyCatEntity