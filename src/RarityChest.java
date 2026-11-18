public class RarityChest {
    private final double x;
    private final double y;
    private int rarityIndex;
    private boolean opened;
    private double respawnTimer;
    private double bobPhase;

    public RarityChest(double x, double y, int rarityIndex) {
        this.x = x;
        this.y = y;
        this.rarityIndex = clampRarity(rarityIndex);
        this.opened = false;
        this.respawnTimer = 0.0;
        this.bobPhase = Math.random() * Math.PI * 2.0;
    }

    public static int rollWeightedRarity() {
        double roll = Math.random();
        if (roll < 0.55) {
            return 0;
        } else if (roll < 0.82) {
            return 1;
        } else if (roll < 0.95) {
            return 2;
        } else if (roll < 0.99) {
            return 3;
        } else {
            return 4;
        }
    }

    private int clampRarity(int idx) {
        return Math.max(0, Math.min(4, idx));
    }

    public void update(double dt) {
        bobPhase += dt * 3.2;
        if (opened) {
            respawnTimer -= dt;
            if (respawnTimer <= 0.0) {
                opened = false;
                respawnTimer = 0.0;
                rarityIndex = rollWeightedRarity();
            }
        }
    }

    public int getHpRestore() {
        int[] values = {22, 36, 55, 80, 120};
        return values[rarityIndex];
    }

    public int getEnergyRestore() {
        int[] values = {32, 50, 75, 100, 100};
        return values[rarityIndex];
    }

    public int getBerryReward() {
        int[] values = {1, 2, 4, 7, 12};
        return values[rarityIndex];
    }

    public void markOpened(double respawnSeconds) {
        this.opened = true;
        this.respawnTimer = Math.max(1.0, respawnSeconds);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRarityIndex() { return rarityIndex; }
    public boolean isOpened() { return opened; }
    public double getBobPhase() { return bobPhase; }
}
