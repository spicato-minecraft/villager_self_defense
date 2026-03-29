# Villager Self Defense

Fabric mod for Minecraft **1.21.10** that gives villagers defensive behavior (implementation TBD).

## Compatibility

| Component    | Version        |
|-------------|----------------|
| Minecraft   | 1.21.10        |
| Fabric Loader | 0.18.5+      |
| Fabric API  | 0.138.4+1.21.10 |
| Java        | 21+            |

## Build

From this directory:

```bash
./gradlew build
```

The output JAR is at `build/libs/villager_self_defense-1.0.0.jar` (version from `gradle.properties`).

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.10.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.10 and place it in your mods folder.
3. Copy the built JAR into your Minecraft `mods` folder (`.minecraft/mods` on most setups).

## Development

Open the project in your IDE and import as a Gradle project. Run the `client` or `server` Gradle tasks to launch the game with the mod.
