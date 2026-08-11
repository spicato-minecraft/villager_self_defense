# Low-health flee — Manual UAT

Manual checklist for the low-health flee feature (AC #1–#7). Automated coverage: `LowHealthFleeGameTest` (AC #1–#3, #6–#7) and unit tests for `LowHealthFleePolicy` / config migration. AC #4 and #5 require in-game verification here.

See [Notion task](https://app.notion.com/p/3b816e8d4da381c89820e0363a9ea75d).

---

## Environment setup

| Requirement | Notes |
|-------------|--------|
| **Branch** | `feat/low-health-flee` off `1.21.11` |
| **Build** | `./gradlew build` |
| **Run client** | `./gradlew runClient` |
| **Config** | `config/villager_self_defense.json` — keys `lowHealthFleeEnabled` (default `true`), `lowHealthFleeThreshold` (default `0.333…`, i.e. ≤⅓ max HP) |

Threshold math: `health <= floor(maxHealth × threshold)` — e.g. 20 max HP → flee at **6 HP or below**.

---

## World / scenario setup

1. Flat or village world, **Easy+** difficulty.
2. **Adult villager** with gear optional (iron sword helps prolong fights).
3. **Hostile mob** (zombie) to trigger mob defense (Tier 0).
4. For pack tests: second adult villager within **16 blocks** (`allyRadius`).

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **Wounded defender flees** (AC #1, #2) | Trigger defense (zombie hits adult villager). Let HP drop to ≤6 (or threshold). | Villager **stops attacking**, runs away (panic/flee). Gear main hand cleared; no longer in defense pose. |
| 2 | **Low HP never enters defense** (AC #3) | Damage villager to ≤6 HP **before** defense activates. Then have zombie hit again. | Villager **does not** enter mod defense; uses vanilla flee/panic only. |
| 2b | **Low HP distress call** | Adult villager at ≤6 HP + second healthy villager within ~16 blocks. Zombie hits wounded villager. | Wounded villager flees/does not defend; **nearby ally enters defense** vs the attacker. |
| 3 | **Flee ends on threat gone** (AC #4) | Trigger flee (case 1). Kill zombie or move it far away. **Do not heal** villager. | Flee ends when threat is gone/out of range — **not** because HP recovered. |
| 4 | **Flee persists despite healing** (AC #4) | Trigger flee. Use `/effect give … regeneration` or golden apple while zombie still nearby. | Villager **keeps fleeing** until threat gone, even if HP rises above threshold. |
| 5 | **Re-entry after flee** (AC #5) | Trigger flee, let threat remain nearby but out of panic range (or wait for panic to end). Heal villager above threshold (e.g. `/effect` or wait for regen). | After panic ends, if HP > threshold and zombie still valid threat, villager **re-enters defense** and attacks again. |
| 6 | **Baby unchanged** (AC #6) | Repeat case 1 with **baby** villager. | Baby does not enter mod defense; vanilla flee only. |
| 7 | **Config toggle** (AC #7) | Set `lowHealthFleeEnabled: false` in config, restart/reload. Repeat case 1. | Villager **keeps fighting** at low HP (no flee override). |
| 8 | **Config threshold** (AC #7) | Set `lowHealthFleeThreshold: 0.5` (50% HP). Repeat case 1. | Flee triggers at ≤10 HP on 20 max (not ≤6). |
| 9 | **Pack: fleeing ally excluded** | Two adult villagers near each other; one enters flee while the other defends. | Fleeing villager is **not** pulled back into defense by ally notifier or dispersal. |

---

## Regression

Run existing phase checklists to ensure low-health flee did not break Tier 0/1 behavior:

- [phaseATest.md](phaseATest.md) — mob defense, stand-down, trading block
- [phaseBTest.md](phaseBTest.md) — ally recruitment (if applicable)

Automated gate:

```bash
./gradlew test runGameTest
```

---

## Sign-off

| Role | Name | Date | Pass |
|------|------|------|------|
| Manual UAT | | | ☐ |
