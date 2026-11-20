package com.grandlineescape;

import java.util.ArrayList;
import java.util.List;

public class Projectile {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private final double radius;
    private final int damage;
    private double remainingLife;
    private final boolean fromPlayer;
    private final boolean piercing;
    private final String color;
    private final String rarityColor;
    private final List<Integer> hitEnemyIds;

    public Projectile(double x, double y, double vx, double vy, double radius, int damage,
                      double life, boolean fromPlayer, boolean piercing,
                      String color, String rarityColor) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.damage = damage;
        this.remainingLife = life;
        this.fromPlayer = fromPlayer;
        this.piercing = piercing;
        this.color = color;
        this.rarityColor = rarityColor;
        this.hitEnemyIds = new ArrayList<>();
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        remainingLife -= dt;
    }

    public boolean hasAlreadyHit(int enemyId) {
        for (int i = 0; i < hitEnemyIds.size(); i++) {
            if (hitEnemyIds.get(i) == enemyId) {
                return true;
            }
        }
        return false;
    }

    public void recordHit(int enemyId) {
        hitEnemyIds.add(enemyId);
    }

    public boolean isExpired() { return remainingLife <= 0.0; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public int getDamage() { return damage; }
    public boolean isFromPlayer() { return fromPlayer; }
    public boolean isPiercing() { return piercing; }
    public String getColor() { return color; }
    public String getRarityColor() { return rarityColor; }
}
