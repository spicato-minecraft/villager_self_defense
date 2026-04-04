# Phase A — Testing (Tier 0 mob retaliation)

This document matches the Phase A exit criteria: mob-triggered defense for adult villagers, stand-down, Brain priority over routine tasks, and trading UI rules. See [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md) and [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md).

---

## Environment setup

| Requirement | Notes |
|-------------|--------|
| **JDK** | **21** (matches `build.gradle` `release = 21`). |
| **Project** | Fabric mod for **Minecraft 1.21.10**; versions in [gradle.properties](gradle.properties) (`loader_version`, `fabric_api_version`, `minecraft_version`). |
| **Build** | From the project root: `./gradlew build` |
| **Run client** | `./gradlew runClient` (uses the same Gradle/JDK as above). |
| **Run dedicated server** | `./gradlew runServer` — optional for multiplayer checks. |
| **Logs** | Client: `run/logs/latest.log` (or console). Dedicated server: server `logs/latest.log`. |

On first launch, the mod creates **`config/villager_self_defense.json`** with defaults (`standDownQuietSeconds`: 30, `mobDefenseEnabled`: true, Tier 0b keys such as `playerActivationEnabled`, `playerHitWindowSeconds`, `playerHitsToActivate`). Edit to tune stand-down, disable mob defense, or adjust player hit windows.

---

## World / scenario setup

1. Create a **Creative** or **Survival** world (Easy+ so hostiles spawn as usual).
2. Use a **flat** world or a **village** seed so you can place a villager and a hostile quickly.
3. **Adult villager:** spawn or cure; confirm not a baby.
4. **Hostile:** zombie, skeleton, or other non-player mob that can damage the villager.
5. **Distraction props (Brain priority):** place a **bell**, **bed**, and **workstation** (e.g. lectern) near the villager to observe whether combat keeps priority while defense is active.

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **Mob hit → retaliation** | Spawn adult villager + zombie; let zombie hit villager | Villager paths toward zombie and attacks (melee); zombie takes damage over time. |
| 2 | **Player hit — Phase A only** | Punch adult villager in Survival | In **Phase A** alone: no mod defense vs player. With **Phase A2** (Tier 0b), see [phaseA2Test.md](phaseA2Test.md): single hit does not trigger; repeated hits in the window do. |
| 3 | **Baby does not defend** | Repeat with **baby** villager + zombie | Baby does not use mod defense melee; may use vanilla panic. |
| 4 | **Stand-down on kill** | Let villager kill zombie | Villager stops attacking; after a short time behavior returns toward normal schedule. |
| 5 | **Stand-down on timeout** | Trigger defense, then remove/kill attacker or stop damage; wait **> `standDownQuietSeconds`** (default 30s) without new hits | Defense ends; villager no longer stuck in fight pose vs old target. |
| 6 | **Trading closes on defense** | Open trade with villager (second player or quick setup), then trigger defense (e.g. have zombie hit villager) | Merchant UI **closes** for that villager. |
| 7 | **Trading blocked while defending** | While villager is defending, try to open trade again | Interaction does **not** open trading until stand-down. |
| 8 | **Multiplayer — only that villager** | Two players: P1 trades villager A, P2 trades villager B; trigger defense only on A | Only P1’s menu closes; P2 unaffected if B not defending. |
| 9 | **Distractions (bell / bed / workstation)** | While villager is defending, ring bell, or let night fall near bed, or use workstation POI | Villager should **not** abandon combat for those behaviors while defense is active (visual check: keeps engaging threat). |

---

## Regression / future

- **Tier 0b** (player hit windows): use [phaseA2Test.md](phaseA2Test.md). **Tier 1** (allies) remains out of scope for this checklist unless you extend the doc.
- **Automated GameTests** are optional and not required for Phase A sign-off.

---

## Troubleshooting

| Symptom | Check |
|---------|--------|
| No `config/villager_self_defense.json` | Run client once; ensure write access to the instance `config/` folder. |
| Villager never fights | Confirm `mobDefenseEnabled` is true; confirm damage is from a **non-player** living entity; check logs for errors. |
| Crash on trade close | Report stack trace from `latest.log`; merchant menu uses `MerchantMenu` + accessor for `trader`. |
