# SeasonRPG Demo

Playable **core loop** for a seasonal Survival RPG Minecraft server (Paper 1.21.4).
Runs on a clean Paper server — **no external plugins required**. Built to validate
the fun before investing in a full Season 1.

## The four pillars

| System | What it does |
|---|---|
| **Season** | Live boss bar: "Season 1 · Week X/8 · ends in…", persistent clock |
| **Lives** | 5 lives per player; a dramatic server-wide "LAST LIFE" broadcast; elimination → spectator |
| **Classes** | Warrior (dash) vs Archer (power shot); pick via menu, ability on shift + right-click |
| **Dynamic event** | The "Invasion": warning countdown → mob wave → collective defense → rewards |

Everything is tunable in `config.yml` without recompiling. In the paid build the
vanilla event mobs are replaced by **MythicMobs** bosses without changing this structure.

## Build

**Linux (VPS):**
```bash
sudo apt install openjdk-21-jdk curl   # if needed
./build.sh                             # -> build/SeasonRPGDemo-0.1.0.jar
```

**Windows:**
```powershell
./build.ps1
```

Both scripts download Paper 1.21.4, extract its API, compile, and package the jar.

## Run (VPS)

```bash
./build.sh
./start.sh          # copies the jar into server/plugins and boots Paper
```
Then make yourself admin in the console: `op YOUR_NAME`.

Full step-by-step (PT-BR), commands, and playit.gg tunnel: see **[COMO-RODAR.md](COMO-RODAR.md)**.

## In-game commands

| Command | Purpose |
|---|---|
| `/season` | Season summary |
| `/class` | Open class picker |
| `/ability` | Use class ability (also: shift + right-click) |
| `/lives` | Show remaining lives |
| `/event start\|stop` | Trigger/stop an invasion (admin) |
| `/seasonadmin setweek <n>` / `reset` / `resetlives` / `givelife <p> <n>` | Admin utilities |

## Project layout

```
src/main/java/com/seasonrpg/demo/
  SeasonRPGPlugin.java     main entry
  season/                  season clock
  lives/                   lives + last-life narrative
  classes/                 class choice + abilities
  event/                   dynamic invasion event
  hud/                     boss bar + action bar
  command/                 command handlers
src/main/resources/        plugin.yml, config.yml
build.sh / build.ps1       build without Gradle (javac)
build.gradle.kts           optional Gradle build
```

## Status

Compiles cleanly against the real Paper 1.21.4 API and is loaded/initialized by Paper
without errors. Ready to boot and playtest on any machine (this repo's original build
host had an unrelated VPN driver that crashed all socket binds — not a plugin issue).
