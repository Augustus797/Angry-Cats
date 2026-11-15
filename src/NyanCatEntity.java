
public class NyanCatEntity extends CatEntity{
	
	public static final String SPRITE = "sprites/nyancat.gif";
	
	public NyanCatEntity(Main g, int centerX, int centerY, double power) {
		super(g, SPRITE, centerX, centerY);
		bounces = 0;
		cost = 3;
		launch(1500, power);
	} // constructor
} // NyanCatEntity
