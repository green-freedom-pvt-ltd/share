package Models;

/**
 * Created by ankitmaheshwari on 5/20/17.
 */

public class Level {

    private int level;
    private int minKm; // inclusive
    private int maxKm; // exclusive

    public Level(int level, int minKm, int maxKm) {
        this.level = level;
        this.minKm = minKm;
        this.maxKm = maxKm;
    }

    public int getLevel() {
        return level;
    }

    public int getMinKm() {
        return minKm;
    }

    public int getMaxKm() {
        return maxKm;
    }
}
