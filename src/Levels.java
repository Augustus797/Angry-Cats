//dog is 52x39, grass line: y = 464
public class Levels {
    public static final int LEVEL_COUNT = 3;

    private static final int[] AMMO = {4, 5, 6};

    private static final int[][][] DOGS = {

        // ===== LEVEL 1 : a pyramid =====
        {
            {500, 425}, {556, 425}, {612, 425}, {668, 425},  
            {528, 382}, {584, 382}, {640, 382},             
            {556, 339}, {612, 339},                           
            {584, 296}                                        
        },

        // ===== LEVEL 2 : two towers and some doge =====
        {
            {420, 425}, {420, 382}, {420, 339},               
            {640, 425}, {640, 382}, {640, 339}, {640, 296},   
            {530, 250}, {530, 200},                           
            {300, 150}, {250, 100}                            
        },

        // ===== LEVEL 3 : dog arch =====
        {
            {300, 425}, {350, 330}, {420, 250}, {500, 200},
            {580, 250}, {650, 330}, {700, 425},
            {500, 425}, {500, 380}, {500, 335}                
        }
    };

    public static int getAmmo(int level) {
        return AMMO[level];
    } // getAmmo

    public static int[][] getDogs(int level) {
        return DOGS[level];
    } // getDogs
} // Levels