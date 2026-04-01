# Phase B2 — Testing (Tier 1b multi-attacker dispersal)

Manual checklist for Tier 1b: when **multiple** valid threats exist inside the ally sphere, defenders split by **nearest-threat** assignment (greedy), with **throttled** rebalance, optional **soft cap** per threat, **broad** pack threat refresh for stand-down, and integration with Tier 1 ally notify (including per-hit notify **attempts** and `allyNotifyMinTicks` throttle). See [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md), [phaseBTest.md](phaseBTest.md) (Tier 1), and [DefenseDispersal](src/main/java/villager_self_defense/defense/DefenseDispersal.java).

---

## Environment setup

Same base setup as [phaseATest.md](phaseATest.md) (JDK 21, Fabric 1.21.10, `./gradlew runClient` / `runServer`).

**`config/villager_self_defense.json`** — Tier 1 keys from [phaseBTest.md](phaseBTest.md) still apply. Add or rely on defaults for Tier 1b:

| Key | Default | Role |
|-----|---------|------|
| `dispersalEnabled` | `true` | Master toggle for multi-threat assignment (off → Phase B single shared target behavior for assignment) |
| `dispersalMaxAttackersK` | `8` | Max threat entities considered per rebalance (sorted by distance to anchor) |
| `dispersalRebalanceMinTicks` | `15` | Min ticks between full rebalance passes **per damage victim (anchor)** unless **forced** (threat set signature changed on a damage event) |
| `dispersalMaxDefendersPerAttacker` | `0` | Soft max defenders per threat (`0` = unlimited); when &gt; 0, load-spreads to next-nearest |

Threat eligibility: `MobCategory.MONSTER` mobs and non–creative / non-Peaceful **players** in sphere; excludes villagers and iron golems ([DefenseEligibility.isValidThreatTarget](src/main/java/villager_self_defense/defense/DefenseEligibility.java)).

---

## World / scenario setup

1. **Easy+** Survival (not Peaceful) for mob + player threat tests.
2. **Two or more adult villagers** within **`allyRadius`** (3D) of a central test area.
3. **Two or more hostiles** (e.g. zombies) placed so both are inside the **same** ally sphere around the **victim** when the victim is hit — e.g. opposite sides of a small cluster, both within 16 blocks of the victim.
4. Optional: **third+** mobs beyond `dispersalMaxAttackersK` to confirm only **K** threats participate.
5. For **player** as one of multiple threats: Survival, non-creative; combine with a zombie in the same sphere (both must count as valid threats).

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **Two mobs — split** | Adults V1…Vn and **two** zombies Z1, Z2 inside `allyRadius` of V1; damage **V1** with Z1 (or let Z1 aggro) so defense + notify run | After rebalance, defenders **not** all stacked on one zombie when both qualify; assignments tend toward **nearest** zombie per villager (allow a tick or two / second hit for throttle). |
| 2 | **One mob — Phase B** | Same cluster but only **one** zombie in the sphere | Behavior matches Phase B: single shared target; no dispersal-only churn. |
| 3 | **`dispersalEnabled` false** | Two zombies in sphere; trigger defense | Everyone focuses the **broadcast** attacker (Phase B); dispersal does not re-split. |
| 4 | **Broad pack refresh** | V1 vs Z1, V2 vs Z2 (both in defense, different targets); stand within `allyRadius` of **V1**; damage **V2** only | **`lastThreatGameTime`** refreshes for **both** (and other defending adults in sphere) so the pack does **not** stand down on “wrong” attacker ID ([refreshPackThreatNeighbors](src/main/java/villager_self_defense/defense/AllyDefenseNotifier.java)). |
| 5 | **Forced rebalance** | Two threats in sphere; note positions; add a **third** valid threat entering the sphere (or first tick where signature changes) | Rebalance can run before `dispersalRebalanceMinTicks` elapses when the **threat set** changes (damage-driven path). |
| 6 | **Throttle** | Two stable threats; spam hits on one victim | Full greedy rebalance does **not** run every tick; pathing should not thrash wildly (sticky + interval). |
| 7 | **Soft cap** | Set `dispersalMaxDefendersPerAttacker` to e.g. `2`; many villagers, two zombies | No more than **2** villagers preferring the same nearest zombie when another threat has capacity (approximate; may exceed cap if all nearest slots “full” — then nearest anyway). |
| 8 | **K cap** | Spawn **&gt; K** valid monsters in sphere (or lower `dispersalMaxAttackersK` temporarily) | Only up to **K** threats used in the sorted list; no runaway cost (subjective TPS / profiler). |
| 9 | **Recruitment skip unchanged** | Ally **B** already defending vs **mob X**; victim **A** hit by **mob Y** | **B** still **not** stolen by Tier 1 notify ([AllyDefenseNotifier](src/main/java/villager_self_defense/defense/AllyDefenseNotifier.java) skip rule); dispersal does not pull them in from a different fight. |
| 10 | **Periodic rebalance** | Two threats walk into sphere **without** new damage to the anchor (edge case) | Staggered periodic pass ([DefenseDispersal.tryPeriodicRebalance](src/main/java/villager_self_defense/defense/DefenseDispersal.java)) may reassign over time; not required to be instant. |
| 11 | **Stand-down / unload** | Defender stands down or chunk-unloads | [DefenseDispersal.clearCachesFor](src/main/java/villager_self_defense/defense/DefenseDispersal.java) on unload; caches cleared on stand-down — **no** stale throttle blocking the **next** fight. |
| 12 | **Players as threats** | Valid player + zombie both in sphere; both qualify as threats | Player not creative/Peaceful; dispersal can assign some villagers to player, some to zombie (nearest). |

---

## Regression

- Re-run [phaseBTest.md](phaseBTest.md) (Tier 1 allies, throttle, target conflict).
- Re-run [phaseA2Test.md](phaseA2Test.md) and [phaseATest.md](phaseATest.md) as needed.

---

## Troubleshooting

| Symptom | Check |
|---------|--------|
| Everyone still on one mob | Only one **valid** threat in sphere (`MobCategory.MONSTER` + distance); `dispersalEnabled`; two threats must both be inside **3D** `allyRadius` of **anchor** (victim). |
| No split after many hits | `dispersalRebalanceMinTicks` — wait or force threat set change; confirm **two** monster-category mobs. |
| Villagers ignore a zombie | Not `MONSTER` category (e.g. wrong entity type); inside radius; `mobDefenseEnabled` / defense actually active. |
| Stand-down while ally still pressed | Broad refresh: ensure damage events run `refreshPackThreatNeighbors` for the **victim** in range of allies. |
| Creative player in mix | Creative players are **not** valid threat targets for dispersal. |
