import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    public static final double ENEMY_RESPAWN_SECONDS = 2.0;

    public static final String[] DIFFICULTY_NAMES = {
        "Easy", "Normal", "Hard", "Expert", "Ludicrous"
    };
    public static final double[] DIFFICULTY_HP_MULT = {0.80, 1.00, 1.35, 1.75, 2.30};
    public static final double[] DIFFICULTY_DMG_MULT = {0.75, 1.00, 1.30, 1.60, 2.00};
    public static final int[] DIFFICULTY_GATE_REQ = {4, 5, 6, 7, 8};

    private String gameMode;
    private int difficultyIndex;
    private double elapsedTime;
    private boolean showHitboxes;

    private int enemiesDefeatedCount;
    private int chestsOpenedCount;
    private int totalBerriesEarned;
    private double lowestHpReached;
    private int highestSingleHitDamage;
    private boolean imuDefeated;
    private boolean secretVaultUnlocked;
    private boolean secretWeaponClaimed;

    private boolean keyUp, keyDown, keyLeft, keyRight;
    private boolean firingHeld;
    private double inputAimAngle;

    private Player player;
    private final List<WorldObstacle> walls;
    private final List<SectorGate> gates;
    private final List<RarityChest> chests;
    private final List<PirateEnemy> enemies;
    private final List<Projectile> projectiles;
    private final List<SkyLaser> lasers;
    private final List<String> recentEvents;
    private final List<PendingEnemyRespawn> pendingEnemyRespawns;
    private int nextEventId;
    private int nextEnemyId;

    private static final class PendingEnemyRespawn {
        private final PirateEnemy enemy;
        private double remainingSeconds;

        private PendingEnemyRespawn(PirateEnemy enemy, double remainingSeconds) {
            this.enemy = enemy;
            this.remainingSeconds = remainingSeconds;
        }
    }

    public GameEngine() {
        this.walls = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.chests = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.lasers = new ArrayList<>();
        this.recentEvents = new ArrayList<>();
        this.pendingEnemyRespawns = new ArrayList<>();
        this.difficultyIndex = 1;
        this.showHitboxes = false;
        resetGame("Normal", "Monkey D. Luffy");
        this.gameMode = "START";
    }

    public synchronized void resetGame(String difficultyName, String captainName) {
        this.difficultyIndex = resolveDifficultyIndex(difficultyName);
        this.gameMode = "PLAYING";
        this.elapsedTime = 0.0;

        this.enemiesDefeatedCount = 0;
        this.chestsOpenedCount = 0;
        this.totalBerriesEarned = 0;
        this.lowestHpReached = 100.0;
        this.highestSingleHitDamage = 0;
        this.imuDefeated = false;
        this.secretVaultUnlocked = false;
        this.secretWeaponClaimed = false;

        this.keyUp = false;
        this.keyDown = false;
        this.keyLeft = false;
        this.keyRight = false;
        this.firingHeld = false;
        this.inputAimAngle = 0.0;

        this.walls.clear();
        this.gates.clear();
        this.chests.clear();
        this.enemies.clear();
        this.pendingEnemyRespawns.clear();
        this.projectiles.clear();
        this.lasers.clear();
        this.recentEvents.clear();
        this.nextEventId = 1;
        this.nextEnemyId = 1;

        this.player = new Player(captainName, 520.0, 1300.0);
        buildOpenIslandWorld();
        pushEvent("Set sail on " + DIFFICULTY_NAMES[difficultyIndex] + " difficulty!", "#38bdf8", player.getX(), player.getY() - 30);
    }

    public int resolveDifficultyIndex(String diffName) {
        if (diffName == null) {
            return 1;
        }
        for (int i = 0; i < DIFFICULTY_NAMES.length; i++) {
            if (DIFFICULTY_NAMES[i].equals(diffName)) {
                return i;
            }
        }
        return 1;
    }

    private void buildOpenIslandWorld() {
        int gateReq = DIFFICULTY_GATE_REQ[difficultyIndex];
        double hpMult = DIFFICULTY_HP_MULT[difficultyIndex];
        double dmgMult = DIFFICULTY_DMG_MULT[difficultyIndex];

        walls.add(new WorldObstacle(260, 420, 3260, 40, "cliff"));
        walls.add(new WorldObstacle(260, 2140, 3260, 40, "cliff"));
        walls.add(new WorldObstacle(260, 420, 40, 1760, "cliff"));
        walls.add(new WorldObstacle(3480, 420, 40, 1760, "cliff"));

        walls.add(new WorldObstacle(1740, 460, 38, 750, "fortress"));
        walls.add(new WorldObstacle(1740, 1390, 38, 750, "fortress"));
        gates.add(new SectorGate("gate1", "Sector 1 Sea-Gate", 1740, 1210, 38, 180, gateReq, 0, "#22c55e"));

        walls.add(new WorldObstacle(2690, 460, 38, 750, "fortress"));
        walls.add(new WorldObstacle(2690, 1390, 38, 750, "fortress"));
        gates.add(new SectorGate("gate2", "Sector 2 Red-Line Gate", 2690, 1210, 38, 180, gateReq + 1, 0, "#3b82f6"));

        walls.add(new WorldObstacle(2060, 460, 28, 210, "ruin"));
        walls.add(new WorldObstacle(2352, 460, 28, 210, "ruin"));
        walls.add(new WorldObstacle(2060, 645, 105, 28, "ruin"));
        walls.add(new WorldObstacle(2275, 645, 105, 28, "ruin"));
        gates.add(new SectorGate("secret_vault", "Secret Poneglyph Vault", 2165, 645, 110, 28, 0, 3, "#eab308"));

        walls.add(new WorldObstacle(1020, 950, 68, 56, "rock"));
        walls.add(new WorldObstacle(1180, 1520, 74, 58, "rock"));
        walls.add(new WorldObstacle(1450, 1060, 64, 64, "crate"));
        walls.add(new WorldObstacle(1480, 1560, 64, 64, "crate"));
        walls.add(new WorldObstacle(1960, 980, 82, 64, "ruin"));
        walls.add(new WorldObstacle(2020, 1580, 82, 64, "ruin"));
        walls.add(new WorldObstacle(2380, 1140, 76, 76, "ruin"));
        walls.add(new WorldObstacle(2420, 1560, 84, 62, "ruin"));
        walls.add(new WorldObstacle(2920, 940, 76, 66, "obsidian"));
        walls.add(new WorldObstacle(2960, 1620, 76, 66, "obsidian"));
        walls.add(new WorldObstacle(3180, 1080, 68, 68, "obsidian"));
        walls.add(new WorldObstacle(3180, 1480, 68, 68, "obsidian"));

        walls.add(new WorldObstacle(1200, 2700, 1200, 42, "throne_wall"));
        walls.add(new WorldObstacle(1200, 3610, 1200, 42, "throne_wall"));
        walls.add(new WorldObstacle(1200, 2700, 42, 952, "throne_wall"));
        walls.add(new WorldObstacle(2358, 2700, 42, 952, "throne_wall"));
        walls.add(new WorldObstacle(1460, 3020, 56, 56, "pillar"));
        walls.add(new WorldObstacle(2084, 3020, 56, 56, "pillar"));
        walls.add(new WorldObstacle(1460, 3340, 56, 56, "pillar"));
        walls.add(new WorldObstacle(2084, 3340, 56, 56, "pillar"));

        chests.add(new RarityChest(840, 1400, 0));

        for (int i = 0; i < 5; i++) {
            spawnChestAtValidLocation(900, 1680, 560, 2040, RarityChest.rollWeightedRarity());
        }
        for (int i = 0; i < 6; i++) {
            spawnChestAtValidLocation(1820, 2630, 720, 2040, RarityChest.rollWeightedRarity());
        }
        for (int i = 0; i < 5; i++) {
            int r = (Math.random() < 0.20) ? 3 : RarityChest.rollWeightedRarity();
            spawnChestAtValidLocation(2760, 3380, 560, 2040, r);
        }

        chests.add(new RarityChest(1340, 3440, 3));
        chests.add(new RarityChest(2260, 3440, 4));

        spawnPirates(hpMult, dmgMult);

        int laserDmg = (int) Math.round(28 * dmgMult);
        lasers.add(new SkyLaser(2860, 520, 2860, 2080, 3.6, 0.4, 1.3, 0.85, 26, false, laserDmg));
        lasers.add(new SkyLaser(3060, 520, 3060, 2080, 3.6, 1.6, 1.3, 0.85, 26, false, laserDmg));
        lasers.add(new SkyLaser(3220, 520, 3220, 2080, 3.6, 2.8, 1.3, 0.85, 26, false, laserDmg));
        lasers.add(new SkyLaser(2760, 1300, 3420, 1300, 4.2, 1.1, 1.4, 0.90, 24, false, laserDmg));
    }

    private void spawnChestAtValidLocation(double minX, double maxX, double minY, double maxY, int rarityIdx) {
        int attempts = 0;
        boolean placed = false;
        while (!placed && attempts < 30) {
            attempts++;
            double cx = minX + Math.random() * (maxX - minX);
            double cy = minY + Math.random() * (maxY - minY);

            if (cx > 2040 && cx < 2400 && cy < 700) {
                continue;
            }

            boolean blocked = false;
            for (int i = 0; i < walls.size(); i++) {
                WorldObstacle w = walls.get(i);
                if (cx + 36 > w.getX() && cx - 36 < w.getX() + w.getWidth() &&
                    cy + 36 > w.getY() && cy - 36 < w.getY() + w.getHeight()) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                chests.add(new RarityChest(cx, cy, rarityIdx));
                placed = true;
            }
        }
    }

    private void spawnPirates(double hpMult, double dmgMult) {

        addEnemy("buggy", "Buggy Pirate", 1050, 820, 21, 65, 125, 12, 1.65, 340, 8, 1, 0, "#ef4444", "#38bdf8", false, hpMult, dmgMult);
        addEnemy("buggy", "Buggy Pirate", 1120, 1340, 21, 65, 125, 12, 1.65, 340, 8, 1, 0, "#ef4444", "#38bdf8", false, hpMult, dmgMult);
        addEnemy("alvida", "Alvida Bruiser", 1240, 1080, 26, 115, 98, 18, 1.90, 160, 11, 1, 0, "#ec4899", "#fde047", false, hpMult, dmgMult);
        addEnemy("buggy", "Buggy Pirate", 1350, 1680, 21, 65, 125, 12, 1.65, 340, 8, 1, 0, "#ef4444", "#38bdf8", false, hpMult, dmgMult);
        addEnemy("alvida", "Alvida Bruiser", 1520, 860, 26, 115, 98, 18, 1.90, 160, 11, 1, 0, "#ec4899", "#fde047", false, hpMult, dmgMult);
        addEnemy("buggy", "Buggy Pirate", 1580, 1320, 21, 65, 125, 12, 1.65, 340, 8, 1, 0, "#ef4444", "#38bdf8", false, hpMult, dmgMult);
        addEnemy("alvida", "Alvida Bruiser", 1620, 1740, 26, 115, 98, 18, 1.90, 160, 11, 1, 0, "#ec4899", "#fde047", false, hpMult, dmgMult);

        addEnemy("fishman", "Arlong Fish-Man", 1940, 840, 23, 140, 172, 20, 1.35, 260, 14, 1, 0, "#0ea5e9", "#f97316", false, hpMult, dmgMult);
        addEnemy("sniper", "Baroque Sniper", 2060, 1420, 20, 105, 105, 24, 2.10, 480, 15, 1, 0, "#6366f1", "#facc15", false, hpMult, dmgMult);
        addEnemy("elite_pink", "Elite Pink Captain", 1960, 1780, 27, 210, 142, 24, 1.25, 360, 25, 2, 1, "#f472b6", "#eab308", false, hpMult, dmgMult);
        addEnemy("fishman", "Arlong Fish-Man", 2240, 1040, 23, 140, 172, 20, 1.35, 260, 14, 1, 0, "#0ea5e9", "#f97316", false, hpMult, dmgMult);
        addEnemy("elite_pink", "Elite Pink Captain", 2340, 1320, 27, 210, 142, 24, 1.25, 360, 25, 2, 1, "#f472b6", "#eab308", false, hpMult, dmgMult);
        addEnemy("sniper", "Baroque Sniper", 2480, 860, 20, 105, 105, 24, 2.10, 480, 15, 1, 0, "#6366f1", "#facc15", false, hpMult, dmgMult);
        addEnemy("fishman", "Arlong Fish-Man", 2520, 1680, 23, 140, 172, 20, 1.35, 260, 14, 1, 0, "#0ea5e9", "#f97316", false, hpMult, dmgMult);
        addEnemy("elite_pink", "Elite Pink Captain", 2560, 1180, 27, 210, 142, 24, 1.25, 360, 25, 2, 1, "#f472b6", "#eab308", false, hpMult, dmgMult);

        addEnemy("cp0", "CP0 Masked Agent", 2840, 840, 22, 220, 192, 26, 1.15, 330, 22, 2, 0, "#f8fafc", "#1e293b", false, hpMult, dmgMult);
        addEnemy("admiral", "Navy Admiral", 2980, 1320, 30, 340, 125, 32, 1.40, 420, 34, 2, 1, "#eab308", "#dc2626", false, hpMult, dmgMult);
        addEnemy("cp0", "CP0 Masked Agent", 2890, 1760, 22, 220, 192, 26, 1.15, 330, 22, 2, 0, "#f8fafc", "#1e293b", false, hpMult, dmgMult);
        addEnemy("admiral", "Navy Admiral", 3160, 880, 30, 340, 125, 32, 1.40, 420, 34, 2, 1, "#eab308", "#dc2626", false, hpMult, dmgMult);
        addEnemy("cp0", "CP0 Masked Agent", 3220, 1660, 22, 220, 192, 26, 1.15, 330, 22, 2, 0, "#f8fafc", "#1e293b", false, hpMult, dmgMult);
        addEnemy("admiral", "Navy Admiral", 3320, 1120, 30, 340, 125, 32, 1.40, 420, 34, 2, 1, "#eab308", "#dc2626", false, hpMult, dmgMult);

        addEnemy("imu", "IMU — Sovereign of the Empty Throne", 1800, 2920, 46, 2400, 118, 34, 1.35, 680, 150, 0, 0, "#090d16", "#e11d48", true, hpMult, dmgMult);
    }

    private void addEnemy(String type, String name, double x, double y, double radius, int hp,
                          double speed, int dmg, double fireRate, double range, int berries,
                          int greenDrop, int goldDrop, String c1, String c2, boolean boss,
                          double hpMult, double dmgMult) {
        enemies.add(new PirateEnemy(nextEnemyId++, type, name, x, y, radius, hp, speed, dmg,
                fireRate, range, berries, greenDrop, goldDrop, c1, c2, boss, hpMult, dmgMult));
    }

    public synchronized void updateInputState(boolean up, boolean down, boolean left, boolean right,
                                              boolean firing, double aimAngle) {
        this.keyUp = up;
        this.keyDown = down;
        this.keyLeft = left;
        this.keyRight = right;
        this.firingHeld = firing;
        this.inputAimAngle = aimAngle;
        if (player != null) {
            player.setAimAngle(aimAngle);
        }
    }

    public synchronized String executePlayerAction(String action, int paramValue) {
        if (action == null || player == null) {
            return "INVALID_ACTION";
        }

        if ("TOGGLE_HITBOXES".equals(action)) {
            showHitboxes = !showHitboxes;
            return "HITBOXES_" + (showHitboxes ? "ON" : "OFF");
        }

        if (!"PLAYING".equals(gameMode)) {
            return "NOT_PLAYING";
        }

        if ("FIRE".equals(action)) {
            fireEquippedWeapon();
            return "FIRED";
        } else if ("DASH".equals(action)) {
            double dx = (keyRight ? 1.0 : 0.0) - (keyLeft ? 1.0 : 0.0);
            double dy = (keyDown ? 1.0 : 0.0) - (keyUp ? 1.0 : 0.0);
            if (player.tryDash(dx, dy)) {
                pushEvent("GUM-GUM ROCKET!", "#38bdf8", player.getX(), player.getY() - 26);
                return "DASHED";
            } else {
                pushEvent("Need 25 Energy! (Open Chests)", "#38bdf8", player.getX(), player.getY() - 26);
                return "LOW_ENERGY";
            }
        } else if ("RELOAD".equals(action)) {
            player.getEquippedWeapon().startReload();
            return "RELOADING";
        } else if ("SELECT_SLOT".equals(action)) {
            return player.selectWeaponSlot(paramValue) ? "SLOT_SELECTED" : "SLOT_LOCKED";
        } else if ("BUY_WEAPON".equals(action)) {
            if (player.buyWeapon(paramValue)) {
                Weapon w = player.getWeapons().get(paramValue);
                pushEvent("Unlocked " + w.getName() + "!", "#22c55e", player.getX(), player.getY() - 28);
                return "PURCHASED";
            }
            return "CANNOT_BUY";
        } else if ("UPGRADE_WEAPON".equals(action)) {
            if (player.upgradeWeaponRarity(paramValue)) {
                Weapon w = player.getWeapons().get(paramValue);
                pushEvent("Upgraded to " + w.getRarityName() + "!", w.getRarityColor(), player.getX(), player.getY() - 28);
                return "UPGRADED";
            }
            return "CANNOT_UPGRADE";
        } else if ("INTERACT".equals(action)) {
            return handleInteract();
        }
        return "UNKNOWN_ACTION";
    }

    private String handleInteract() {

        for (int i = 0; i < chests.size(); i++) {
            RarityChest c = chests.get(i);
            if (!c.isOpened() && player.distanceTo(c.getX(), c.getY()) <= 58.0) {
                openChest(c);
                return "CHEST_OPENED";
            }
        }

        for (int i = 0; i < gates.size(); i++) {
            SectorGate g = gates.get(i);
            if (!g.isOpened() && player.distanceTo(g.getCenterX(), g.getCenterY()) <= 125.0) {
                if (g.canUnlock(player.getGreenLogPoses(), player.getGoldKeycards())) {
                    if (g.getRequiredGoldKeys() > 0) {
                        player.spendGoldKeycards(g.getRequiredGoldKeys());
                        secretVaultUnlocked = true;
                    } else {
                        player.spendGreenLogPoses(g.getRequiredGreenKeys());
                    }
                    g.setOpened(true);
                    pushEvent(g.getName() + " UNLOCKED!", "#22c55e", g.getCenterX(), g.getCenterY() - 20);
                    return "GATE_UNLOCKED";
                } else {
                    String msg = (g.getRequiredGoldKeys() > 0)
                            ? "Need " + g.getRequiredGoldKeys() + " Gold Keycards!"
                            : "Need " + g.getRequiredGreenKeys() + " Green Log Poses!";
                    pushEvent(msg, "#f87171", g.getCenterX(), g.getCenterY() - 20);
                    return "GATE_LOCKED";
                }
            }
        }

        if (player.distanceTo(760, 1300) <= 76.0) {
            player.setPosition(1860, 1300);
            pushEvent("FAST TRAVELED TO JUNGLE!", "#22d3ee", player.getX(), player.getY() - 26);
            return "WARPED";
        }
        if (player.distanceTo(1860, 1120) <= 76.0 || player.distanceTo(2810, 1120) <= 76.0) {
            player.setPosition(760, 1300);
            pushEvent("FAST TRAVELED TO SAFE HARBOR!", "#22d3ee", player.getX(), player.getY() - 26);
            return "WARPED";
        }

        if (!secretWeaponClaimed && player.distanceTo(2220, 540) <= 78.0) {
            secretWeaponClaimed = true;
            Weapon hakiRod = player.getWeapons().get(5);
            hakiRod.unlockWithRarity(3);
            player.selectWeaponSlot(5);
            pushEvent("ACQUIRED: CONQUEROR'S HAKI ROD!", "#f43f5e", 2220, 510);
            return "SECRET_CLAIMED";
        }

        int throneReq = DIFFICULTY_GATE_REQ[difficultyIndex] + 2;
        if (player.distanceTo(3320, 1300) <= 90.0) {
            if (player.getGreenLogPoses() >= throneReq) {
                player.spendGreenLogPoses(throneReq);
                player.setPosition(1800, 3480);
                pushEvent("ENTERED THE EMPTY THRONE!", "#fb7185", 1800, 3420);
                return "ENTERED_THRONE";
            } else {
                pushEvent("Need " + throneReq + " Green Log Poses!", "#f87171", 3320, 1265);
                return "PORTAL_LOCKED";
            }
        }

        if (imuDefeated && player.distanceTo(1800, 3480) <= 86.0) {
            player.setPosition(500, 1300);
            pushEvent("BOARD THE THOUSAND SUNNY TO ESCAPE!", "#fbbf24", 500, 1265);
            return "RETURNED_HARBOR";
        }

        if (player.distanceTo(375, 1300) <= 104.0) {
            if (imuDefeated) {
                gameMode = "VICTORY";
                pushEvent("GRAND LINE ESCAPED! VICTORY!", "#fbbf24", 375, 1240);
                return "VICTORY";
            } else {
                pushEvent("Defeat IMU on the Empty Throne first!", "#fbbf24", 375, 1240);
                return "BOSS_ALIVE";
            }
        }

        return "NO_TARGET";
    }

    private void openChest(RarityChest c) {
        c.markOpened(40.0);
        chestsOpenedCount++;
        int idx = c.getRarityIndex();
        int hpGain = c.getHpRestore();
        int enGain = c.getEnergyRestore();
        int berryGain = c.getBerryReward();

        player.applyChestRewards(hpGain, enGain, berryGain, idx == 4);
        totalBerriesEarned += berryGain;

        String rarityName = Weapon.RARITY_NAMES[idx];
        String rarityColor = Weapon.RARITY_COLORS[idx];
        StringBuilder sb = new StringBuilder();
        sb.append("+").append(hpGain).append(" HP  +").append(enGain).append(" EN  +B").append(berryGain);

        if (idx >= 3 || (idx == 2 && Math.random() < 0.45)) {
            player.addGreenLogPoses(1);
            sb.append("  +1 LogPose");
        }
        if (idx == 4 && Math.random() < 0.55) {
            player.addGoldKeycard();
            sb.append("  +1 GoldKey");
        }

        pushEvent(rarityName.toUpperCase() + " CHEST! " + sb.toString(), rarityColor, c.getX(), c.getY() - 22);
    }

    private void fireEquippedWeapon() {
        Weapon w = player.getEquippedWeapon();
        if (!w.canFire()) {
            if (w.isUnlocked() && !w.isReloading() && w.getCurrentAmmo() <= 0) {
                w.startReload();
            }
            return;
        }

        w.consumeShot();
        int scaledDmg = w.getScaledDamage();
        double muzzleDist = player.getRadius() + 12.0;
        double mx = player.getX() + Math.cos(player.getAimAngle()) * muzzleDist;
        double my = player.getY() + Math.sin(player.getAimAngle()) * muzzleDist;

        int pellets = w.getPelletCount();
        for (int i = 0; i < pellets; i++) {
            double offset = (pellets == 1) ? 0.0 : (i - (pellets - 1) / 2.0) * w.getSpreadAngle();
            double ang = player.getAimAngle() + offset + (Math.random() - 0.5) * 0.02;
            projectiles.add(new Projectile(
                    mx, my,
                    Math.cos(ang) * w.getProjectileSpeed(),
                    Math.sin(ang) * w.getProjectileSpeed(),
                    w.getProjectileRadius(),
                    scaledDmg,
                    w.getProjectileLife(),
                    true,
                    w.isPiercing(),
                    w.getProjectileColor(),
                    w.getRarityColor()
            ));
        }
    }

    public synchronized void tick(double dt) {
        if (!"PLAYING".equals(gameMode) || player == null) {
            return;
        }

        elapsedTime += dt;
        player.regeneratePassiveEnergy(dt);
        player.tickTimers(dt);

        if (firingHeld) {
            fireEquippedWeapon();
        }

        if (player.isDashing()) {
            player.setPosition(
                    player.getX() + player.getDashDirX() * 720.0 * dt,
                    player.getY() + player.getDashDirY() * 720.0 * dt
            );
        } else {
            double vx = (keyRight ? 1.0 : 0.0) - (keyLeft ? 1.0 : 0.0);
            double vy = (keyDown ? 1.0 : 0.0) - (keyUp ? 1.0 : 0.0);
            if (vx != 0.0 || vy != 0.0) {
                double len = Math.hypot(vx, vy);
                double speed = 268.0;
                player.setPosition(
                        player.getX() + (vx / len) * speed * dt,
                        player.getY() + (vy / len) * speed * dt
                );
                player.incrementWalkBob(dt);
            } else {
                player.resetWalkBob();
            }
        }
        resolveEntityWalls(player);

        for (int i = 0; i < chests.size(); i++) {
            RarityChest c = chests.get(i);
            c.update(dt);
        }

        updateEnemies(dt);
        updatePendingEnemyRespawns(dt);

        updateProjectiles(dt);

        updateLasers(dt);
    }

    private void updatePendingEnemyRespawns(double dt) {
        for (int i = 0; i < pendingEnemyRespawns.size();) {
            PendingEnemyRespawn pending = pendingEnemyRespawns.get(i);
            pending.remainingSeconds -= dt;
            if (pending.remainingSeconds <= 0.0) {
                PirateEnemy respawned = pending.enemy.respawnAs(nextEnemyId++);
                enemies.add(respawned);
                pushEvent(respawned.getName() + " returned to battle!", "#fb7185",
                        respawned.getX(), respawned.getY() - 24);
                pendingEnemyRespawns.remove(i);
            } else {
                i++;
            }
        }
    }

    private void resolveEntityWalls(Player p) {
        double px = p.getX();
        double py = p.getY();
        double r = p.getRadius();

        for (int i = 0; i < walls.size(); i++) {
            double[] res = walls.get(i).resolveCircleCollision(px, py, r);
            px = res[0];
            py = res[1];
        }
        for (int i = 0; i < gates.size(); i++) {
            SectorGate g = gates.get(i);
            if (!g.isOpened()) {
                double closestX = Math.max(g.getX(), Math.min(px, g.getX() + g.getWidth()));
                double closestY = Math.max(g.getY(), Math.min(py, g.getY() + g.getHeight()));
                double dx = px - closestX;
                double dy = py - closestY;
                double distSq = dx * dx + dy * dy;
                if (distSq < r * r) {
                    double dist = Math.sqrt(distSq);
                    if (dist > 0.0001) {
                        px += (dx / dist) * (r - dist);
                        py += (dy / dist) * (r - dist);
                    } else {
                        px += r;
                    }
                }
            }
        }
        p.setPosition(px, py);
    }

    private void updateEnemies(double dt) {
        boolean playerInThrone = player.getY() > 2500.0;

        for (int i = 0; i < enemies.size(); i++) {
            PirateEnemy en = enemies.get(i);
            if (!en.isAlive()) {
                continue;
            }
            if (en.isBoss() && !playerInThrone) {
                continue;
            }
            if (!en.isBoss() && playerInThrone && en.getY() < 2500.0) {
                continue;
            }

            en.tickTimers(dt);
            double dx = player.getX() - en.getX();
            double dy = player.getY() - en.getY();
            double dist = Math.hypot(dx, dy);
            en.setAimAngle(Math.atan2(dy, dx));

            double aggroRange = en.isBoss() ? 920.0 : 480.0;
            if (dist < aggroRange) {
                double preferredDist = en.isBoss() ? 240.0 : (en.getAttackRange() * 0.68);
                if (dist > preferredDist) {
                    en.setPosition(
                            en.getX() + (dx / dist) * en.getSpeed() * dt,
                            en.getY() + (dy / dist) * en.getSpeed() * dt
                    );
                } else if (en.isBoss()) {
                    en.setPosition(en.getX() + Math.cos(elapsedTime * 1.6) * en.getSpeed() * 0.7 * dt, en.getY());
                }

                if (en.getAttackTimer() <= 0.0 && dist <= en.getAttackRange() + 60.0) {
                    en.resetAttackTimer();
                    if (en.isBoss()) {
                        int phase = en.getBossPhase();
                        int shots = (phase >= 2) ? 7 : 5;
                        for (int s = 0; s < shots; s++) {
                            double ang = en.getAimAngle() + (s - (shots - 1) / 2.0) * 0.22;
                            projectiles.add(new Projectile(
                                    en.getX(), en.getY(),
                                    Math.cos(ang) * 430.0, Math.sin(ang) * 430.0,
                                    10.0, en.getDamage(), 2.2, false, false, "#e11d48", "#fb7185"
                            ));
                        }
                    } else {
                        double spd = "sniper".equals(en.getType()) ? 560.0 : 370.0;
                        double rad = "admiral".equals(en.getType()) ? 11.0 : 7.0;
                        projectiles.add(new Projectile(
                                en.getX(), en.getY(),
                                Math.cos(en.getAimAngle()) * spd, Math.sin(en.getAimAngle()) * spd,
                                rad, en.getDamage(), 1.8, false, false, en.getAccentColor(), en.getPrimaryColor()
                        ));
                    }
                }

                if (en.isBoss() && en.getSpecialTimer() <= 0.0) {
                    int phase = en.getBossPhase();
                    en.resetSpecialTimer(phase >= 2 ? 3.2 : 4.4);
                    for (int k = 0; k < 3; k++) {
                        double rx = player.getX() + (Math.random() - 0.5) * 320.0;
                        double ry = player.getY() + (Math.random() - 0.5) * 320.0;
                        lasers.add(new SkyLaser(rx, ry - 260, rx, ry + 20, 2.3, 0.0, 1.25, 0.75, 34, true,
                                (int) Math.round(en.getDamage() * 1.25)));
                    }
                }
            }

            double ex = en.getX();
            double ey = en.getY();
            for (int w = 0; w < walls.size(); w++) {
                double[] res = walls.get(w).resolveCircleCollision(ex, ey, en.getRadius());
                ex = res[0];
                ey = res[1];
            }
            en.setPosition(ex, ey);
        }
    }

    private void updateProjectiles(double dt) {
        int i = 0;
        while (i < projectiles.size()) {
            Projectile pr = projectiles.get(i);
            pr.update(dt);

            if (pr.isExpired() || hitsAnyWallOrClosedGate(pr.getX(), pr.getY())) {
                projectiles.remove(i);
                continue;
            }

            boolean removed = false;
            if (pr.isFromPlayer()) {
                for (int j = 0; j < enemies.size(); j++) {
                    PirateEnemy en = enemies.get(j);
                    if (!en.isAlive() || pr.hasAlreadyHit(en.getId())) {
                        continue;
                    }
                    double dist = Math.hypot(pr.getX() - en.getX(), pr.getY() - en.getY());
                    if (dist < pr.getRadius() + en.getRadius()) {
                        en.takeDamage(pr.getDamage());
                        if (pr.getDamage() > highestSingleHitDamage) {
                            highestSingleHitDamage = pr.getDamage();
                        }
                        pushEvent("-" + pr.getDamage(), "#fde047", en.getX(), en.getY() - en.getRadius() - 8);

                        if (!en.isAlive()) {
                            handleEnemyDefeated(en);
                        }

                        if (pr.isPiercing()) {
                            pr.recordHit(en.getId());
                        } else {
                            projectiles.remove(i);
                            removed = true;
                        }
                        break;
                    }
                }
            } else {
                double dist = Math.hypot(pr.getX() - player.getX(), pr.getY() - player.getY());
                if (dist < pr.getRadius() + player.getRadius() - 3.0) {
                    applyDamageToPlayer(pr.getDamage());
                    projectiles.remove(i);
                    removed = true;
                }
            }

            if (!removed) {
                i++;
            }
        }
    }

    private boolean hitsAnyWallOrClosedGate(double px, double py) {
        for (int i = 0; i < walls.size(); i++) {
            if (walls.get(i).containsPoint(px, py)) {
                return true;
            }
        }
        for (int i = 0; i < gates.size(); i++) {
            SectorGate g = gates.get(i);
            if (!g.isOpened() && px >= g.getX() && px <= g.getX() + g.getWidth() &&
                py >= g.getY() && py <= g.getY() + g.getHeight()) {
                return true;
            }
        }
        return false;
    }

    void handleEnemyDefeated(PirateEnemy en) {
        enemiesDefeatedCount++;
        if (en.isBoss()) {
            imuDefeated = true;
            player.addBerries(en.getBerryDrop());
            totalBerriesEarned += en.getBerryDrop();
            pushEvent("IMU DEFEATED! RETURN TO THE THOUSAND SUNNY!", "#fbbf24", en.getX(), en.getY() - 30);
            chests.add(new RarityChest(en.getX() - 60, en.getY() + 40, 4));
            chests.add(new RarityChest(en.getX() + 60, en.getY() + 40, 4));
            return;
        }

        pendingEnemyRespawns.add(new PendingEnemyRespawn(en, ENEMY_RESPAWN_SECONDS));
        player.addBerries(en.getBerryDrop());
        totalBerriesEarned += en.getBerryDrop();
        if (en.getGreenKeyDrop() > 0) {
            player.addGreenLogPoses(en.getGreenKeyDrop());
        }
        if (en.getGoldKeyDrop() > 0) {
            player.addGoldKeycard();
        }
        String rewardMsg = "+B" + en.getBerryDrop()
                + (en.getGreenKeyDrop() > 0 ? "  +" + en.getGreenKeyDrop() + " LogPose" : "")
                + (en.getGoldKeyDrop() > 0 ? "  +1 GOLD KEY!" : "");
        pushEvent(rewardMsg, "#34d399", en.getX(), en.getY() - 20);
    }

    private void updateLasers(double dt) {
        int i = 0;
        while (i < lasers.size()) {
            SkyLaser lz = lasers.get(i);
            lz.update(dt);
            if (lz.isFinishedOneShot()) {
                lasers.remove(i);
                continue;
            }
            if (lz.isFiring()) {
                double dist = lz.distanceToPoint(player.getX(), player.getY());
                if (dist < player.getRadius() + lz.getWidth() * 0.42) {
                    applyDamageToPlayer(lz.getDamage());
                }
            }
            i++;
        }
    }

    private void applyDamageToPlayer(int damage) {
        if (player.takeDamage(damage)) {
            if (player.getHp() < lowestHpReached) {
                lowestHpReached = player.getHp();
            }
            pushEvent("-" + damage + " HP", "#f87171", player.getX(), player.getY() - 26);
            if (player.getHp() <= 0.0) {
                gameMode = "DEFEAT";
                pushEvent("DEFEATED ON THE GRAND LINE!", "#ef4444", player.getX(), player.getY() - 40);
            }
        }
    }

    public String getCurrentZoneSummary() {
        String rawCode;
        if (player.getY() > 2500.0) {
            rawCode = "ZONE 4:THE EMPTY THRONE";
        } else if (player.getX() < 850.0) {
            rawCode = "ZONE 0:SAFE HARBOR DOCK";
        } else if (player.getX() < 1755.0) {
            rawCode = "ZONE 1:EAST BLUE CORAL SHORES";
        } else if (player.getX() < 2705.0) {
            rawCode = "ZONE 2:PONEGLYPH JUNGLE";
        } else {
            rawCode = "ZONE 3:MARY GEOISE VOLCANIC CLIFFS";
        }
        int sep = rawCode.indexOf(":");
        if (sep != -1) {
            return rawCode.substring(0, sep) + " - " + rawCode.substring(sep + 1);
        }
        return rawCode;
    }

    private void pushEvent(String text, String color, double x, double y) {
        recentEvents.add("{\"id\":" + (nextEventId++) + ",\"text\":\"" + escapeJson(text)
                + "\",\"color\":\"" + color + "\",\"x\":" + Math.round(x) + ",\"y\":" + Math.round(y) + "}");
        if (recentEvents.size() > 14) {
            recentEvents.remove(0);
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public synchronized String toJsonSnapshot() {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("{");
        sb.append("\"mode\":\"").append(gameMode).append("\",");
        sb.append("\"difficulty\":\"").append(DIFFICULTY_NAMES[difficultyIndex]).append("\",");
        sb.append("\"difficultyIndex\":").append(difficultyIndex).append(",");
        sb.append("\"gateReq\":").append(DIFFICULTY_GATE_REQ[difficultyIndex]).append(",");
        sb.append("\"elapsedTime\":").append(String.format(java.util.Locale.US, "%.2f", elapsedTime)).append(",");
        sb.append("\"showHitboxes\":").append(showHitboxes).append(",");
        sb.append("\"zoneLabel\":\"").append(escapeJson(getCurrentZoneSummary())).append("\",");
        sb.append("\"imuDefeated\":").append(imuDefeated).append(",");
        sb.append("\"secretVaultUnlocked\":").append(secretVaultUnlocked).append(",");
        sb.append("\"secretWeaponClaimed\":").append(secretWeaponClaimed).append(",");

        sb.append("\"metrics\":{");
        sb.append("\"kills\":").append(enemiesDefeatedCount).append(",");
        sb.append("\"chestsOpened\":").append(chestsOpenedCount).append(",");
        sb.append("\"totalBerriesEarned\":").append(totalBerriesEarned).append(",");
        sb.append("\"lowestHpReached\":").append(Math.round(lowestHpReached)).append(",");
        sb.append("\"highestSingleHit\":").append(highestSingleHitDamage);
        sb.append("},");

        sb.append("\"player\":{");
        sb.append("\"callsign\":\"").append(escapeJson(player.getCaptainCallsign())).append("\",");
        sb.append("\"x\":").append(String.format(java.util.Locale.US, "%.1f", player.getX())).append(",");
        sb.append("\"y\":").append(String.format(java.util.Locale.US, "%.1f", player.getY())).append(",");
        sb.append("\"radius\":").append(player.getRadius()).append(",");
        sb.append("\"hp\":").append(Math.ceil(player.getHp())).append(",");
        sb.append("\"maxHp\":").append(Math.round(player.getMaxHp())).append(",");
        sb.append("\"energy\":").append(Math.floor(player.getEnergy())).append(",");
        sb.append("\"maxEnergy\":").append(Math.round(player.getMaxEnergy())).append(",");
        sb.append("\"berries\":").append(player.getBerries()).append(",");
        sb.append("\"greenKeys\":").append(player.getGreenLogPoses()).append(",");
        sb.append("\"goldKeys\":").append(player.getGoldKeycards()).append(",");
        sb.append("\"aimAngle\":").append(String.format(java.util.Locale.US, "%.3f", player.getAimAngle())).append(",");
        sb.append("\"walkBob\":").append(String.format(java.util.Locale.US, "%.2f", player.getWalkBob())).append(",");
        sb.append("\"dashing\":").append(player.isDashing()).append(",");
        sb.append("\"equippedIndex\":").append(player.getEquippedWeaponIndex()).append(",");
        sb.append("\"weapons\":[");
        List<Weapon> weps = player.getWeapons();
        for (int i = 0; i < weps.size(); i++) {
            Weapon w = weps.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":\"").append(w.getId()).append("\",");
            sb.append("\"name\":\"").append(escapeJson(w.getName())).append("\",");
            sb.append("\"gimkitRef\":\"").append(escapeJson(w.getGimkitRef())).append("\",");
            sb.append("\"icon\":\"").append(w.getIcon()).append("\",");
            sb.append("\"cost\":").append(w.getUnlockCost()).append(",");
            sb.append("\"unlocked\":").append(w.isUnlocked()).append(",");
            sb.append("\"rarityIndex\":").append(w.getRarityIndex()).append(",");
            sb.append("\"rarityName\":\"").append(w.getRarityName()).append("\",");
            sb.append("\"rarityColor\":\"").append(w.getRarityColor()).append("\",");
            sb.append("\"upgradeCost\":").append(w.getNextUpgradeCost()).append(",");
            sb.append("\"maxRarity\":").append(w.isMaxRarity()).append(",");
            sb.append("\"damage\":").append(w.getScaledDamage()).append(",");
            sb.append("\"ammo\":").append(w.getCurrentAmmo()).append(",");
            sb.append("\"clipSize\":").append(w.getClipSize()).append(",");
            sb.append("\"reloading\":").append(w.isReloading()).append(",");
            sb.append("\"desc\":\"").append(escapeJson(w.getDescription())).append("\"");
            sb.append("}");
        }
        sb.append("]},");

        sb.append("\"walls\":[");
        for (int i = 0; i < walls.size(); i++) {
            WorldObstacle w = walls.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"x\":").append(w.getX()).append(",\"y\":").append(w.getY())
              .append(",\"w\":").append(w.getWidth()).append(",\"h\":").append(w.getHeight())
              .append(",\"type\":\"").append(w.getMaterialType()).append("\"}");
        }
        sb.append("],");

        sb.append("\"gates\":[");
        for (int i = 0; i < gates.size(); i++) {
            SectorGate g = gates.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"").append(g.getId()).append("\",\"name\":\"").append(escapeJson(g.getName()))
              .append("\",\"x\":").append(g.getX()).append(",\"y\":").append(g.getY())
              .append(",\"w\":").append(g.getWidth()).append(",\"h\":").append(g.getHeight())
              .append(",\"reqGreen\":").append(g.getRequiredGreenKeys())
              .append(",\"reqGold\":").append(g.getRequiredGoldKeys())
              .append(",\"opened\":").append(g.isOpened())
              .append(",\"color\":\"").append(g.getHexColor()).append("\"}");
        }
        sb.append("],");

        sb.append("\"chests\":[");
        for (int i = 0; i < chests.size(); i++) {
            RarityChest c = chests.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"x\":").append(Math.round(c.getX())).append(",\"y\":").append(Math.round(c.getY()))
              .append(",\"rarityIndex\":").append(c.getRarityIndex())
              .append(",\"opened\":").append(c.isOpened())
              .append(",\"bob\":").append(String.format(java.util.Locale.US, "%.2f", c.getBobPhase())).append("}");
        }
        sb.append("],");

        sb.append("\"enemies\":[");
        for (int i = 0; i < enemies.size(); i++) {
            PirateEnemy e = enemies.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":").append(e.getId()).append(",\"type\":\"").append(e.getType())
              .append("\",\"name\":\"").append(escapeJson(e.getName()))
              .append("\",\"x\":").append(String.format(java.util.Locale.US, "%.1f", e.getX()))
              .append(",\"y\":").append(String.format(java.util.Locale.US, "%.1f", e.getY()))
              .append(",\"radius\":").append(e.getRadius())
              .append(",\"hp\":").append(e.getHp()).append(",\"maxHp\":").append(e.getMaxHp())
              .append(",\"color\":\"").append(e.getPrimaryColor()).append("\",\"accent\":\"").append(e.getAccentColor())
              .append("\",\"boss\":").append(e.isBoss())
              .append(",\"hitFlash\":").append(e.getHitFlashTimer() > 0)
              .append(",\"aimAngle\":").append(String.format(java.util.Locale.US, "%.2f", e.getAimAngle())).append("}");
        }
        sb.append("],");

        sb.append("\"projectiles\":[");
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"x\":").append(String.format(java.util.Locale.US, "%.1f", p.getX()))
              .append(",\"y\":").append(String.format(java.util.Locale.US, "%.1f", p.getY()))
              .append(",\"radius\":").append(p.getRadius())
              .append(",\"color\":\"").append(p.getColor())
              .append("\",\"rarityColor\":\"").append(p.getRarityColor()).append("\"}");
        }
        sb.append("],");

        sb.append("\"lasers\":[");
        for (int i = 0; i < lasers.size(); i++) {
            SkyLaser l = lasers.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"x1\":").append(Math.round(l.getX1())).append(",\"y1\":").append(Math.round(l.getY1()))
              .append(",\"x2\":").append(Math.round(l.getX2())).append(",\"y2\":").append(Math.round(l.getY2()))
              .append(",\"width\":").append(l.getWidth())
              .append(",\"warning\":").append(l.isWarning())
              .append(",\"firing\":").append(l.isFiring()).append("}");
        }
        sb.append("],");

        sb.append("\"events\":[");
        for (int i = 0; i < recentEvents.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(recentEvents.get(i));
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    public Player getPlayer() { return player; }
    public String getGameMode() { return gameMode; }
    public boolean isImuDefeated() { return imuDefeated; }
    List<PirateEnemy> getEnemies() { return enemies; }
    public List<RarityChest> getChests() { return chests; }
    public List<SectorGate> getGates() { return gates; }
}
