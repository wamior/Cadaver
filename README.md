# Cadaver

When a player dies, their corpse stays where they fell, holding everything they had on them.

- Works on every Minecraft version from 1.20.1 to 26.3 (Fabric, NeoForge, Forge 1.20.1).
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
Every Minecraft release from 1.20.1 to 26.3. One jar per loader and Minecraft range:

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 1.20.1 | `+1.20.1-fabric` | (use the Forge jar) | `+1.20.1-forge` |
| 1.20.2 | `+1.20.2-fabric` | `+1.20.2-neoforge` | |
| 1.20.3 – 1.20.4 | `+1.20.4-fabric` | `+1.20.4-neoforge` (1.20.4) | |
| 1.20.5 – 1.20.6 | `+1.20.6-fabric` | `+1.20.6-neoforge` (1.20.6) | |
| 1.21 – 1.21.1 | `+1.21.1-fabric` | `+1.21.1-neoforge` (1.21.1) | |
| 1.21.2 – 1.21.3 | `+1.21.3-fabric` | `+1.21.3-neoforge` (1.21.3) | |
| 1.21.4 | `+1.21.4-fabric` | `+1.21.4-neoforge` | |
| 1.21.5 | `+1.21.5-fabric` | `+1.21.5-neoforge` | |
| 1.21.6 – 1.21.8 | `+1.21.8-fabric` | `+1.21.8-neoforge` (1.21.8) | |
| 1.21.9 – 1.21.10 | `+1.21.10-fabric` | `+1.21.10-neoforge` (1.21.10) | |
| 1.21.11 | `+1.21.11-fabric` | `+1.21.11-neoforge` | |
| 26.1 – 26.1.2 | `+26.1.2-fabric` | `+26.1.2-neoforge` (26.1.2) | |
| 26.2 | `+26.2-fabric` | `+26.2-neoforge` | |
| 26.3 | `+26.3-fabric` | `+26.3-neoforge` | |

Jar names are `cadaver-<version>+<mc>-<loader>.jar`. The Fabric jars need Fabric API. Accessories: Curios on NeoForge / Forge (optional; Curios was never released for NeoForge 1.21.3, so there accessories drop as normal items), Trinkets on Fabric (optional). Forge is only built for 1.20.1: from 1.20.2 on the Forge community moved to NeoForge. Forge 1.20.1 jars also run on NeoForge 1.20.1.

The mod is needed on both the server and the client. Behaviour is the same everywhere: the corpse takes the final death drops (so Curse of Vanishing, `keepInventory` and soulbound mods are respected), has no hitbox, its slots are take-only, and the despawn timer runs on game time. On Fabric the update notice reads the same `update.json` itself, since Fabric has no built-in update checker.

## Build
`gradlew buildAll` builds every target into `build/libs/<mod version>/<Minecraft version>/`. Single-target builds use the same layout: `gradlew :neoforge-1.21.1:build` → `build/libs/1.0.0/1.21.1/cadaver-1.0.0+1.21.1-neoforge.jar`. Jars for different loaders are stored together inside their Minecraft version folder. A jar supporting several Minecraft versions is stored under the version it was compiled against, without duplicate copies. Gradle runs on JDK 25 (`gradle/gradle-daemon-jvm.properties`; it is downloaded automatically if missing); jars for 1.20.1 – 1.20.4 are compiled for Java 17, 1.20.5 – 1.21.11 for Java 21, 26.x for Java 25. The first full build downloads every Minecraft version and takes a while.

Icon for CurseForge/Modrinth: `icon.png` (512×512). Store description drafts in `docs/` are local only and excluded from Git.

### Project layout
One codebase, one Gradle project per jar (Architectury Loom; Mojang names everywhere, unobfuscated Minecraft from 26.1 on). Code that is the same on every version lives in `src/common`. Everything else is split by *concern* and by the Minecraft version it is needed from:

| Folder | Contents |
|---|---|
| `src/common` | Shared logic: corpse entity, sectioned storage (`CorpseLayout`), death handling, config, menu and GUI layout, look check, update notice, lang and icon |
| `src/mc/<concern>/<since>` | Vanilla API differences, shared by all loaders, e.g. `mc/render/1.21.2` (render states), `mc/entitydata/1.21.6` (ValueOutput saves), `mc/screen/26.1` |
| `src/<loader>/<concern>/<since>` | Loader glue: `core` (entry point, registration, events or mixins, networking, `platform/Platform`, metadata), `compat` (Curios / Trinkets), `deathhook` (Fabric death-drop mixin) |

