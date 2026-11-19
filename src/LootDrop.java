public class LootDrop {
    private final String type;
    private final double x;
    private final double y;
    private final int amount;
    private double bobPhase;

    public LootDrop(String type, double x, double y, int amount) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.amount = Math.max(0, amount);
        this.bobPhase = Math.random() * Math.PI * 2;
    }

    public void update(double dt) {
        bobPhase += dt * 3.5;
    }

    public boolean isBerry() { return "BERRY".equals(type); }
    public String getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getAmount() { return amount; }
    public double getBobPhase() { return bobPhase; }
}
