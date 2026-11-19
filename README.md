# Cadaver

When a player dies, their corpse stays where they fell, holding everything they had on them.

- Works with Curse of Vanishing, `keepInventory`, soulbound enchantments from other mods and Curios. The corpse only gets the items that would have dropped on the ground.
- No hitbox: bullets (TaCZ and others), arrows, hits and the crosshair go straight through the corpse.
- Right click opens the corpse. Sneak + right click takes everything at once, and the owner gets each item back in its original slot, armor included.
- Items can only be taken out of a corpse, not put in, so it can't be used as storage.
- The despawn timer counts from the moment of death, even while the chunk is unloaded.
- The corpse shows the player's skin, including on servers with SkinsRestorer.

## Config: `config/cadaver-common.toml`
| Key | Default | What it does |
|---|---|---|
| `general.enabled` | true | Turns corpses on or off |
| `general.sendCoordinates` | true | Sends the corpse's coordinates in chat |
| `general.dimensionBlacklist` | [] | Dimensions with no corpses |
| `despawn.despawnMinutes` | 60 | Minutes until the corpse disappears, 0 = never |
| `despawn.dropItemsOnDespawn` | true | Drop the items when the corpse despawns (false = delete them) |
| `despawn.removeWhenEmpty` | true | Remove the corpse once it's empty |
| `access.ownerProtectionMinutes` | 0 | How many minutes only the owner can open it |
| `access.quickLoot` | true | Allow Sneak + right click |

Config changes apply without a restart.

## Build
`gradlew build` → `build/libs/`. Icon for CurseForge/Modrinth: `icon.png` (512×512). Store texts: `docs/listing.md`, `docs/description.md`.

## Releasing a new version
1. Bump `mod_version` in `gradle.properties` and build.
2. Upload the jar to CurseForge, Modrinth and a GitHub release `vX.Y.Z`.
3. Update `update.json` on `main`: the new version in `promos` (`1.21.1-latest` / `1.21.1-recommended`) and a changelog line. Players on older versions then see a mark in the mod list and a chat notice on join.

## License
All Rights Reserved, see [LICENSE](LICENSE). You may use the mod and include it in modpacks; copying the code, re-uploading and ports require permission.
