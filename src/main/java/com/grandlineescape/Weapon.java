package com.grandlineescape;

public class Weapon {
    public static final double ENERGY_COST_PER_SHOT = 1.0;
    public static final String[] RARITY_NAMES = {"Common", "Uncommon", "Rare", "Epic", "Legendary"};
    public static final String[] RARITY_COLORS = {"#94a3b8", "#22c55e", "#3b82f6", "#a855f7", "#eab308"};
    public static final double[] RARITY_MULTIPLIERS = {1.00, 1.35, 1.80, 2.35, 3.10};
    public static final int[] UPGRADE_COSTS = {23, 45, 80, 140, 0};

    private final String id;
    private final String name;
    private final String gimkitRef;
    private final String icon;
    private final int unlockCost;
    private final int clipSize;
    private final double reloadDuration;
    private final double fireInterval;
    private final int baseDamage;
    private final double projectileSpeed;
    private final double projectileRadius;
    private final double projectileLife;
    private final double spreadAngle;
    private final int pelletCount;
    private final boolean piercing;
    private final String projectileColor;
    private final String description;

    private boolean unlocked;
    private int rarityIndex;
    private int currentAmmo;
    private boolean reloading;
    private double reloadTimer;
    private double fireCooldown;

    public Weapon(String id, String name, String gimkitRef, String icon, int unlockCost,
                  int clipSize, double reloadDuration, double fireInterval, int baseDamage,
                  double projectileSpeed, double projectileRadius, double projectileLife,
                  double spreadAngle, int pelletCount, boolean piercing,
                  String projectileColor, String description, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.gimkitRef = gimkitRef;
        this.icon = icon;
        this.unlockCost = unlockCost;
        this.clipSize = clipSize;
        this.reloadDuration = reloadDuration;
        this.fireInterval = fireInterval;
        this.baseDamage = baseDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRadius = projectileRadius;
        this.projectileLife = projectileLife;
        this.spreadAngle = spreadAngle;
        this.pelletCount = Math.max(1, pelletCount);
        this.piercing = piercing;
        this.projectileColor = projectileColor;
        this.description = description;

        this.unlocked = unlocked;
        this.rarityIndex = 0;
        this.currentAmmo = clipSize;
        this.reloading = false;
        this.reloadTimer = 0.0;
        this.fireCooldown = 0.0;
    }

    public void updateTimers(double dt) {
        if (fireCooldown > 0.0) {
            fireCooldown = Math.max(0.0, fireCooldown - dt);
        }
        if (reloading) {
            reloadTimer -= dt;
            if (reloadTimer <= 0.0) {
                reloading = false;
                reloadTimer = 0.0;
                currentAmmo = clipSize;
            }
        }
    }

    public boolean canFire() {
        return unlocked && !reloading && fireCooldown <= 0.0 && currentAmmo > 0;
    }

    public void consumeShot() {
        if (currentAmmo > 0) {
            currentAmmo--;
        }
        fireCooldown = fireInterval;
        if (currentAmmo == 0) {
            startReload();
        }
    }

    public void startReload() {
        if (unlocked && !reloading && currentAmmo < clipSize) {
            reloading = true;
            reloadTimer = reloadDuration;
        }
    }

    public int getScaledDamage() {
        double mult = RARITY_MULTIPLIERS[rarityIndex];
        return (int) Math.round(baseDamage * mult);
    }

    public boolean upgradeRarity() {
        if (!unlocked || rarityIndex >= RARITY_NAMES.length - 1) {
            return false;
        }
        rarityIndex++;
        return true;
    }

    public void unlockWithRarity(int initialRarity) {
        this.unlocked = true;
        this.rarityIndex = Math.max(0, Math.min(RARITY_NAMES.length - 1, initialRarity));
        this.currentAmmo = this.clipSize;
        this.reloading = false;
    }

    public String getRarityName() { return RARITY_NAMES[rarityIndex]; }
    public String getRarityColor() { return RARITY_COLORS[rarityIndex]; }
    public int getNextUpgradeCost() { return UPGRADE_COSTS[rarityIndex]; }
    public boolean isMaxRarity() { return rarityIndex >= RARITY_NAMES.length - 1; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getGimkitRef() { return gimkitRef; }
    public String getIcon() { return icon; }
    public int getUnlockCost() { return unlockCost; }
    public int getClipSize() { return clipSize; }
    public double getReloadDuration() { return reloadDuration; }
    public int getBaseDamage() { return baseDamage; }
    public double getProjectileSpeed() { return projectileSpeed; }
    public double getProjectileRadius() { return projectileRadius; }
    public double getProjectileLife() { return projectileLife; }
    public double getSpreadAngle() { return spreadAngle; }
    public int getPelletCount() { return pelletCount; }
    public boolean isPiercing() { return piercing; }
    public String getProjectileColor() { return projectileColor; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }
    public int getRarityIndex() { return rarityIndex; }
    public int getCurrentAmmo() { return currentAmmo; }
    public boolean isReloading() { return reloading; }
}
