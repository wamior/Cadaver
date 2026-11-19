# Store listing fields

Long description: `docs/description_en.md` (English) and `docs/description_ru.md` (Russian). Both sites accept Markdown; paste the English one as the main description (CurseForge: switch the editor to Markdown). Modrinth/CurseForge have no separate language field, so either append the Russian text below the English one or keep it for your own channels.

## Shared

- **Name:** Cadaver
- **Logo / icon:** `icon.png` (512x512)
- **Summary** (one line, English):
  > Leaves a corpse with all your items when you die. Respects Curse of Vanishing, keepInventory and soulbound enchantments, and never blocks your bullets.
- **Source:** https://github.com/wamior/Cadaver
- **Issues:** https://github.com/wamior/Cadaver/issues
- **License:** All Rights Reserved (full text in `LICENSE`)

## CurseForge

- **Class:** Mods
- **Main category:** Utility & QoL
- **Additional categories:** Server Utility, Adventure and RPG, Armor, Tools, and Weapons
- **License:** All Rights Reserved
- **Upload files** (one per jar from `build/libs/<version>/`, release type Release):
  - `+1.21.1-neoforge.jar`: game version 1.21.1, loader NeoForge, Java 21
  - `+1.21.1-fabric.jar`: game version 1.21.1 (and 1.21), loader Fabric, Java 21
  - `+1.20.1-forge.jar`: game version 1.20.1, loader Forge, Java 17
  - `+1.20.1-fabric.jar`: game version 1.20.1, loader Fabric, Java 17
- **Relations:** Curios API → *Optional Dependency*, Trinkets → *Optional Dependency*, Fabric API → *Required Dependency* (Fabric files)

## Modrinth

- **Type:** Project (mod), URL `modrinth.com/mod/cadaver`, owner wamior, visibility Public
- **Categories:** Utility, Game Mechanics (+ optional: Equipment)
- **Environment:** Client required, Server required
- **License:** "Custom license" → `LicenseRef-All-Rights-Reserved`, URL `https://github.com/wamior/Cadaver/blob/main/LICENSE`
- **Version upload:** one version per jar, channel Release, version number = jar version (e.g. `1.0.0+1.20.1-forge`):
  NeoForge 1.21.1 · Fabric 1.21.1 (+1.21) · Forge 1.20.1 · Fabric 1.20.1
- **Dependencies:** Curios API → *optional* (NeoForge / Forge), Trinkets → *optional* and Fabric API → *required* (Fabric)

## Before publishing

- Add 3-4 gallery screenshots: a corpse lying in the world, the loot screen with armor + Curios, quick loot result.
- Make a GitHub release `v1.0.0` with the same four jars, and bump `update.json` with every release (see README).
