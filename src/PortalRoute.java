public class PortalRoute {
    private final String id;
    private final String name;
    private final double dockX;
    private final double dockY;
    private final double destinationX;
    private final double destinationY;
    private final int berryCost;
    private boolean unlocked;

    public PortalRoute(String id, String name, double dockX, double dockY,
                       double destinationX, double destinationY, int berryCost) {
        this.id = id;
        this.name = name;
        this.dockX = dockX;
        this.dockY = dockY;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.berryCost = Math.max(0, berryCost);
        this.unlocked = false;
    }

    public boolean isNear(double x, double y) {
        return Math.hypot(x - dockX, y - dockY) <= 64.0
                || Math.hypot(x - destinationX, y - destinationY) <= 64.0;
    }

    public boolean isAtDock(double x, double y) {
        return Math.hypot(x - dockX, y - dockY) <= 64.0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getDockX() { return dockX; }
    public double getDockY() { return dockY; }
    public double getDestinationX() { return destinationX; }
    public double getDestinationY() { return destinationY; }
    public int getBerryCost() { return berryCost; }
    public boolean isUnlocked() { return unlocked; }
    public void unlock() { unlocked = true; }
}
