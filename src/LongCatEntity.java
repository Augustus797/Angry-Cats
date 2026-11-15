
public class LongCatEntity extends CatEntity{
	public static final String SPRITE = "sprites/longcat.gif";

    public LongCatEntity(Main g, int centerX, int centerY, double power) {
        super(g, SPRITE, centerX, centerY);
        bounces = 1;
        gravity = 250;
        cost = 3;
        launch(700, power);
    } // constructor
}
