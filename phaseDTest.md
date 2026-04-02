# Phase D manual checklist — villager equipment rendering

Tier 2 gear must be working (Phase C) so slots are populated server-side.

## Local

- Equip a villager with helmet, chestplate, leggings, and/or boots via the trade screen gear drawer.
- Confirm armor is visible in third-person and when looking at the villager (not only in UI).
- Equip a main-hand weapon; confirm it still renders in hand (vanilla `HoldingEntityRenderState` path).
- Set `renderVillagerArmor` to `false` in `config/villager_self_defense.json`, reload or restart client; armor should no longer draw (vanilla villager look for armor).

## Multiplayer

- Two clients near the same equipped villager: both should see the same armor and held item without extra mod packets (vanilla equipment sync).

## Config

- Key: `renderVillagerArmor` (default `true`) in [ModConfig](src/main/java/villager_self_defense/config/ModConfig.java) / `villager_self_defense.json`.
