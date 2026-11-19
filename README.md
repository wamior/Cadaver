# Cadaver

When a player dies, their corpse stays where they fell, holding everything they had on them.

- Works with Curse of Vanishing, `keepInventory`, soulbound enchantments from other mods, Curios (NeoForge / Forge) and Trinkets (Fabric). The corpse only gets the items that would have dropped on the ground.
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
| `general.updateNotice` | true | Client: chat notice when a newer version is out |
| `general.dimensionBlacklist` | [] | Dimensions with no corpses |
| `despawn.despawnMinutes` | 60 | Minutes until the corpse disappears, 0 = never |
| `despawn.dropItemsOnDespawn` | true | Drop the items when the corpse despawns (false = delete them) |
| `despawn.removeWhenEmpty` | true | Remove the corpse once it's empty |
| `access.ownerProtectionMinutes` | 0 | How many minutes only the owner can open it |
| `access.quickLoot` | true | Allow Sneak + right click |

The same file and keys are used on every loader. Config changes apply without a restart.

## Supported versions
| Minecraft | Loader | Accessories | Jar |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1.77+ | Curios (optional) | `cadaver-<version>+1.21.1-neoforge.jar` |
| 1.21.1 | Fabric (Loader 0.15+, Fabric API) | Trinkets (optional) | `cadaver-<version>+1.21.1-fabric.jar` |
| 1.20.1 | Forge 47+ | Curios (optional) | `cadaver-<version>+1.20.1-forge.jar` |
| 1.20.1 | Fabric (Loader 0.15+, Fabric API) | Trinkets (optional) | `cadaver-<version>+1.20.1-fabric.jar` |

The mod is needed on both the server and the client. Behaviour is the same everywhere: the corpse takes the final death drops (so Curse of Vanishing, `keepInventory` and soulbound mods are respected), has no hitbox, its slots are take-only, and the despawn timer runs on game time. On Fabric the update notice reads the same `update.json` itself, since Fabric has no built-in update checker.

## Build
`gradlew buildAll` builds every target and collects the jars in `build/libs/<version>/`. A single target: `gradlew :fabric-1.20.1:build` → `targets/fabric-1.20.1/build/libs/`. Requires JDK 21 (the 1.20.1 jars are compiled for Java 17).

Icon for CurseForge/Modrinth: `icon.png` (512×512). Store texts: `docs/listing.md`, `docs/description_en.md`, `docs/description_ru.md`.

### Project layout
One codebase, one Gradle project per jar (Architectury Loom, Mojang mappings). Each target compiles three source folders:

| Folder | Contents |
|---|---|
| `src/common` | Everything shared: corpse entity, sectioned storage (`CorpseLayout`), death handling, config, menu and GUI layout, screen, renderer, look check, update notice, lang and icon |
| `src/mc-<version>` | The few vanilla calls that differ between Minecraft versions (`version/Mc`, `version/McClient`, `entity/CorpseEntityImpl`, `client/CorpseSkins`) |
| `src/<loader>-<version>` | Loader glue: entry point and registration, `platform/Platform` + `ClientPlatform`, events or mixins, networking, Curios / Trinkets (`compat/Accessories`), mod metadata |

`targets/<loader>-<version>/gradle.properties` holds the loader and dependency versions; the list of targets is `targets` in the root `gradle.properties`. To add a target, add a line there, a `targets/<name>/gradle.properties` and a `src/<name>` folder (plus `src/mc-<version>` for a new Minecraft version).

Only the `local_copy_target` jar (NeoForge 1.21.1) is picked up by a local auto-copy Gradle init script, so a full build doesn't put jars for other loaders into the test instance.

## Releasing a new version
1. Bump `mod_version` in `gradle.properties` and run `gradlew buildAll`.
2. Upload each jar to CurseForge, Modrinth and a GitHub release `vX.Y.Z`, tagged with its loader and Minecraft version.
3. Update `update.json` on `main`: for each Minecraft version, the new version in `promos` (`<mc>-latest` / `<mc>-recommended`, e.g. `1.20.1-recommended`) and a changelog line under `"<mc>"`. Players on older versions then see a mark in the mod list (NeoForge / Forge) and a chat notice on join (all loaders).

## License
All Rights Reserved, see [LICENSE](LICENSE). You may use the mod and include it in modpacks; copying the code, re-uploading and ports require permission.
