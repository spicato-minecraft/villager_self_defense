# Villager defense mod — implementation plan

High-level breakdown for splitting work. Details stay in code and smaller task notes as the mod is scaffolded.

---

## Principles

- Ship **vertical slices** (each tier is playable).
- Prefer **server-authoritative** logic; client only what Minecraft needs for visuals.
- **Config-first:** ship an **exposed config file** early; wire new behavior to keys as you go (see [VILLAGER_DEFENSE_CORE.md](./VILLAGER_DEFENSE_CORE.md) — configurability principle and suggested defaults).
- **Defense `Activity` priority:** when defense is active, the mod’s Defense activity **outranks** vanilla activities (work, bell, sleep, raid redirect, etc.); implement Brain/schedule integration accordingly (see core doc).

---

## Phase map (aligned with core tiers)

### Phase A — Tier 0: Retaliation spine (mobs)

**Goal:** Demonstrate end-to-end “villager fights back” for **non-player** attackers.

**Likely includes:** brain / memory / task wiring, **Defense activity** registered as **top priority** when defense triggers, damage or hurt listener, set attack target, basic melee (or reuse vanilla patterns). No pickup, no allies.

**Exit:** Manual test: spawn mob, hit villager, villager engages attacker; **bell / workstation / bed** do not steal pathing while defense is active.

---

### Phase A2 — Tier 0b: Player-activated defense

**Goal:** **Same Defense activity** as Tier 0, but **activation** requires repeated player hits within the configured window (hit counting, creative/peaceful ignore, indirect damage rules per core doc).

**Likely includes:** damage listener filtering `Player` sources, rolling hit window, thresholds (3 / 6 hits), wiring into existing defense state—**no** new allies yet (still 1:1 villager vs player until Tier 1).

**Exit:** Single accidental hit does not trigger defense; sustained poking triggers defense; behavior matches Tier 0 once active.

---

### Phase B — Tier 1: Group response

**Goal:** Villagers within **R** blocks of the victim also acquire the same attacker target—whether defense was triggered by a **mob** (Tier 0) or **player** (Tier 0b).

**Likely includes:** radius query, filters (alive, adult, same dimension), optional cooldowns to avoid thrash.

**Exit:** Multiple villagers pile onto one mob **or** one player per design; no gear yet.

---

### Phase B2 — Tier 1b: Multi-attacker dispersal

**Goal:** When **multiple** valid hostiles are in play, **divide defenders** by **proximity / nearest-threat** assignment instead of everyone stacking one target—without per-tick O(V×A) reassignment.

**Guardrails (must align with core doc):** event-driven + **interval-capped** rebalance; **sticky** targets between passes; cap **K** attackers; optional max defenders per threat; reuse ally caps and dedupe.

**Likely includes:** threat set maintenance, greedy or incremental assignment, config keys for K / interval / toggles.

**Exit:** Two+ zombies at different spots → villagers split sensibly; profiling shows no runaway tick cost with many entities loaded.

---

### Phase C — Tier 2: Gear loop

**Goal:** Pick up weapons/armor from ground; equip; attacks use gear where applicable.

**Sub-phases (optional split):** C1 collect + equip only; C2 combat damage uses item rules; C3 polish (drop on death, durability, edge cases).

**Exit:** Dropped sword/armor on floor → villager equips → visible combat benefit.

---

### Phase E — Hardening (ongoing / after C)

Balance, edge cases (beds, raids, golems), performance passes, documentation for players.

---

### Planned follow-up — defense-mode movement speed

*(Does not modify Phases A–B; schedule after Tier 1 group work.)*

**Goal:** Match [VILLAGER_DEFENSE_CORE.md](./VILLAGER_DEFENSE_CORE.md): while **defense mode** is active, **adult villagers** move at a **fixed** speed **just below** vanilla **base player sprint**; **static** code constant only (no config key).

**Likely includes:** server-side `generic.movement_speed` modifier (or equivalent) applied on defense activate and removed on `standDown` (consistent with existing unload cleanup); playtest sprint escape on flat ground.

**Exit:** A sprinting player outruns defending villagers; no leftover modifier after stand-down.

---

## Dependency order

```
A → A2 → B → B2 → C → E
     (A2 = player activation; B/B2 = allies + dispersal; C = gear)
```

Suggested default: **A → A2 → B → B2 → C** so **activation** (mobs + players) is complete before **group** behavior, then **gear**. **B2** can be deferred only if you temporarily treat multi-threat as “shared first target” (document that shortcut).

---

## Work package checklist (tick as you go)

Use this as a lightweight backlog; keep individual PRs small.

- [ ] **A1** Project scaffold (1.21.10 Fabric, mod id, mixins entry) + minimal **config file** stub.
- [ ] **A2** Tier 0: threat detection + target assignment (non-player).
- [ ] **A3** Tier 0b: player hit tracking + activation thresholds (same Defense pipeline as A2).
- [ ] **A4** Config file + defaults (flesh out keys for A2/A3: hits, windows, toggles).
- [ ] **B1** Ally notification within radius (Tier 1).
- [ ] **B2** Dispersal layer: multi-attacker assignment + performance guardrails (Tier 1b).
- [ ] **C1** Item pickup + registration (Fabric API or mixin as needed).
- [ ] **C2** Equip slots + attack using held item.
- [ ] **E1** Playtesting + tuning notes.

---

## Testing themes (not full cases)

- Single villager vs zombie; pack vs zombie; **Tier 0b:** player poke vs player spam (forgiveness vs activation).
- **Multiple attackers** at different positions → dispersal splits defenders; verify no tick spikes with throttles on.
- **Defense priority:** ring bell / open typical distractions → villager in defense should **not** abandon combat for those behaviors.
- Server reload / chunk unload mid-fight.
- Armor stand / item frame interactions (should not equip junk).

---

## References

- Behavior and open questions: [VILLAGER_DEFENSE_CORE.md](./VILLAGER_DEFENSE_CORE.md)
