package Models;

/**
 * Created by ankitmaheshwari on 5/20/17.
 */

public class Level {

    private int level;
    private int minImpact; // inclusive
    private int maxImpact; // exclusive

    public Level(int level, int minImpact, int maxImpact) {
        this.level = level;
        this.minImpact = minImpact;
        this.maxImpact = maxImpact;
    }

    public int getLevel() {
        return level;
    }

    public int getMinImpact() {
        return minImpact;
    }

    public int getMaxImpact() {
        return maxImpact;
    }
}
