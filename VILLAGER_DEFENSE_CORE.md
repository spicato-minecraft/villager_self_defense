# Villager defense mod — core vision

**Minecraft / stack:** Fabric, targeting **1.21.10** (Loader + mappings; Fabric API optional per feature).

**Purpose:** Villagers can equip gear from the ground and defend themselves and each other, with stricter rules when the aggressor is a player.

---

## Core design principles

### Configurability

The mod should ship with an **exposed config file** (server-side, versioned with the mod) so players and pack makers can tune behavior **within reason**—radii, timers, hit thresholds, performance limits, and feature toggles—without recompiling. Sensible defaults should match the design decisions below; anything we are uncertain about should still be **configurable** so you can iterate in playtesting.

**Within reason:** Options should map to clear gameplay or technical knobs (numbers, booleans, bounded lists). We avoid turning the config into an unsupported scripting surface. If making something configurable exposes mod-breaking behavior, the configuration should come with limits.

### Defense activity priority

When **defense mode** is triggered for an **eligible adult villager**, the mod’s **Defense** `Activity` (exact identifier TBD) is the **highest-priority** behavior for that villager’s **Brain**: it **supersedes** other activities—**work**, **social/meet**, **idle**, **bell-related gathering or alarm movement**, **raid behaviors** that would redirect the villager away from the threat, **sleep** (while defense is active), and other schedule-driven tasks—so defenders **do not** abandon the fight to path to a bell, workstation, bed, or meeting point until **defense mode ends** per the stand-down rules.

**Babies** are excluded: they **flee** instead of entering combat Defense activity; their movement may still use vanilla panic/flee-style tasks (see existing baby rules).

**Implementation sketch (not prescriptive):** wire Defense into the villager **Brain** so that when defense state is active, the Defense **task list** runs in preference to conflicting vanilla activities (priority ordering / activity replacement is version-specific in Yarn mappings).

---

## Intended behavior (north star)

1. **Equipment:** Villagers can **pick up** eligible weapons and armor from the world and **hold / wear / use** them in combat (details TBD per tier).
2. **Mob aggression:** If a villager is attacked by a **non-player** entity, that villager **and** other villagers within a **configurable radius** treat the attacker as a threat and **fight back**. With **one** active threat, the group typically **shares that target**. With **multiple** simultaneous threats, a **dispersal** layer assigns defenders across attackers (see below).
3. **Player aggression:** If a villager is **attacked repeatedly in quick succession** by a **player**, defense activates per Tier **0b**; at Tier **1+**, the same **group response** applies (radius + targeting; dispersal when multiple threats qualify).

---

## Delivery progression (summary)

Order is **activation first** (who can turn on Defense), then **scale out** (allies, dispersal), then **gear**.

| Tier | Scope |
|------|--------|
| **0** | One villager retaliates vs **non-player** attacker — minimal AI path, no gear, no allies. |
| **0b** | **Player-activated defense:** repeated player hits within the configured window **activate the same Defense activity** as Tier 0 (hit counting + gating only; still one villager vs player until Tier 1). |
| **1** | Allies within radius join the same threat (single shared target at first); applies whether defense was triggered by a **mob** (Tier 0) or **player** (Tier 0b). |
| **1b** | **Dispersal:** multiple attackers → divide defenders by **proximity** (or nearest-threat assignment), subject to performance guardrails. |
| **2** | Pickup + equip + use gear (can split armor vs weapon). |

Later documents may rename tiers; **0b** is intentionally early so **defense activation** is unified (mobs + players) before group behavior.

---

## Design decisions (locked for v1 unless config overrides)

**Player activation (Tier 0b; “player escalation”)**

- **Not in defense mode:** **3** hits by the **same player** within **10 seconds** against **any loaded villager** (village-wide intent for “same aggressor” tracking—implementation may refine scope).
- **Already in defense mode:** **6** hits by the same player within **10 seconds** before further escalation (threshold while the pack is already riled).

**Creative, invulnerability, indirect damage**

- **Creative-mode players / peaceful mode players** are **ignored** for retaliation (defense mode is not triggered by creative players/ targeting from villager hits).
- **Indirect damage** (fire, explosions, etc.) **does not** count toward hit-based escalation **for now**; direct melee/projectile hits from the player do.

**Friendly fire and targets**

- Villagers **do not** target other villagers, **iron golems**, or **passive mob mounts** (defensive behavior only—no broad PvP against neutrals).
- If the attacker is riding a mob, behavior stays **defensive**; specifics (target rider vs mount) can follow vanilla targeting norms unless config adds a mode.

**Babies, trading UI**

- **Baby villagers** do **not** fight; they should **flee** (run away) instead of joining combat.
- If a villager **enters defense mode**, they **close any open trading UI** with them; trading **cannot** be opened again until they **exit defense mode**.

**Defense mode exit (“forget target” / stand down)**

- The pack **exits defense mode** when:
  - the **offending target is killed**, or
  - **no villager in the relevant area** is attacked or damaged by a hostile player/entity for **30 seconds** (local “quiet” window—**configurable**).

