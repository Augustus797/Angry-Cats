public class PopCatEntity extends CatEntity {

    public static final String SPRITE = "sprites/popcatopen.gif";

    public PopCatEntity(Main g, int centerX, int centerY, double power) {
        super(g, SPRITE, centerX, centerY);
        bounces = 1;
        launch(700, power);
    } // constructor
} // PopCatEntity
