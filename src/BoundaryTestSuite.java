public class BoundaryTestSuite {

    public static void main(String[] args) {
        runAllTests();
    }

    public static void runAllTests() {
        System.out.println("=== Running AP CSA Units 1 & 2 Boundary & Reliability Tests ===");

        Player testPlayer = new Player("StrawHatLuffyPirateKing:AdminHack", 500, 500);
        assertCondition(testPlayer.getCaptainCallsign().length() <= 18, "Callsign truncated via length() & substring()");
        assertCondition(testPlayer.getCaptainCallsign().indexOf(":") == -1, "Disallowed colon stripped via indexOf()");

        Player luffyAlias = new Player("Luffy", 500, 500);
        assertCondition("Monkey D. Luffy".equals(luffyAlias.getCaptainCallsign()), "Alias normalized via equals()");
        assertCondition(luffyAlias.outranksLexicographically("Zoro"), "Rank compared via compareTo()");
        assertCondition(luffyAlias.getBerries() == 0, "Player starts without berries");
        Player energyPlayer = new Player("Energy Test", 0, 0);
        assertCondition(energyPlayer.tryDash(1, 0), "Energy regeneration test consumes dash energy");
        energyPlayer.regeneratePassiveEnergy(1.0);
        assertCondition(energyPlayer.getEnergy() == 76.0, "Passive energy regenerates at one energy per second");
        assertCondition(GameEngine.shouldDropLogPose(0.0)
                && GameEngine.shouldDropLogPose(0.4999)
                && !GameEngine.shouldDropLogPose(0.50)
                && !GameEngine.shouldDropLogPose(1.0),
                "Log Pose drop chance uses the exact 50 percent boundary");
        GameEngine sectionEngine = new GameEngine();
        sectionEngine.resetGame("Normal", "Section Count Test");
        assertCondition(hasExactSectionEnemyCounts(sectionEngine, 2),
                "Every surface section has exactly twice its Log Pose requirement in enemies");
        PirateEnemy[] firstSectionEnemies = sectionEngine.getEnemies().stream()
                .filter(enemy -> !enemy.isBoss() && enemy.getX() < 1740)
                .limit(3).toArray(PirateEnemy[]::new);
        for (PirateEnemy enemy : firstSectionEnemies) {
            enemy.takeDamage(enemy.getHp());
            sectionEngine.handleEnemyDefeated(enemy, 0.99);
        }
        assertCondition(sectionEngine.getLootDrops().stream()
                .anyMatch(drop -> "LOG_POSE".equals(drop.getType())),
                "Last enemies guarantee enough Log Poses to open their section gate");

        GameEngine keyEngine = new GameEngine();
        keyEngine.resetGame("Normal", "Gold Key Test");
        PirateEnemy finalKeyOpportunity = keyEngine.getEnemies().get(0);
        keyEngine.getEnemies().clear();
        finalKeyOpportunity.takeDamage(finalKeyOpportunity.getHp());
        keyEngine.getEnemies().add(finalKeyOpportunity);
        keyEngine.handleEnemyDefeated(finalKeyOpportunity, 0.99);
        assertCondition(keyEngine.getPlayer().getGoldKeycards() == 3,
                "Remaining pirates guarantee enough Gold Keys to open the Secret Vault");
        GameEngine lockedVaultEngine = new GameEngine();
        lockedVaultEngine.resetGame("Normal", "Locked Vault Test");
        lockedVaultEngine.getPlayer().setPosition(3120, 540);
        assertCondition("GATE_LOCKED".equals(lockedVaultEngine.executePlayerAction("INTERACT", 0)),
                "The Secret Vault cannot be opened without Gold Keys");
        keyEngine.getPlayer().setPosition(3120, 540);
        assertCondition("GATE_UNLOCKED".equals(keyEngine.executePlayerAction("INTERACT", 0)),
                "Three Gold Keys open the Secret Vault");
        keyEngine.getPlayer().setPosition(3120, 540);
        assertCondition("SECRET_CLAIMED".equals(keyEngine.executePlayerAction("INTERACT", 0))
                && keyEngine.getPlayer().getWeapons().get(5).isUnlocked(),
                "Opening the Gold Key vault unlocks Conqueror's Haki");
        assertCondition(Weapon.UPGRADE_COSTS[0] == 45 && Weapon.UPGRADE_COSTS[3] == 280,
                "Weapon rarity upgrades require substantial berry costs");
        assertCondition(GameEngine.ENEMY_RESPAWN_SECONDS == 180.0, "Pirates respawn after three minutes");
        Player reloadPlayer = new Player("Reload Test", 0, 0);
        assertCondition(reloadPlayer.getWeapons().stream().allMatch(weapon ->
                weapon.getReloadDuration() == 3.0), "All weapons reload in three seconds");
        Weapon reloadTestWeapon = reloadPlayer.getWeapons().get(0);
        reloadTestWeapon.consumeShot();
        reloadTestWeapon.startReload();
        reloadTestWeapon.updateTimers(2.99);
        assertCondition(reloadTestWeapon.isReloading(), "Reload stays active before three seconds");
        reloadTestWeapon.updateTimers(0.02);
        assertCondition(!reloadTestWeapon.isReloading(), "Reload completes at three seconds");
        assertCondition(new RarityChest(0, 0, 4).getBerryReward() == 12,
                "Even the rarest chest gives fewer berries than an elite pirate captain");
        PirateEnemy fallenPirate = new PirateEnemy(1, "test", "Test Pirate", 40, 50, 10,
                20, 1, 1, 1, 10, 8, 0, 0, "#000000", "#ffffff", false, 1.0, 1.0);
        fallenPirate.takeDamage(20);
        PirateEnemy returnedPirate = fallenPirate.respawnAs(2);
        assertCondition(returnedPirate.isAlive() && returnedPirate.getHp() == returnedPirate.getMaxHp()
                && returnedPirate.getId() == 2, "Respawned pirates return alive with fresh identity");

        GameEngine respawnEngine = new GameEngine();
        respawnEngine.resetGame("Normal", "Test Captain");
        PirateEnemy defeatedPirate = respawnEngine.getEnemies().get(0);
        int defeatedPirateId = defeatedPirate.getId();
        double defeatedPirateX = defeatedPirate.getX();
        double defeatedPirateY = defeatedPirate.getY();
        defeatedPirate.takeDamage(defeatedPirate.getHp());
        respawnEngine.handleEnemyDefeated(defeatedPirate);
        respawnEngine.tick(GameEngine.ENEMY_RESPAWN_SECONDS - 0.01);
        assertCondition(respawnEngine.getEnemies().stream().noneMatch(enemy ->
                enemy.getX() == defeatedPirateX && enemy.getY() == defeatedPirateY
                        && enemy.getId() != defeatedPirateId),
                "Defeated pirates do not respawn before three minutes");
        respawnEngine.tick(0.02);
        assertCondition(respawnEngine.getEnemies().stream().anyMatch(enemy ->
                enemy.getX() == defeatedPirateX && enemy.getY() == defeatedPirateY
                        && enemy.getId() != defeatedPirateId && enemy.isAlive()),
                "Defeated pirates respawn after three minutes");

        GameEngine gateEngine = new GameEngine();
        gateEngine.resetGame("Normal", "Gate Test");
        PirateEnemy gateEnemy = gateEngine.getEnemies().get(0);
        gateEnemy.setPosition(1700, 1300);
        gateEngine.getPlayer().setPosition(2050, 1300);
        for (int i = 0; i < 120; i++) {
            gateEngine.tick(1.0 / 60.0);
        }
        assertCondition(gateEnemy.getX() + gateEnemy.getRadius() <=
                gateEngine.getGates().get(0).getX() + 0.01,
                "Enemies cannot cross a closed sector gate");
        gateEngine.getGates().get(0).setOpened(true);
        for (int i = 0; i < 180; i++) {
            gateEngine.tick(1.0 / 60.0);
        }
        assertCondition(gateEnemy.getX() > gateEngine.getGates().get(0).getX()
                + gateEngine.getGates().get(0).getWidth(),
                "Enemies can cross an opened sector gate");

        GameEngine lootEngine = new GameEngine();
        lootEngine.resetGame("Normal", "Loot Test");
        PirateEnemy defeatedLootEnemy = lootEngine.getEnemies().get(0);
        lootEngine.getPlayer().setPosition(defeatedLootEnemy.getX(), defeatedLootEnemy.getY());
        defeatedLootEnemy.takeDamage(defeatedLootEnemy.getHp());
        lootEngine.handleEnemyDefeated(defeatedLootEnemy, 0.20);
        assertCondition(lootEngine.getPlayer().getBerries() == 0
                && lootEngine.getLootDrops().stream().anyMatch(LootDrop::isBerry)
                && lootEngine.getLootDrops().stream().anyMatch(drop -> "LOG_POSE".equals(drop.getType())),
                "Enemy berries and Log Poses appear as collectible world drops");
        String lootSnapshot = lootEngine.toJsonSnapshot();
        assertCondition(lootSnapshot.contains("\"lootDrops\":[{\"type\":\"BERRY\"")
                && lootSnapshot.contains("\"type\":\"LOG_POSE\""),
                "Loot drops are included in the game snapshot");
        lootEngine.tick(1.0 / 60.0);
        assertCondition(lootEngine.getPlayer().getBerries() == defeatedLootEnemy.getBerryDrop()
                && lootEngine.getPlayer().getGreenLogPoses() == 1,
                "Nearby dropped berries and Log Poses are collected automatically");
        assertCondition(lootEngine.getEnemies().stream().noneMatch(enemy ->
                enemy.getId() == defeatedLootEnemy.getId()),
                "Defeated enemies are removed from the active simulation");

        GameEngine portalEngine = new GameEngine();
        portalEngine.resetGame("Normal", "Portal Test");
        assertCondition(portalEngine.getPortalRoutes().size() == 5,
                "Each surface section has a dock portal route");
        assertCondition(portalEngine.getPortalRoutes().stream().allMatch(route ->
                Math.hypot(route.getDockX() - 375, route.getDockY() - 1300) <= 400),
                "All surface portals are clustered at the ship's dock");
        PortalRoute coralPortal = portalEngine.getPortalRoutes().get(0);
        portalEngine.getPlayer().setPosition(coralPortal.getDockX(), coralPortal.getDockY());
        assertCondition("PORTAL_CANNOT_AFFORD".equals(portalEngine.executePlayerAction("INTERACT", 0))
                && !coralPortal.isUnlocked(), "Portal stays locked when the player cannot afford it");
        portalEngine.getPlayer().addBerries(coralPortal.getBerryCost());
        assertCondition("PORTAL_UNLOCKED".equals(portalEngine.executePlayerAction("INTERACT", 0))
                && coralPortal.isUnlocked()
                && portalEngine.getPlayer().getBerries() == 0,
                "Portal purchase spends berries and unlocks it for the current game");
        assertCondition("WARPED".equals(portalEngine.executePlayerAction("INTERACT", 0))
                && portalEngine.getPlayer().getX() == coralPortal.getDestinationX(),
                "Unlocked dock portal travels to its section");
        assertCondition("WARPED".equals(portalEngine.executePlayerAction("INTERACT", 0))
                && portalEngine.getPlayer().getX() == coralPortal.getDockX(),
                "Section portal returns to the dock");
        assertCondition(portalEngine.toJsonSnapshot().contains("\"portalRoutes\":["),
                "Portal routes and unlock status are serialized");
        portalEngine.resetGame("Normal", "Portal Reset Test");
        assertCondition(!portalEngine.getPortalRoutes().get(0).isUnlocked(),
                "Portal unlocks reset when a new game starts");

        GameEngine hardEngine = new GameEngine();
        hardEngine.resetGame("Hard", "Difficulty Test");
        GameEngine ludicrousEngine = new GameEngine();
        ludicrousEngine.resetGame("Ludicrous", "Difficulty Test");
        assertCondition(ludicrousEngine.getEnemies().get(0).getMaxHp()
                > hardEngine.getEnemies().get(0).getMaxHp()
                && ludicrousEngine.getEnemies().get(0).getDamage()
                > hardEngine.getEnemies().get(0).getDamage(),
                "Ludicrous enemies gain substantially more health and damage");
        assertCondition(hasExactSectionEnemyCounts(ludicrousEngine, 5),
                "Ludicrous section enemy counts scale with Log Pose requirements");
        GameEngine easyEngine = new GameEngine();
        easyEngine.resetGame("Easy", "Difficulty Test");
        GameEngine normalEngine = new GameEngine();
        normalEngine.resetGame("Normal", "Difficulty Test");
        assertCondition(easyEngine.getEnemies().get(0).getMaxHp()
                < normalEngine.getEnemies().get(0).getMaxHp()
                && easyEngine.getEnemies().get(0).getDamage()
                < normalEngine.getEnemies().get(0).getDamage(),
                "Easy difficulty reduces enemy health and damage");

        GameEngine chestEngine = new GameEngine();
        chestEngine.resetGame("Normal", "Test Captain");
        RarityChest nearbyChest = chestEngine.getChests().get(0);
        chestEngine.getPlayer().setPosition(nearbyChest.getX(), nearbyChest.getY());
        chestEngine.tick(1.0 / 60.0);
        assertCondition(!nearbyChest.isOpened(), "Walking over a chest does not open it automatically");
        chestEngine.executePlayerAction("INTERACT", 0);
        assertCondition(nearbyChest.isOpened(), "Nearby chests open through the interact action");
        nearbyChest.update(39.99);
        assertCondition(nearbyChest.isOpened(), "Opened chests stay closed for nearly 40 seconds");
        nearbyChest.update(0.02);
        assertCondition(!nearbyChest.isOpened(), "Opened chests return after 40 seconds");

        assertCondition(!testPlayer.buyWeapon(4), "Purchase rejected when berries < cost");

        testPlayer.applyChestRewards(9999, 9999, 50, false);
        assertCondition(testPlayer.getHp() == testPlayer.getMaxHp(), "HP clamped at maxHp");
        assertCondition(testPlayer.getEnergy() == testPlayer.getMaxEnergy(), "Energy clamped at maxEnergy");

        testPlayer.takeDamage(-50.0);
        assertCondition(testPlayer.getHp() == testPlayer.getMaxHp(), "Negative damage safely ignored");
        testPlayer.takeDamage(9999.0);
        assertCondition(testPlayer.getHp() == 0.0, "HP floored cleanly at 0.0");

        assertCondition(!testPlayer.selectWeaponSlot(-1), "Negative weapon slot rejected");
        assertCondition(!testPlayer.selectWeaponSlot(99), "Out-of-range weapon slot rejected");

        GameEngine engine = new GameEngine();
        assertCondition("Normal".equals(GameEngine.DIFFICULTY_NAMES[
                engine.resolveDifficultyIndex("InvalidMode")]), "Unknown difficulty defaults to Normal");
        assertCondition(GameEngine.DIFFICULTY_NAMES.length == 5, "Only five difficulty levels are available");

        System.out.println("=== All Boundary & Reliability Assertions PASSED! ===");
    }

    private static void assertCondition(boolean condition, String label) {
        if (!condition) {
            throw new IllegalStateException("Boundary test failed: " + label);
        }
        System.out.println("  [PASS] " + label);
    }

    private static boolean hasExactSectionEnemyCounts(GameEngine engine, int gateRequirement) {
        int[] sectionMinX = {260, 1778, 2728, 3628, 4578};
        int[] sectionMaxX = {1740, 2690, 3590, 4540, 5360};
        int[] extraLogPoseRequirements = {0, 0, 1, 1, 2};
        for (int i = 0; i < sectionMinX.length; i++) {
            final int sectionStart = sectionMinX[i];
            final int sectionEnd = sectionMaxX[i];
            long enemiesInSection = engine.getEnemies().stream()
                    .filter(enemy -> !enemy.isBoss()
                            && enemy.getX() >= sectionStart && enemy.getX() < sectionEnd)
                    .count();
            if (enemiesInSection != 2L * (gateRequirement + extraLogPoseRequirements[i])) {
                return false;
            }
        }
        return true;
    }
}
