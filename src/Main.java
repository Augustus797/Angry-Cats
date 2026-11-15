import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Canvas {
	
    public static final int SCREEN_W = 800;
    public static final int SCREEN_H = 600;

    private static final double MAX_PULL = 100;    // how far you can drag the cat
    private static final double MIN_PULL = 15;     // thereshold for shot firing (less = cancel)
    private static final double GRAB_RADIUS = 60;  // how close you have to click the cat

    private static final Font HUD_FONT  = new Font("Arial", Font.BOLD, 28);
    private static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 14);

    private static final String[] CAT_NAMES   = {"Pop Cat", "Happy Cat", "Grumpy Cat", "Nyan Cat", "Long Cat"};
    private static final String[] CAT_SPRITES = {PopCatEntity.SPRITE, HappyCatEntity.SPRITE, GrumpyCatEntity.SPRITE, NyanCatEntity.SPRITE, LongCatEntity.SPRITE};
    private CatEntity shot;

    private BufferStrategy strategy;
    private volatile boolean waitingForKeyPress = false;
    private boolean upPressed = false;    // true if up key (W) is pressed
    private boolean downPressed = false;  // true if down key (S) is pressed
    private volatile boolean paused = false;
    private volatile boolean displayMenu = true;

    private boolean gameRunning = true;
    private ArrayList<Entity> entities = new ArrayList<Entity>();       // list of entities in game
    private ArrayList<Entity> removeEntities = new ArrayList<Entity>(); // list of entities to remove
    private ArrayList<Button> buttons = new ArrayList<Button>();        // cat buttons
    private ArrayList<Button> menuButtons = new ArrayList<Button>();    // menu buttons 

    private PlayerEntity player;
    private Entity pointer;
    private double moveSpeed = 600;
    private double radius = 40;        // how far the pointer is from the cat (px)
    private long lastFire = 0;         // the time the last shot fired
    private long firingInterval = 500;
    private int alienCount;            
    private int ammo;  
    private int level;
    private BufferedImage background;
    private BufferedImage logo;
    private boolean levelWon = false;
    private boolean levelSelected = false;

    public int mouseX;
    public int mouseY;

    private volatile boolean dragging = false;
    private volatile boolean fireRequested = false;
    private volatile boolean newGame = false;
    private volatile int cat = 0;

    private double pullX = 0;       // how far the cat is pulled from the sling
    private double pullY = 0;
    private double pullLength = 0;  // length of the pull
    private double aimAngle = 0;

    private String message = "";

    public Main() {
        JFrame container = new JFrame("Angry Cats");

        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        panel.setLayout(null);

        setBounds(0, 0, SCREEN_W, SCREEN_H);
        panel.add(this);

        setIgnoreRepaint(true);

        try {
            background = ImageIO.read(new File("images/background.png"));
            logo = ImageIO.read(new File("images/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        } // catch

        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            } // windowClosing
        });

        addKeyListener(new KeyInputHandler());
        addMouseListener(new MouseInputHandler());
        requestFocus();

        createBufferStrategy(3);
        strategy = getBufferStrategy();
        
        initPlayer();
        drawMenu((Graphics2D) strategy.getDrawGraphics());
        IO.println("main: " + level);
        initButtons();
        initEntities();
        gameLoop();
    } //Main

    private void initButtons() {
        int w = 80;
        int h = 70;
        int gap = 10;
        for (int i = 0; i < CAT_NAMES.length; i++) {
            int bx = SCREEN_W - (CAT_NAMES.length - i) * (w + gap);
            int by = SCREEN_H - h - 10;
            buttons.add(new Button(CAT_NAMES[i], CAT_SPRITES[i], bx, by, w, h));
        } // for
    } // initButtons

    private void initPlayer() {
    	player = new PlayerEntity(this, CAT_SPRITES[cat], 100, 500);
        entities.add(player);
    }
    
    private void initEntities() {
    	if (levelSelected) {
    		pointer = new PlayerPointer(this, "sprites/pointer.gif", 0, 0);
            pointer.setVisible(false);
            entities.add(pointer);

            // dogs at random spots
            alienCount = 0;
            int[][] dogs = Levels.getDogs(level);
            IO.println("initEntity dog: " + level);
    		for (int i = 0; i < dogs.length; i++) {
    			entities.add(new DogEntity("sprites/dog.gif", dogs[i][0], dogs[i][1]));
    			alienCount++;
    		}

            ammo = Levels.getAmmo(level);
            IO.println("initEntity ammo: " + level);
    	}
    	else {do {
    		drawMenu((Graphics2D) strategy.getDrawGraphics());
    	}while(!levelSelected);
    	if (levelSelected) {
    		initEntities();
    	}
    	}
    } // initEntities

    public void addEntity(Entity e) {
        entities.add(e);
    } // addEntity

    public void removeEntity(Entity entity) {
        removeEntities.add(entity);
    } // removeEntity

    public void notifyDeath() {
        message = "You failed to eradicate the dogs! Try again?";
        levelSelected = false;
        waitingForKeyPress = true;
        drawMenu((Graphics2D) strategy.getDrawGraphics());
    } // notifyDeath

    public void notifyWin() {
    	levelSelected = false;
        message = (level + 1 < Levels.LEVEL_COUNT) ? "You eradicated all the dogs here. Onwards!" : "You eradicated all dogs from existance!";
        waitingForKeyPress = true;
        drawMenu((Graphics2D) strategy.getDrawGraphics());
    } // notifyWin

    public void notifyAlienKilled() {
        alienCount--;

        if (alienCount <= 0) {
            notifyWin();
            
        } // if
    } // notifyAlienKilled

    public void alienHit(DogEntity dog) {
        if (removeEntities.contains(dog)) {
            return;
        } // if
        removeEntity(dog);
        notifyAlienKilled();
    } // alienHit

	//create new square bomb entity for explosion and kill all that collides
    public void explode(double cx, double cy, double explosionRadius) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity instanceof DogEntity) {
                double distX = entity.getCenterX() - cx;
                double distY = entity.getCenterY() - cy;
                if (Math.sqrt(distX * distX + distY * distY) <= explosionRadius) {
                    alienHit((DogEntity) entity);
                } // if
            } // if
        } // for
    } // explode

    public double getAimAngle() {
        return pointer.angle;
    } // getAimAngle

    private boolean isLoaded() {
        return ammo > 0 && (System.currentTimeMillis() - lastFire) >= firingInterval;
    } // isLoaded

    private int countShots() {
        int count = 0;
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i) instanceof CatEntity) {
                count++;
            } // if
        } // for
        return count;
    } // countShots

    private void fireCat() {
        if (pullLength < MIN_PULL || !isLoaded()) {
            return;
        } // if

        double power = pullLength / MAX_PULL;
        int startX = (int) (player.getCenterX() + pullX);
        int startY = (int) (player.getCenterY() + pullY);

        if (cat == 0) {
            shot = new PopCatEntity(this, startX, startY, power);
        } 
        else if (cat == 1) {
            shot = new HappyCatEntity(this, startX, startY, power * 2);
        } 
        else if (cat == 2) {
            shot = new GrumpyCatEntity(this, startX, startY, power);
        }
        else if (cat == 3) {
        	shot = new NyanCatEntity(this, startX, startY, power);
        }
        else if(cat == 4) {
        	shot = new LongCatEntity(this, startX, startY, power * 2);
        }
        
        if (ammo - shot.cost >= 0) {
        	addEntity(shot);
        	ammo -= shot.cost;
        }
        
        lastFire = System.currentTimeMillis();
    } // fireCat

    public void drawMenu(Graphics2D g) {
        paused = true;
        float alpha = 1f; // 0 is transparent, 1 is opaque
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g.setColor(Color.BLUE);
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);

        g.setComposite(AlphaComposite.SrcOver);
        
        g.drawImage(background, 0, 0, SCREEN_W, SCREEN_H, null);
        g.drawImage(logo, 150, 10, logo.getWidth() * 2, logo.getHeight() * 2, null);
        
        menuButtons.add(new Button("", "sprites/level1.png", 200, 275, 64, 64));
        menuButtons.add(new Button("", "sprites/level2.png", 400, 275, 64, 64));
        menuButtons.add(new Button("", "sprites/level3.png", 600, 275, 64, 64));
        
        for (int i = 0; i < menuButtons.size(); i++) {
            Button b = menuButtons.get(i);
            b.draw(g, b.contains(mouseX, mouseY));
        } // for
        do {
    		g.dispose();
            strategy.show();
    	}while(displayMenu);
    } // drawMenu

    private void updateMouse() {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) {
            return;
        } // if
        Point p = new Point(info.getLocation());
        try {
            SwingUtilities.convertPointFromScreen(p, this);
        } catch (Exception ex) {
        } // try
        mouseX = p.x;
        mouseY = p.y;
    } // updateMouse

    private void updateDrag() {
        if (!dragging) {
            player.setPull(0, 0);
            pointer.setVisible(false);
            return;
        } // if

        double ax = player.getCenterX();
        double ay = player.getCenterY();

        double px = mouseX - ax;
        double py = mouseY - ay;
        double len = Math.sqrt(px * px + py * py);
        if (len > MAX_PULL) {
            px = px * MAX_PULL / len;
            py = py * MAX_PULL / len;
        } // if

        double catX = Math.max(25, Math.min(SCREEN_W - 25, ax + px));
        double catY = Math.max(25, Math.min(SCREEN_H - 25, ay + py));
        px = catX - ax;
        py = catY - ay;

        pullX = px;
        pullY = py;
        pullLength = Math.sqrt(px * px + py * py);
        player.setPull(px, py);

        // fire to the opposite direction
        if (pullLength > 0) {
            aimAngle = Math.atan2(-px, -py);
        } // if

        pointer.setVisible(true);
        drawPointer();
    } // updateDrag

    public void drawPointer() {
        double dist = radius + pullLength;
        pointer.setCenter(player.getCenterX() + dist * Math.sin(aimAngle), player.getCenterY() + dist * Math.cos(aimAngle));
        pointer.angle = aimAngle;
    } // drawPointer

    private void updateButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setSelected(i == cat);
        } // for
        for (int i = 0; i < menuButtons.size(); i++) {
            menuButtons.get(i).setSelected(i == level);
        } // for
        player.setSprite(CAT_SPRITES[cat]);
        player.setVisible(isLoaded());      
    } // updateButtons
    

    private void drawShadowText(Graphics2D g, String text, int tx, int ty) {
        g.setColor(Color.BLACK);
        g.drawString(text, tx + 2, ty + 2);
        g.setColor(Color.WHITE);
        g.drawString(text, tx, ty);
    } // drawShadowText

    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        while (gameRunning) {
            if (newGame) {
                newGame = false;
                waitingForKeyPress = false;
                startGame();
            } // if
            if (fireRequested) {
                fireRequested = false;
                fireCat();
            } // if

            updateMouse();
            updateDrag();
            updateButtons();

			//time since last loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            player.setHorizontalMovement(0);
            player.setVerticalMovement(0);
            if (upPressed && !downPressed) {
                player.setVerticalMovement(-moveSpeed);
            } else if (downPressed && !upPressed) {
                player.setVerticalMovement(moveSpeed);
            } // else
            if (!displayMenu) {menuButtons.clear();}
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.black);
            g.fillRect(0, 0, SCREEN_W, SCREEN_H);
            g.drawImage(background, 0, 0, SCREEN_W, SCREEN_H, null);

            if (dragging) {
                g.setColor(new Color(90, 50, 20));
                g.setStroke(new BasicStroke(6));
                g.drawLine((int) player.getCenterX(), (int) player.getCenterY(), (int) (player.getCenterX() + pullX), (int) (player.getCenterY() + pullY));
                g.setStroke(new BasicStroke(1));
            } // if

            // move entities
            if (!paused && !displayMenu) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = entities.get(i);
                    entity.move(delta);
                } // for
            } // if

            // draw entities
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                entity.draw(g);
            } // for

            // draw buttons
            for (int i = 0; i < buttons.size(); i++) {
                Button b = buttons.get(i);
                b.draw(g, b.contains(mouseX, mouseY));
            } // for
            
            for (int i = 0; i < menuButtons.size(); i++) {
                Button b = menuButtons.get(i);
                b.draw(g, b.contains(mouseX, mouseY));
            } // for

            // check for collisions
            for (int i = 0; i < entities.size(); i++) {
                for (int j = i + 1; j < entities.size(); j++) {
                    Entity me = entities.get(i);
                    Entity him = entities.get(j);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    } // if
                } //for
            } // for

            entities.removeAll(removeEntities);
            removeEntities.clear();

            if (ammo <= 0 && !waitingForKeyPress && countShots() == 0) {
                notifyDeath();
            } // if

            g.setFont(HUD_FONT);
            drawShadowText(g, "Level " + (level + 1) + "   Cats: " + ammo + "   Dogs: " + alienCount, 15, 35);

            if (waitingForKeyPress) {
                g.setFont(HUD_FONT);
                drawShadowText(g, message, (SCREEN_W - g.getFontMetrics().stringWidth(message)) / 2, 250);
                g.setFont(INFO_FONT);
                String text = "Press any key to play again";
                drawShadowText(g, text, (SCREEN_W - g.getFontMetrics().stringWidth(text)) / 2, 290);
            } // if

            if (displayMenu) {
                drawMenu(g);
            } // if

            // flip buffer
            g.dispose();
            strategy.show();

            try { Thread.sleep(10); } catch (Exception e) {}
        } // while
    } // gameLoop


    private void startGame() {
    	if(levelWon) {
    		levelWon = false;
    		level++;
    		if (level > Levels.LEVEL_COUNT) {
    			level = 0;
    		}
    	}
    	IO.println("started game");
    	drawMenu((Graphics2D) strategy.getDrawGraphics());
        entities.clear();
        removeEntities.clear();
        
        initPlayer();
        
        initEntities();

        upPressed = false;
        downPressed = false;
        dragging = false;
        fireRequested = false;
        lastFire = 0;
        pullLength = 0;
        levelWon = false;
        
        displayMenu = true;
    } // startGame


    private void setKey(int keyCode, boolean down) {
        if (keyCode == KeyEvent.VK_W) {
            upPressed = down;
        } // if
        if (keyCode == KeyEvent.VK_S) {
            downPressed = down;
        } // if
    } // setKey


    private class KeyInputHandler extends KeyAdapter {

        public void keyPressed(KeyEvent e) {

            // ignore keys while waiting to start
            if (waitingForKeyPress) {
                return;
            } // if

            // movement
            setKey(e.getKeyCode(), true);

            //if clicked on cat
            if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() < KeyEvent.VK_1 + CAT_NAMES.length) {
                cat = e.getKeyCode() - KeyEvent.VK_1;
            } // if
            
            // pause (P)
            if (e.getKeyCode() == KeyEvent.VK_P) {
                paused = !paused;
            } // if
        } // keyPressed

        public void keyReleased(KeyEvent e) {
            setKey(e.getKeyCode(), false);
        } // keyReleased

        public void keyTyped(KeyEvent e) {
			
            if (waitingForKeyPress) {
                newGame = true;
            } // if

            // escape exits the game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            } // if
        } // keyTyped
    } // class KeyInputHandler

    private class MouseInputHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (waitingForKeyPress) {
                newGame = true;
                return;
            } // if

            for (int i = 0; i < buttons.size(); i++) {
                if (buttons.get(i).contains(e.getX(), e.getY())) {
                    cat = i;
                    return;
                } // if
            } // for
            
            for (int i = 0; i < menuButtons.size(); i++) {
                if (menuButtons.get(i).contains(e.getX(), e.getY())) {
                    level = i;
                    IO.print("selected lvl: " + level);
                    displayMenu = false;
                    paused = false;
                    levelSelected = true;
                    return;
                } // if
            } // for

            // start dragging if clicked on sling
            double distX = e.getX() - player.getCenterX();
            double distY = e.getY() - player.getCenterY();
            if (!paused && isLoaded() && Math.sqrt(distX * distX + distY * distY) <= GRAB_RADIUS) {
                dragging = true;
            } // if
        } // mousePressed

        public void mouseReleased(MouseEvent e) {
            if (dragging) {
                dragging = false;
                fireRequested = true;
            } // if
        } // mouseReleased
    } // class MouseInputHandler

    public static void main(String [] args) {
        new Main();
    } // main
} // Main