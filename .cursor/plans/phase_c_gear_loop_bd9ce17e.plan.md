---
name: Phase C Gear Loop
overview: "Implement Tier 2 (Phase C) from [VILLAGER_DEFENSE_IMPLEMENTATION.md](VILLAGER_DEFENSE_IMPLEMENTATION.md): player assigns gear to defending villagers via a drawer UI on the villager trade screen (helmet, chestplate, leggings, boots, main-hand weapon), with melee damage reflecting the held item and optional C3 polish (durability, death drops). Builds on existing defense brain ([ModDefenseActivities](src/main/java/villager_self_defense/brain/ModDefenseActivities.java), [DefenseBrainHooks](src/main/java/villager_self_defense/brain/DefenseBrainHooks.java)) and config ([ModConfig](src/main/java/villager_self_defense/config/ModConfig.java)). Server-authoritative equip via `ScreenHandler` / slot sync."
todos:
  - id: c-config
    content: Add Tier 2 config keys (gear feature toggle, optional UI offsets for gear icon) + migration defaults in ModConfig
    status: in_progress
  - id: c1-menu-equip
    content: Merchant/trade screen — gear icon (top-right) toggles drawer; ScreenHandler with 5 armor + weapon slots; server validates tags and applies equipment
    status: pending
  - id: c1-client-ui
    content: Client-only drawer layout, icon button, slot rendering; optional REI/EMI exclusion registration if overlays clash
    status: pending
  - id: c2-damage
    content: Wire melee damage to held item rules (verify doHurtTarget path; mixin or attribute sync as needed)
    status: pending
  - id: c3-polish
    content: Death drops, durability/hurtAndBreak, edge cases (empty hand, unload cleanup for modifiers)
    status: pending
  - id: c-test
    content: Manual test pass per implementation doc exit criteria + multiplayer sync + modpack UI spot-check
    status: pending
isProject: false
---

# Phase C — Tier 2: Gear via merchant drawer

## Goal (from implementation doc)

**Exit criterion:** Player places sword/armor into the villager gear UI → villager equips → **visible** combat benefit (higher damage and/or armor mitigation).

**Sub-phases:** C1 menu + equip → C2 item-based combat → C3 polish.

**Design anchors:** [VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md) — modpack-friendly **tags** (not a tiny vanilla-only list), **config toggle** for gear features, **server-authoritative** logic. Acquisition is **player-driven** (no autonomous floor vacuum in the default design); optional future “pick up dropped gear” can layer on the same equip + C2 stack.

---

## UX — Drawer + gear icon

- **Host screen:** Vanilla **villager trade** (`MerchantScreen` / `MerchantMenu`). Extend with a **client-only** overlay: a **gear icon** anchored to the **top-right** of the trade screen (inside the GUI area, not the full window—see positioning below).
- **Interaction:** Clicking the icon **toggles** a **drawer** panel (slide or expand—implementation detail) that reveals equipment slots:
  - **Helmet, chestplate, leggings, boots** (`EquipmentSlot` armor group)
  - **One main-hand / weapon slot** (maps to villager main hand; mirrors “weapon” in the plan)
- **Collapsed by default** (or remember last state via client prefs—optional): drawer hidden until the player opens it via the icon.
- **Server:** Either extend `MerchantMenu` with **synchronized custom slots** (Fabric networking / `ExtendedScreenHandlerType` pattern) or a **dedicated `ScreenHandler`** opened alongside—pick one approach and keep **all equip mutations server-side** with tag validation (same tag policy as the old pickup sketch).

---

## C1 — Menu + equip (replaces floor pickup as primary path)

**Policy:**

- **When:** Player has the villager trade UI open; server applies changes only for **valid** slot operations (no creative client stuffing).
- **What is eligible:** Items matching **tags** (e.g. Fabric convention `#c:swords` / `#c:tools/melee` + armor tags, vanilla fallbacks). Document chosen tags in code comments for pack makers.
- **No floor scan** in the default Tier 2 flow—simpler than radius/throttle pickup. Revisit as an optional Phase C′ if design calls for autonomous looting.

**Implementation sketch:**

1. Add config: `gearMenuEnabled` (or fold into existing Tier 2 toggle), plus **UI**: optional `**gearIconOffsetX` / `gearIconOffsetY`** (or single `gearIconPaddingFromRight`) so players can nudge the icon when another mod occupies the same corner.
2. **ScreenHandler** with ghost/synced slots mirroring villager equipment for the five slots above; on `slotClick` / `quickMove`, server copies into `LivingEntity` equipment APIs (and takes items from player inventory as usual).
3. **Opening path:** Hook **trade screen** init (client) to add the icon + drawer; keep **merchant** behavior aligned with existing [DefenseManager](src/main/java/villager_self_defense/defense/DefenseManager.java) / [MerchantMenuAccessor](src/main/java/villager_self_defense/mixin/MerchantMenuAccessor.java) patterns where trades are blocked while defending—gear slots should still follow the same “which villager” identity checks.
4. **Sync:** Equipment changes on server propagate via vanilla entity data; verify third-person held item and armor layers in playtest.

