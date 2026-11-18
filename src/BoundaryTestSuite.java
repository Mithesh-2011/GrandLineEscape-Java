public class BoundaryTestSuite {

    public static void runAllTests() {
        System.out.println("=== Running AP CSA Units 1 & 2 Boundary & Reliability Tests ===");

        Player testPlayer = new Player("StrawHatLuffyPirateKing:AdminHack", 500, 500);
        assertCondition(testPlayer.getCaptainCallsign().length() <= 18, "Callsign truncated via length() & substring()");
        assertCondition(testPlayer.getCaptainCallsign().indexOf(":") == -1, "Disallowed colon stripped via indexOf()");

        Player luffyAlias = new Player("Luffy", 500, 500);
        assertCondition("Monkey D. Luffy".equals(luffyAlias.getCaptainCallsign()), "Alias normalized via equals()");
        assertCondition(luffyAlias.outranksLexicographically("Zoro"), "Rank compared via compareTo()");
        assertCondition(luffyAlias.getBerries() == 0, "Player starts without berries");
        assertCondition(Weapon.UPGRADE_COSTS[0] == 45 && Weapon.UPGRADE_COSTS[3] == 280,
                "Weapon rarity upgrades require substantial berry costs");
        assertCondition(GameEngine.ENEMY_RESPAWN_SECONDS == 2.0, "Pirates respawn after two seconds");
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
                "Defeated pirates do not respawn before two seconds");
        respawnEngine.tick(0.02);
        assertCondition(respawnEngine.getEnemies().stream().anyMatch(enemy ->
                enemy.getX() == defeatedPirateX && enemy.getY() == defeatedPirateY
                        && enemy.getId() != defeatedPirateId && enemy.isAlive()),
                "Defeated pirates respawn after two seconds");

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
}
