# Phase C — Testing (Tier 2 gear: armor + weapons)

Manual checklist for Tier 2: gear assigned via the villager **trade screen** gear drawer applies to **armor** and **main-hand weapon** for damage and protection. See [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md), [.cursor/plans/phase_c_gear_loop_bd9ce17e.plan.md](.cursor/plans/phase_c_gear_loop_bd9ce17e.plan.md), and [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java).

**Minecraft 1.21.x:** attribute ids use the short registry names (e.g. `minecraft:attack_damage`), not `minecraft:generic.*`.

---

## Environment setup

- Same base as [phaseATest.md](phaseATest.md) / [phaseBTest.md](phaseBTest.md): JDK 21, Fabric, `./gradlew runClient` (or dedicated server).
- **Cheats enabled** (singleplayer: Open to LAN → Allow Cheats, or creative test world).
- **`gearMenuEnabled`:** `true` in `run/config/villager_self_defense.json` (default).

---

## Selector shortcuts

Use **nearest** villager (stand next to the one you equipped):

```mcfunction
@e[type=villager,sort=nearest,limit=1]
```

If the villager has a **custom name** (name tag), you can use:

```mcfunction
@e[type=villager,name=YourName,limit=1]
```

---

## Commands — attributes (final value including gear)

**Armor** (points from worn armor):

```mcfunction
/attribute @e[type=villager,sort=nearest,limit=1] minecraft:armor get
```

**Armor toughness:**

```mcfunction
/attribute @e[type=villager,sort=nearest,limit=1] minecraft:armor_toughness get
```

**Attack damage** (base + weapon item modifiers; should rise with a sword, drop toward **1.0** if main hand is empty and the mod’s +1 base is the only contribution):

```mcfunction
/attribute @e[type=villager,sort=nearest,limit=1] minecraft:attack_damage get
```

**Optional — compare base vs modified** (if your client lists subcommands; tab-complete after the attribute name):

```mcfunction
/attribute @e[type=villager,sort=nearest,limit=1] minecraft:attack_damage base get
```

---

## Commands — NBT / equipment (what is actually equipped)

Full equipment blob (can be long in chat):

```mcfunction
/data get entity @e[type=villager,sort=nearest,limit=1] equipment
```

**Per-slot** checks (1.21 uses the `equipment` component):

```mcfunction
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.head
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.chest
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.legs
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.feet
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.mainhand
/data get entity @e[type=villager,sort=nearest,limit=1] equipment.offhand
```

If a path errors, run `/data get entity @e[type=villager,sort=nearest,limit=1]` **without** a path and find the `equipment` section in the output.

---

## Procedure — quick pass/fail

1. **Open trade** with an adult villager → open the **gear drawer** (hammer icon) → place **full leather** (or one piece) and note **expected armor** from vanilla.
2. Run **`minecraft:armor get`** — value should be **> 0** with armor on; **`equipment.chest`** (etc.) should show the items.
3. Place an **iron sword** (or other allowed weapon) in the **weapon** slot → run **`minecraft:attack_damage get`** — should be **clearly above 1.0** (mod adds **+1** base; unarmed effective read is typically **1.0** without weapon bonuses).
4. Run **`equipment.mainhand`** — should show the **same** sword id/count as assigned.
5. **Remove** the sword from the gear slot only → **`attack_damage`** should fall back toward **1.0**; **armor** unchanged if you left armor in place.
6. **Combat spot-check (optional):** spawn a zombie, let the villager fight with sword vs without — time-to-kill or hearts on the zombie should differ. For **armor**, compare damage taken with vs without diamond chestplate (same zombie, same difficulty).

---

## Pass / fail table

| # | Check | Pass |
|---|--------|------|
| 1 | Armor pieces in drawer → `minecraft:armor get` increases vs naked villager | Armor attribute matches expectation |
| 2 | `equipment.head` / `chest` / `legs` / `feet` show the items you placed | NBT matches UI |
| 3 | Weapon in drawer → `minecraft:attack_damage get` > **1.0** (typical with iron sword) | Damage attribute reflects weapon |
| 4 | `equipment.mainhand` shows the weapon | Server-side main hand matches stash |
| 5 | Remove weapon from drawer → attack damage returns to **~1.0** | Weapon bonus removed |
| 6 | Save & quit, reload world → re-run **4** and **attribute** checks | Gear persists (no silent stash encode failures in `latest.log`) |

---

## Troubleshooting

- **`Can't find element` for an attribute:** Use **`minecraft:attack_damage`**, **`minecraft:armor`**, not `minecraft:generic.*`.
- **Attack damage stuck at 1.0 but you see a sword:** Check **`equipment.mainhand`** — if empty, main hand is not applied server-side; ensure **`gearMenuEnabled`** and retry after re-opening trade once.
- **Armor shows in UI but `equipment` is wrong:** Confirm you targeted the same villager (**nearest** vs another villager in the village).
