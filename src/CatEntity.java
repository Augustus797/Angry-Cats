public abstract class CatEntity extends Entity {

    private static final double MIN_POWER = 0.3;

    protected Main game;            // the game in which the shot exists
    protected int bounces = 1;      // # of bounces a cat has (default 1)
    protected double gravity = 0;
    public int cost = 1; 

    public CatEntity(Main g, String r, int centerX, int centerY) {
        super(r, 0, 0);       
        game = g;
        setCenter(centerX, centerY);
        angle = game.getAimAngle();
    } // constructor

    protected void launch(double speed, double power) {
        double s = speed * (MIN_POWER + (1 - MIN_POWER) * power);
        dx = s * Math.sin(angle);
        dy = s * Math.cos(angle);
    } // launch

    @Override
    public void move(long delta) {
        dy += gravity * delta / 1000;

        if (bounces > 0) {
            if (xOutOfBounds()) {
                dx = -dx;
                bounces--;
                return;
            } // if
            if (yOutOfBounds()) {
                dy = -dy;
                bounces--;
                return;
            } // if
        } // if

        super.move(delta);

        if (x < -100 || x > Main.SCREEN_W + 100 || y < -100 || y > Main.SCREEN_H + 100) {
            game.removeEntity(this);
        } // if
    } // move

    public void collidedWith(Entity other) {
        if (other instanceof DogEntity) {
            dogHit((DogEntity) other);
        } // if
    } // collidedWith

    protected void dogHit(DogEntity dog) {
        game.alienHit(dog);
    } // dogHit
} // CatEntity