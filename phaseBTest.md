# Phase B — Testing (Tier 1 group response / allies in radius)

Manual checklist for Tier 1: nearby adult villagers join the same attacker target after the **victim** activates defense (mob or Tier 0b player), pack-aligned stand-down refresh, ally notify caps/throttle, and 6-hit escalation re-notify. See [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md), [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md), [phaseATest.md](phaseATest.md) (Tier 0), and [phaseA2Test.md](phaseA2Test.md) (Tier 0b).

---

## Environment setup

Same base setup as [phaseATest.md](phaseATest.md) (JDK 21, Fabric 1.21.10, `./gradlew runClient` / `runServer`).

**`config/villager_self_defense.json`** includes Tier 1 keys (defaults applied if missing; see [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java)):

| Key | Default | Role |
|-----|---------|------|
| `groupDefenseEnabled` | `true` | Master toggle for ally recruitment |
| `allyRadius` | `16.0` | 3D sphere (blocks) for ally notify and pack threat refresh |
| `maxAlliesNotifiedPerEvent` | `32` | Max additional villagers processed per notify pass (nearest first) |
| `allyNotifyMinTicks` | `5` | Min ticks between repeat ally broadcasts for the same victim–attacker pair |

Tier 0b keys (`playerHitsToActivate`, `playerHitWindowSeconds`, etc.) and `standDownQuietSeconds` still apply. Creative / Peaceful rules for **players** are unchanged (CORE; not configurable).

---

## World / scenario setup

1. **Easy+** Survival for player and mob tests (not Peaceful for Tier 0b player activation).
2. **Two or more adult villagers** within **≤ `allyRadius`** blocks of each other (3D distance), same chunk/area so they load together.
3. **Baby villager** (optional): place near the group to confirm babies are **not** recruited as allies.
4. For **mob** tests: a zombie or other hostile that can melee the **primary** victim.
5. For **player** tests: Survival, direct melee (or qualifying projectiles per [PlayerQualifyingHits](src/main/java/villager_self_defense/defense/PlayerQualifyingHits.java)).

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **Mob — allies join** | Adult villagers A and B within 16 blocks (3D) of each other; zombie hits **A** only | **B** also enters defense vs that zombie (paths/attacks); baby nearby does **not** join. |
| 2 | **Player — allies after activation** | A and B within radius; land **3** qualifying hits on **A** within the window | **A** activates vs player; **B** joins vs the same player without needing their own 3 hits. |
| 3 | **Ally out of radius** | Place **C** **> `allyRadius`** from **A** (victim); trigger defense on **A** | **C** does **not** join from that notify (may still activate later if damaged directly). |
| 4 | **`groupDefenseEnabled` off** | Set `groupDefenseEnabled` to `false`, reload config if applicable; mob hits **A** | Only **A** defends; no ally pile-on from Tier 1. |
| 5 | **Pack stand-down refresh** | Two defenders vs same threat; only **one** villager keeps taking hits; the other is never damaged | After **> `standDownQuietSeconds`** with **ongoing** hits on the engaged villager, the **untouched** ally should **not** stand down early solely because they never refreshed locally — pack refresh near the victim keeps shared quiet timers aligned. |
| 6 | **6-hit escalation re-notify** | Already defending vs player; land **6** qualifying hits in window (see [phaseA2Test.md](phaseA2Test.md)) | No crash; ally notify may run again (throttled); useful if allies were unloaded/out of range at first activation. |
| 7 | **Throttle / cap sanity** | Many villagers in a tight cluster; trigger notify | At most **`maxAlliesNotifiedPerEvent`** new allies per pass; repeated notifies respect **`allyNotifyMinTicks`** (no obvious TPS stutter from ally logic alone). |
| 8 | **Target conflict (Phase B)** | Ally **B** already defending vs **mob X**; **A** is hit by **mob Y** in range | **B** does **not** switch target to **Y** in Tier 1 (skip if different `targetUuid`). |
| 9 | **Unload** | Trigger defense with allies, then unload a defending villager (chunk unload / kill entity) | No crash; state removal on unload remains sane (see `DefenseManager.removeState`). |
| 10 | **Same-target refill after ally stand-down** | Two attackers (A1, A2) and victims (V1, V2) with shared locals in radius: A1 hits V1 (allies activate vs A1); A2 hits V2 (V2 targets A2); **V1 dies** (ex-allies stand down); **A2 damages V2 again** while V2 still defends vs A2 | Nearby adults who stood down should **re-enter** defense vs A2 after a throttled recruit pass (same-target damage re-notify); not only on V2’s first hit vs A2. |

---

## Regression

- Re-run [phaseATest.md](phaseATest.md) (mob Tier 0, baby, trading, distractions, stand-down).
- Re-run [phaseA2Test.md](phaseA2Test.md) (3-hit window, creative/peaceful, indirect damage). **Update expectation for case 8:** the 6-hit path now invokes Tier 1 **ally re-notification** (throttled), not a no-op stub.

---

## Troubleshooting

| Symptom | Check |
|---------|--------|
| No allies join | `groupDefenseEnabled` true; allies are **adults** within **3D** `allyRadius` of the **victim**; victim actually activated defense (mob hit or 3 player hits). |
| Allies join from too far | Verify **3D** distance (vertical separation counts); adjust `allyRadius` for testing. |
| Everyone stands down while one still fights | Report repro; confirm pack refresh runs on damage events involving the shared attacker. |
| Creative/peaceful ally pull | Allies should not be recruited onto creative/peaceful **players**; victim path should not activate vs them anyway. |
