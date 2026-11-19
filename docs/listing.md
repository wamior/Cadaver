# Store listing fields

The long description for both sites is `docs/description.md` (paste it as is; both CurseForge and Modrinth accept Markdown).

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
- **Upload a file:** game version 1.21.1, loader NeoForge, Java 21, release type Release
- **Relations:** Curios API → *Optional Dependency*

## Modrinth

- **Type:** Project (mod), URL `modrinth.com/mod/cadaver`, owner wamior, visibility Public
- **Categories:** Utility, Game Mechanics (+ optional: Equipment)
- **Environment:** Client required, Server required
- **License:** "Custom license" → `LicenseRef-All-Rights-Reserved`, URL `https://github.com/wamior/Cadaver/blob/main/LICENSE`
- **Version upload:** loaders NeoForge, game version 1.21.1, channel Release
- **Dependencies:** Curios API → *optional*

## Before publishing

- Add 3-4 gallery screenshots: a corpse lying in the world, the loot screen with armor + Curios, quick loot result.
- Make a GitHub release `v1.0.0` with the same jar, and bump `update.json` with every release (see README).
