 import java.awt.*;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 
 public class SpriteStore {
   
   private static SpriteStore single = new SpriteStore();
   private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();

   public static SpriteStore get() {
     return single;
   } // get
   
    public Sprite getSprite(String ref) {
     
      if (sprites.get(ref) != null) {
        return sprites.get(ref);
      } // if
          
      BufferedImage sourceImage = null;
      
      try {
        // get image location
        URL url = this.getClass().getClassLoader().getResource(ref);
        if (url == null) {
          System.out.println("Failed to load: " + ref);
          System.exit(0);
        }
        sourceImage = ImageIO.read(url); // get image
      } catch (IOException e) {
        System.out.println("Failed to load: " + ref);
        System.exit(0); 
      } // catch

     //default graphics config
      GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      Image image = gc.createCompatibleImage(sourceImage.getWidth(), sourceImage.getHeight(), Transparency.TRANSLUCENT);

      image.getGraphics().drawImage(sourceImage, 0, 0, null);

      Sprite sprite = new Sprite(image);
      sprites.put(ref, sprite);
      
      return sprite;
    } // getSprite
 } // SpriteStore