---

## C2 — Combat uses item rules

(Unchanged from prior plan.)

**Objective:** Unarmed villager ≈ current 1.0 punch; with iron sword, damage should **clearly** reflect the item.

**Approach (verify against your Mojang mappings in-tree):**

1. Trace `Mob#doHurtTarget` (or `LivingEntity` equivalent) for your loader version.
2. If vanilla already applies held-item damage once a sword is in main hand, C2 may be attribute sync on equip/unequip — or minimal mixin if villagers are excluded.
3. If not, narrow mixin for defending villagers delegating to the same damage path as other mobs/players for the stack.
4. **Durability:** `hurtAndBreak` on successful hit if needed; C2 or C3.

---

## C3 — Polish

- **Death drops:** On villager death, drop equipped gear so gear isn’t voided.
- **Durability / unbreaking:** Weapon `hurtAndBreak`; optional armor damage parity.
- **Stand-down / unload:** On `ENTITY_UNLOAD` and defense clear, clear stale **attack damage** modifiers if used in C2.
- **Edge cases:** Empty hand after break; deny invalid items via tags + optional `villager_self_defense:gear_blacklist` datapack tag.

---

## Flow (high level)

```mermaid
flowchart LR
  tradeOpen[Player opens villager trade]
  drawer[Gear icon opens drawer]
  slots[ScreenHandler slots server]
  equip[Apply equipment on server]
  melee[MeleeAttack / doHurtTarget]
  tradeOpen --> drawer
  drawer --> slots
  slots --> equip
  equip --> melee
```



---

## Other mod compatibility — gear icon & overlays

There is **no single standard library** that reserves “the top-right corner” across all mods. Each mod typically places widgets with fixed offsets. Mitigations that work well in practice:


| Approach                                                                           | Role                                                                                                                                                                                                                                                                                                                        |
| ---------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Fabric Screen API** (`fabric-screen-api-v1`: `ScreenEvents.afterInit` / related) | **Standard** hook to add child widgets after the trade screen initializes; use **screen width/height** and the **trade panel bounds** (from `MerchantScreen` layout) to position the gear icon relative to the **panel**, not blind hardcoded screen coordinates.                                                           |
| **Configurable offsets**                                                           | Expose small **X/Y nudges** (or padding from right/top) in [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java) so pack users can avoid collisions with e.g. **inventory-sort**, **mouse tweaks**, or other trade-screen add-ons.                                                                         |
| **REI / EMI**                                                                      | If the drawer or icon sits where **Roughly Enough Items** or **EMI** draws recipe/overlay zones, register an **exclusion area** for the merchant screen (REI: `registerBoundsHandler` / screen exclusion API; EMI: equivalent **exclusion zone** registration) so recipe ghosts don’t steal clicks over your icon or slots. |
| **LibGui** (CottonMC)                                                              | Useful for **layout and widgets** on a **fully custom** screen; it does **not** by itself coordinate with other mods’ buttons. Still pair with **relative positioning + config offsets** if you build a custom screen instead of only overlaying vanilla.                                                                   |
| **Mod Menu**                                                                       | Applies to **pause/title** menus; **not** the usual solution for **in-world trade** overlays. No dependency required for the villager gear icon.                                                                                                                                                                            |


**Recommendation:** Implement with **Fabric Screen API** + **anchor to merchant panel** + **config offsets**; add **REI/EMI exclusions** if you ship to modpacks that use those on the trade screen. Document the offset keys in the example config comment.

---

## Files likely touched

- New: `.../gear/` or `.../client/gear/` — `ScreenHandler`, server slot logic, tag validation; client screen/drawer + icon.
- Client init: register screen, `ScreenEvents` listeners for `MerchantScreen` (exact class name per mappings).
- [DefenseManager](src/main/java/villager_self_defense/defense/DefenseManager.java) — only if you need to tie gear UI to defending state / same villager identity as existing merchant close logic.
- [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java) + optional [run/config/villager_self_defense.json](run/config/villager_self_defense.json).
- [VillagerMixin](src/main/java/villager_self_defense/mixin/VillagerMixin.java) — only if C2 requires changes beyond current mixins.
- [fabric.mod.json](src/main/resources/fabric.mod.json) — new entrypoints / optional REI/EMI soft deps (`suggests` / `breaks` as appropriate).

---

## Testing

- Open trade with villager → gear icon visible and toggleable → place iron sword + leather → villager model updates → faster kills / less damage taken vs unarmed baseline.
- Multiplayer: second player sees equipped villager; gear changes sync.
- With **REI/EMI** installed: icon and drawer remain clickable; adjust exclusion if needed.
- Nudge **config offsets** and confirm icon moves without breaking layout.

---

## Out of scope for Phase C

- **Autonomous floor pickup** of `ItemEntity` — optional follow-up; same tags + equip helpers can be reused.
- **Defense-mode movement speed** follow-up ([VILLAGER_DEFENSE_CORE.md](VILLAGER_DEFENSE_CORE.md)).
- **Phase E** hardening — defer unless a gear bug blocks exit criteria.

