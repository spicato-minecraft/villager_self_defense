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

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties` (e.g. `1.21.10`, `1.20.10`). Version branches are the source of truth; `main` is not used.

- **Default branch:** latest supported version (`1.21.10`)
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch
- **New MC version:** create `{version}` from the prior version branch, bump `gradle.properties`, push to origin
- **Retiring a version:** tag the final release, then archive the branch (do not delete)

```bash
git clone https://github.com/spicato-spicato/villager_self_defense.git
cd villager_self_defense
git checkout 1.21.10   # or 1.20.10 for the older supported version
```
