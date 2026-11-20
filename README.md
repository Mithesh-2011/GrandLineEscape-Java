# Grand Line Escape

A top-down action game with a Spring Boot backend and browser-based Canvas client.

## Run locally

Requirements: JDK 17 or newer and Maven 3.8+.

Run the provided script:

```bash
./run.sh
```

On Windows, double-click `run.bat`.

Or run it directly with Maven:

```bash
mvn spring-boot:run
```

Open <http://localhost:8080>. On desktop systems the launcher opens the browser automatically. Set `PORT` to change the listening port or `OPEN_BROWSER=false` to disable browser launching. The REST API is served by Spring MVC, and Spring's embedded Tomcat handles HTTP, JSON, compression, and health requests.

Run the test suite with:

```bash
mvn test
```

## Publish online

The app needs a Java server; it cannot be hosted as a static-only site. A Spring Boot executable JAR, Docker image, and Render Blueprint are included:

1. Push this repository to GitHub.
2. In Render, choose **New → Blueprint**, connect the repository, and deploy the detected `render.yaml`.
3. Wait for the service health check to pass, then share its public URL.

You can also build and run the container on any Docker-compatible host:

```bash
docker build -t grand-line-escape .
docker run --rm -p 8080:8080 grand-line-escape
```

The service listens on `0.0.0.0` and uses the host-provided `PORT`. `/health` is available for platform health checks. HTTPS termination should be provided by the hosting platform or reverse proxy.

Each browser gets a separate, anonymous game session. Sessions are held in memory for up to six hours; they are not saved and will be lost on a server restart. Run one server instance unless session storage is added for multi-instance deployments.

**Before public release:** this game uses One Piece names and characters. Confirm you have the rights needed to publish it, or replace those elements with original content; hosting it does not grant those rights.

## How to play

1. Choose Easy, Normal, Hard, or Expert and a captain name on the home screen, then select **Start game**. Easy uses weaker enemies; each difficulty has 2, 4, 6, or 8 enemies in each surface section.
2. You start with **0 berries**. Defeated pirates leave berry pickups and have a **50% chance** to drop a Log Pose; move close to collect either item. Berry rewards are about **15% higher** than before.
3. Chests must be opened with **E** when nearby, provide health, energy, and berries, and return after 40 seconds. Chests appear in every surface section and in the Empty Throne realm.
4. Cross five surface sections, collecting Log Poses to open the guarded gates. Enter the Gold Key portal in section 2 to fight three stronger, non-respawning guardians in their own realm; each drops one Gold Key. Regular enemies and chests do not drop Gold Keys. Use all three keys to open the Secret Poneglyph Vault and claim Gear 5.
5. Spend berries at the armory and forge. Armory weapons cost **15, 28, 45, and 68 berries**; rarity upgrades cost **23, 45, 80, then 140 berries**. Armory listings show weapon damage, ammo capacity, and energy cost; the currently selected weapon is highlighted.
6. Press **B** to return to base from anywhere, spending one Log Pose. Pirates cannot attack and weapons cannot be fired while you are on the full brown harbor pad.
7. Pirates return at their defeat location after **3 minutes**. Enemy firing rates are 30% lower, and all enemies—including bosses—deal 50% less damage than before. All weapons take **3 seconds** to reload; energy regenerates at **1 point per second**, and each shot costs 1 energy. Enemy health and damage scale with difficulty.
8. Use the portal in section 5 to reach IMU's separate Empty Throne realm. Defeat the boss, return through the portal, then interact with the ship to win.

## Controls

- **Move Luffy:** `W` `A` `S` `D` or Arrow Keys
- **Aim & Attack:** Mouse Cursor + `Left Click` *(each shot costs 1 Energy)*
- **Gum-Gum Rocket Dash:** `Space` or `Shift` *(costs 25 Energy; grants brief invulnerability; energy regenerates at 1 per second)*
- **Open Chests / Unlock Gates / Use Armory, Forge & Thousand Sunny:** `E` (stand close)
- **Return to Base:** `B` *(costs 1 Green Log Pose)*
- **Portals:** press `E` at the section 2 portal to enter the Gold Key realm, or at the section 5 portal to enter the Empty Throne realm. Each realm has a return portal.
- **Gear 5:** claim the vault reward with `E` to increase player damage by 40%, raise maximum health to 150, and regenerate 2 energy per second. It is an upgrade, not a weapon.
- **Pause / Resume:** `P` *(the pause screen also lets you return home)*
- **Instructions:** `I`
- **Reload Weapon:** `R`
- **Switch Weapons (Slots 1–5):** Number Keys `1`–`5`
