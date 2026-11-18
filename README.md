# Grand Line Escape — Java Backend Architecture

A top-down action escape game themed around **One Piece**, engineered with an authoritative **12-class Java Object-Oriented Backend** (`com.sun.net.httpserver.HttpServer` + 60 Hz simulation engine) and a **60 FPS Browser Canvas Client**.

---

## Complete Architecture (`src/` + `web/`)

```text
GrandLine-JavaBackend/
├── src/
│   ├── Main.java               # Entry point: runs boundary tests, starts server & opens browser
│   ├── GameServer.java         # Built-in Java HttpServer (port 8080) & REST JSON API
│   ├── GameEngine.java         # 60 Hz simulation engine, map builder, collisions, trackers & JSON serializer
│   ├── Player.java             # Monkey D. Luffy class (HP, Energy, Dash, Weapons, String methods)
│   ├── Weapon.java             # 6 weapons with Common -> Legendary rarity upgrades
│   ├── PirateEnemy.java        # Villain pirates & multi-phase Final Boss IMU
│   ├── RarityChest.java        # Weighted Math.random() chests replacing trivia questions for HP/Energy
│   ├── SectorGate.java         # Progression barriers unlocked with Green Log Poses & Gold Keycards
│   ├── WorldObstacle.java      # Solid walls/rocks/pillars with Circle-vs-AABB sliding collision math
│   ├── Projectile.java         # Active projectiles with piercing support
│   ├── SkyLaser.java           # Stage 3 Buster Call & Imu Mother Flame orbital sky-beams
│   └── BoundaryTestSuite.java  # Automated Unit 1 & 2 boundary & input-validation test suite
├── web/
│   └── index.html              # 60 FPS browser renderer, illustrated level-selection screen, HUD and minimap
├── run.bat                     # One-click Windows compile & launch script
├── run.sh                      # One-click macOS / Linux compile & launch script
├── APCSA_Project_Planning_And_Rubric_Guide.md  # Ready-to-submit AP CSA planning template & video script
└── README.md                   # Architecture & gameplay guide
```

---

## How to Run (Zero External JARs Required!)

### Option A: One-Click Scripts
- **Windows:** Double-click **`run.bat`**.
- **macOS / Linux:** Open Terminal inside the extracted folder and run:
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option B: Manual Terminal Commands
```bash
mkdir -p out
javac -d out src/*.java
java -cp out Main
```
Once started, open **`http://localhost:8080`** in your browser (it opens automatically on most desktops).

---

## How to Play

1. Choose a level and captain name on the home screen, then select **Start game**.
2. You start with **0 berries**. Defeat pirates for **8–34 or more berries each**; this is the main way to earn currency.
3. Chests are sparse, must be opened with **E** when nearby, give only **1–12 berries**, and take 40 seconds to return after opening.
4. Collect Log Poses and Gold Keys from enemies and chests to unlock gates and reach the final arena.
5. Spend berries at the armory and forge. New weapons cost **35–160 berries**; rarity upgrades cost **45, 90, 160, then 280 berries**.
6. Pirates return at their defeat location after 2 seconds. Defeat the final boss and return to the ship to win.

## Controls

- **Move Luffy:** `W` `A` `S` `D` or Arrow Keys
- **Aim & Attack:** Mouse Cursor + `Left Click`
- **Gum-Gum Rocket Dash:** `Space` or `Shift` *(costs 25 Energy; grants brief invulnerability)*
- **Open Chests / Unlock Gates / Use Armory, Forge, Portals & Thousand Sunny:** `E` (stand close)
- **Reload Weapon:** `R`
- **Switch Weapons (Slots 1–6):** Number Keys `1`–`6`
- **Toggle Live Collision Hitboxes:** `H`
