# Phase A2 — Testing (Tier 0b player-activated defense)

Manual checklist for Tier 0b: rolling hit window, 3/6 thresholds, qualifying hits, and the same Defense pipeline as Tier 0 once active. See [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md), [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md), and [phaseATest.md](phaseATest.md) for Tier 0 regressions.

---

## Environment setup

Same as [phaseATest.md](phaseATest.md) (JDK 21, Fabric 1.21.10, `./gradlew runClient` / `runServer`).

After upgrading from Phase A, **`config/villager_self_defense.json`** gains Tier 0b keys with defaults if missing (see [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java)). Legacy `retaliateAgainstPlayers` is read once to set `playerActivationEnabled` when `playerActivationEnabled` is absent.

| Key | Default | Role |
|-----|---------|------|
| `playerActivationEnabled` | `true` | Master toggle for Tier 0b player path |
| `playerHitWindowSeconds` | `10` | Rolling window for hit counts |
| `playerHitsToActivate` | `3` | Qualifying hits needed to enter defense (or retarget to a new player) |
| `playerHitsWhileInDefense` | `6` | In-window hits while already defending vs same player (escalation hook) |

Creative players and **Peaceful** world difficulty never trigger Tier 0b (CORE; not configurable). Test player retaliation on **Easy+** Survival worlds.

---

## World / scenario setup

1. **Survival** world, **Easy+** for Tier 0b player-damage tests (mob Tier 0 can use any non-Peaceful difficulty where hostiles can apply).
2. Adult villager, no baby.
3. Optional second adult villager for **village-wide** hit counting (hits spread across villagers).

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **One punch — no defense** | Survival, punch adult villager **once** (or twice), wait &lt; window | Villager does **not** enter mod defense vs player. |
| 2 | **Three hits in window — defense** | Within **10s** (default), land **3** qualifying melee hits on the **same** villager | On the **third** hit, villager enters defense: paths toward player, melee; trading closes if open. |
| 3 | **Tier 0 mob unchanged** | Zombie hits villager (no player gate) | **First** mob hit still triggers defense immediately (Tier 0). |
| 4 | **Creative ignored** | Creative mode, punch villager 5+ times | No Tier 0b defense vs player (always; not configurable). |
| 5 | **Peaceful ignored** | Peaceful difficulty, Survival, punch villager 5+ times | No Tier 0b hit counting / activation (CORE; vanilla also suppresses mob→player damage on Peaceful). |
| 6 | **Village-wide bucket (optional)** | Hit villager A once, villager B once, then villager C **third** qualifying hit within window | **Third** victim (C) enters defense when threshold crossed; confirms shared per-player window. |
| 7 | **Mob then player retarget** | Trigger defense vs zombie, then punch player hits on villager | Villager keeps fighting zombie until **3** qualifying player hits in window; then retargets to player. |
| 8 | **Six-hit escalation hook** | Already defending vs player; land **6** qualifying hits within window | No crash; combat still refreshes; escalation hook reserved for Phase B (no ally broadcast yet). |
| 9 | **Stand-down / trading** | After player defense, kill/remove threat or wait quiet window | Same as Tier 0: stand-down, trading allowed again — see [phaseATest.md](phaseATest.md) cases 4–5, 6–7. |
| 10 | **Indirect damage does not count** | Set villager on fire via indirect means without direct melee/projectile attribution per mod rules | Hit count toward 3 does **not** increase from that damage alone (CORE: indirect excluded “for now”). |

---

## Regression

- Re-run Tier 0 cases in [phaseATest.md](phaseATest.md) (mob retaliation, baby, distractions). Note case **2** there described **Phase A** only; with Phase A2, use this doc for player behavior.

---

## Troubleshooting

| Symptom | Check |
|---------|-------|
| Player never gets fought | `playerActivationEnabled` true; **Survival** (creative never triggers Tier 0b); **Easy+** (Peaceful never triggers Tier 0b); **3** qualifying hits in window; direct melee/projectile (not indirect-only). |
| Instant player defense | Should not happen with defaults — verify `playerHitsToActivate` in config. |
