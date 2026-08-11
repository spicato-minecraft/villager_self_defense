# Villager health regen — Manual UAT

Manual checklist for passive villager health regen (AC #1–#13). Automated coverage: `VillagerHealthRegenPolicyTest` (policy math) and `HealthRegenGameTest` (gameplay + persistence). Timing-sensitive cases and external-heal interactions require in-game verification here.

See [Notion task](https://app.notion.com/p/3b916e8d4da381a8ac76ec4971a0b171).

---

## Environment setup

| Requirement | Notes |
|-------------|--------|
| **Branch** | `feat/villager-health-regen` off `1.21.11` |
| **Build** | `./gradlew build` |
| **Run client** | `./gradlew runClient` |
| **Config** | `config/villager_self_defense.json` — keys `healthRegenEnabled` (default `true`), `healthRegenCooldownTicks` (default `24000`, one MC day) |

Heal rate is **not** configurable: **1 HP every 40 ticks (2 seconds)** after cooldown expires.

For faster manual testing, temporarily set `healthRegenCooldownTicks: 200` (~10 seconds).

---

## World / scenario setup

1. Flat or village world, **Easy+** difficulty.
2. **Adult villager** (baby used only for exclusion test).
3. Damage source: zombie melee, `/damage`, or poison potion for DoT cases.
4. Optional: HealthBars mod or F3 debug to observe HP changes.

---

## Test cases (pass / fail)

| # | Case | Steps | Expected |
|---|------|-------|----------|
| 1 | **Cooldown starts on damage** (AC #1) | Damage adult villager to 19/20 HP. Wait several minutes without healing. | No mod-driven HP gain for ~one MC day (24000 ticks). |
| 2 | **Cooldown refresh** (AC #3) | Damage villager, wait partway through cooldown, damage again. | Timer resets; full wait required from last hit. |
| 3 | **No cooldown at full HP** (AC #4) | Hit villager when already at full HP with 0 effective damage (if reproducible). | Cooldown does not start. |
| 4 | **DoT refreshes cooldown** (AC #2) | Apply poison; observe HP ticks. | Each poison tick below max HP refreshes cooldown. |
| 5 | **External heal clears cooldown** (AC #5) | Damage villager, then `/effect give … instant_health` or splash potion to full. | Cooldown cleared immediately; no lingering timer. |
| 6 | **Slow regen after cooldown** (AC #7) | Shorten config cooldown; damage villager; wait for expiry. | +1 HP every 2 seconds until full. |
| 7 | **Defense pauses regen** (AC #8) | Wound villager, wait for cooldown to expire during active defense (zombie fight). | No mod heals while defending. |
| 8 | **Regen after stand-down** (AC #9) | Continue case 7; kill zombie / wait for stand-down. | Mod regen resumes if cooldown already expired and HP below max. |
| 9 | **Cooldown ticks during defense** (AC #9) | Enter defense with long cooldown remaining; wait until cooldown would expire mid-fight. | Still no heal until stand-down; then regen begins. |
| 10 | **Baby excluded** (AC #10) | Damage baby villager; wait past cooldown. | No mod regen on baby. |
| 11 | **Persistence** (AC #11) | Damage villager mid-cooldown; force chunk unload/reload (leave area, return). | Remaining cooldown respected after reload. |
| 12 | **Feature toggle** (AC #12) | Set `healthRegenEnabled: false`; damage villager; wait. | No mod regen; vanilla/external healing still works. |
| 13 | **Vanilla healing during cooldown** (AC #13) | Damage villager; use regeneration potion (not to full). | Potion heals normally; mod does not block external healing. |
| 14 | **Regen during flee** (design note) | Trigger low-health flee; wait for cooldown expiry while fleeing. | Fleeing villager **may** slowly regen (flee ≠ defense). |

---

## Regression

Run existing phase checklists to ensure health regen did not break defense behavior:

- [phaseATest.md](phaseATest.md) — mob defense, stand-down, trading block
- [phaseLowHealthFleeTest.md](phaseLowHealthFleeTest.md) — low-health flee

Automated gate:

```bash
./gradlew test runGameTest
```

---

## Sign-off

| Role | Name | Date | Pass |
|------|------|------|------|
| Manual UAT | | | ☐ |