For each concern a target compiles the newest `<since>` folder that is not newer than its Minecraft version (`gradlew :<target>:printShims` lists them; `shim.<concern>=<since>|none` in a target's properties overrides it). Pure renames Mojang made (e.g. `ResourceLocation` → `Identifier` in 1.21.11) are applied as text replacements on a copy of the sources (`sourceReplacements` in `build.gradle`) instead of copying files.

`targets/<loader>-<mc>/gradle.properties` holds the loader and dependency versions and `mc_versions`, the Minecraft versions that jar supports (its metadata ranges and the update.json entries are built from it). The list of targets is `targets` in the root `gradle.properties`. To support a new Minecraft version: add a target folder and its name to `targets`, run `gradlew :<target>:compileJava`, and add a `<since>` folder (or a replacement) for whatever no longer compiles.

Only the `local_copy_target` jar (NeoForge 1.21.1) is picked up by a local auto-copy Gradle init script, so a full build doesn't put jars for other loaders into the test instance.

## Releasing a new version
Double-click `release.bat` (or run `release.bat 1.2.0 "What changed"`). It:
1. checks that you are on `main`, nothing is uncommitted and `main` is up to date with GitHub;
2. asks for the new version (Enter = next patch version) and a one-line changelog;
3. sets `mod_version`, runs `gradlew buildAll` (jars land in `build/libs/<mod version>/<Minecraft version>/`);
4. updates `update.json`: `<mc>-latest` / `<mc>-recommended` for every supported Minecraft version, the changelog line, and `homepage` → the latest GitHub release;
5. commits, tags `vX.Y.Z`, pushes `main` and the tag, and creates a GitHub release with all jars and a "which file do I need" table.

`release.bat 1.2.0 "text" -DryRun` does steps 2–4 and shows the result without committing or pushing anything (it also works outside `main`). Needs Git and the GitHub CLI (`gh auth login` once).

Players on an older version then see a mark in the mod list (NeoForge / Forge) and a chat notice with a download link on their next join (all loaders). GitHub `update.json` remains the update source on all three loaders.

### Publishing to CurseForge and Modrinth

`release.bat 1.0.1 "What changed" -Publish` builds and publishes one release to GitHub, CurseForge and Modrinth. Requires Python 3.11+ (no pip packages), Git and GitHub CLI. Project settings live in `publish.json`: CurseForge `1816809`, Modrinth `cadavers` (a slug or project ID). The Modrinth project must be accessible with your token; draft projects may return 404 anonymously.

Set `CURSEFORGE_TOKEN` and `MODRINTH_TOKEN` in the environment of the terminal running the scripts. Generate a CurseForge author upload token at [API Tokens](https://authors.curseforge.com/account/api-tokens), and a Modrinth personal token in [settings](https://modrinth.com/settings/pats) with permission to read your project/versions and create versions (`PROJECT_READ`, `VERSION_READ`, `VERSION_CREATE`). Keep tokens out of Git and chat. To enter tokens without putting them in terminal history (PowerShell):

```powershell
$env:CURSEFORGE_TOKEN = [System.Net.NetworkCredential]::new('', (Read-Host 'CurseForge token' -AsSecureString)).Password
$env:MODRINTH_TOKEN = [System.Net.NetworkCredential]::new('', (Read-Host 'Modrinth token' -AsSecureString)).Password
.\release.bat 1.0.1 "What changed" -Publish
```

Already-built jars can be published independently, including the current version without making a new GitHub release:

```powershell
.\publish.bat --dry-run
.\publish.bat --changelog "Initial release" --check
.\publish.bat --changelog "Initial release" --upload
# One platform / selected targets only:
.\publish.bat --platforms modrinth --targets fabric-1.21.1,neoforge-1.21.1 --changelog "Fixes" --upload
```

The default (also `--dry-run`) is an offline preview: `build/publish-plan-<version>.json`. `--check` validates API credentials, Modrinth project access and all selected Minecraft/loader tags without uploading. CurseForge checks permission to upload to the project during the upload itself. `--upload` sends the jars. Use `--version 1.0.0`, `--changelog-file <path>` and `--release-type release|beta|alpha` when needed. A combined release checks both APIs before pushing GitHub; `-DryRun -Publish` only creates a preview. A plain `release.bat` retains the GitHub-only workflow and does not need platform tokens.

Supported Minecraft versions come from each target's `mc_versions`, not just the jar filename. Loader, Java, client/server tags and dependencies are set automatically: Fabric API required, Trinkets optional, Curios optional only on targets with Curios support. Missing platform tags stop the batch before the first upload; select supported targets or wait for the platform to add the tag.

Successful uploads are recorded in ignored `.publish/state.json`; keep it to resume without resending completed files, even after `gradlew clean`. Modrinth also checks remote SHA-512 hashes. Explicit validation/auth/rate-limit rejections are marked `rejected`: fix the error and rerun the same command. A timeout or server error may mean a file arrived but its response was lost: an uncertain attempt stops retries. Check the site's Files page; **only if the file is absent**, retry with `--retry-uncertain`. If it is present on CurseForge, keep the receipt and set its `status` to `done` and `id` to the actual file ID. Do not edit jar bytes after publishing; bump the mod version. After an interrupted process, remove `.publish/upload.lock` only when no publisher is running. API acceptance does not bypass moderation.

If uploading fails after a GitHub release, rerun `publish.bat --version <version> --changelog-file build/release-notes-<version>.md --upload`; do not rerun the whole release or create another tag. The publisher never edits GitHub or `update.json`.

Publishing API references: [CurseForge Upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api), [Modrinth create version](https://docs.modrinth.com/api/operations/createversion/).

## License
All Rights Reserved, see [LICENSE](LICENSE). You may use the mod and include it in modpacks; copying the code, re-uploading and ports require permission.