**Radius**

- Ally notification uses a **3D** radius (horizontal + vertical), not a cylinder-only horizontal disk.

**Multi-attacker dispersal**

When **more than one** valid hostile attacker is present in the defense bubble, villagers should **not** all be forced onto a single mob if that leaves other threats unaddressed. A **dispersal** pass assigns defending villagers to attackers—typically **nearest eligible threat** per villager (greedy), or an equivalent **proximity-based** split—so pressure spreads across multiple attackers.

**Performance guardrails (dispersal + assignment)**

These rules keep cost predictable next to vanilla pathfinding and entity ticking:

- **No full reassignment every tick** for every villager against every hostile. Assignment runs on **events** (e.g. new damage source, threat enters/leaves radius, defense mode start) and/or on a **configurable minimum interval** between global rebalance passes (e.g. every **10–20 ticks** as a default band; exact value configurable).
- **Sticky targets:** once assigned, a villager **keeps** that attacker until it dies, becomes invalid, **leaves** the relevant range, or a **rebalance** timer/interval fires—reduces pathfinding thrash and CPU churn.
- **Cap attackers considered** for dispersal (e.g. **K** nearest or **K** “recent damagers”) so **A** stays small even if many mobs exist in loaded chunks.
- **Reuse group caps:** respect the same **max allies notified per event** and **dedupe/throttle** rules as the rest of the mod; dispersal should not multiply scans without bounds.
- **Optional:** **max defenders per attacker** (soft cap) to avoid fifty villagers pathing to one zombie when others are free—configurable for pack tuning.

**Modded gear**

- **All** weapons and armor consistent with broad tags / compatibility—aim for **modpack-friendly** behavior, not a tiny vanilla-only allowlist.

**Multiplayer**

- **Server-authoritative** logic; client only what vanilla needs for poses, held items, and UI.

**Brain priority (summary)**

- **Defense activity = top priority** when triggered (see **Defense activity priority** under core principles). No other villager activity should preempt combat defense for adult villagers until stand-down.

---

## Configuration surface (high level)

The following should be **user-tunable** in the exposed config (exact names TBD):

| Area | Examples |
|------|-----------|
| **Group response** | 3D ally radius; whether vertical distance uses full sphere or capped Y |
| **Dispersal** | Enable/disable; max attackers **K** considered; rebalance interval (ticks); optional max defenders per attacker; sticky vs eager rebalance |
| **Player activation (Tier 0b)** | Hit counts (normal vs already-in-defense), time window (seconds) |
| **Defense mode** | Stand-down timeout (seconds); conditions for exit |
| **Performance** | Max allies notified per event, minimum tick interval between group scans (throttle) |
| **Features** | Toggles for gear pickup, player activation (Tier 0b) window/thresholds (creative and Peaceful difficulty are not configurable; see design decisions) |
| **Balance** | Optional defaults for suggested radius/throttle (see below) |

---

## Recommended defaults (suggestions; all configurable)

**3D ally radius:** **16 blocks** is a reasonable starting default—close to a tight neighborhood in vanilla villages, easy to feel in-game, and cheap to query. Pack makers can raise to **24–32** for “whole village” feels. Expose as a single `double` (or three axes if we ever split X/Y/Z).

**Scan / notify throttle:** A practical default is **cap allies at 32 per event** and **do not re-broadcast the same target to the same villager more than once per second** (dedupe). Optionally **run group logic at most every 5 ticks** for non-critical paths if profiling shows hotspots. Expose caps and tick intervals in config for your tuning.

**Dispersal defaults (suggestions):** **Enable** dispersal when multiple threats qualify (toggle off → fall back to single shared target for debugging). **K = 8** max attackers considered for assignment (enough for messy raids; raise/lower in config). **Rebalance interval:** **10–20 ticks** between full dispersal passes unless an **event** forces an earlier update. Pair with **sticky assignment** as the default behavior between passes.

---

## Follow-ups for later docs / implementation

- **Invulnerable players:** confirm default (ignore vs count hits).
- **“Any loaded villager” vs per-chunk:** precise definition for the 3-hits/10s rule in multiplayer.
- **Flee AI for babies:** specific task or reuse vanilla panic—implementation detail.
- **Dispersal algorithm:** confirm greedy nearest-threat vs tie-breaking (distance only, or slight load balancing when two villagers equidistant).

---

## Planned follow-up — defense-mode movement speed

*(Not part of Phases A–B; implement after Tier 1 group response is in place.)*

While **defense mode** is active, **adult villagers** should move at a **fixed** effective speed that is **slightly slower than vanilla base player sprint speed** (no Swiftness, no Soul Speed—using the game’s default player sprint as the reference). This is a **static constant** in code—not a config option—so players can **reliably outrun** defenders by sprinting. Apply when entering defense; remove on stand-down.

---

## Non-goals

- Changing villager trades or profession logic.
- Replacing iron golems as primary defense.
- Full “guard” profession (possible future extension).
