# Cadaver

When you die, your body stays where you fell, and it holds everything you were carrying. Walk back, open the corpse, take your gear.

Cadaver was made as a lighter, more predictable alternative to other grave and corpse mods: the corpse never gets in the way of combat, the despawn timer actually works, and items from other mods end up exactly where they should.

## Features

- **Your corpse, your skin.** The corpse is your player model lying on its back, wearing your skin. It works on offline-mode servers with SkinsRestorer too.
- **Only what would have dropped.** Curse of Vanishing, `keepInventory`, soulbound-style enchantments from other mods and Curios drop rules are all respected. Cadaver takes the final death drops, so anything a mod keeps or destroys stays kept or destroyed.
- **No hitbox in combat.** Bullets (TaCZ and other gun mods), arrows, melee hits and your crosshair pass straight through corpses. Lying bodies never eat your shots.
- **Clear loot screen.** Armor, offhand, inventory and hotbar each have their own section. Curios accessories and other modded drops sit in a separate side panel.
- **Quick loot.** Sneak + right click takes everything at once. The owner gets every item back into its original slot: armor is equipped, the hotbar is restored, and accessories go back into their Curios slots.
- **Take-only.** Corpses can't be used as free storage.
- **A despawn timer that works.** The timer counts from the moment of death, also while the chunk is unloaded. A corpse past its time disappears as soon as its chunk loads. Items can drop on the ground or be deleted, your choice.
- **Owner protection (optional).** For the first N minutes only the owner and operators can open the corpse.
- **Death coordinates** are sent in chat.
- **Update notice** in chat when a new version is out (can be turned off).

## Config

`config/cadaver-common.toml`. Changes apply without a restart.

| Option | Default | Description |
|---|---|---|
| `general.enabled` | `true` | Create corpses |
| `general.sendCoordinates` | `true` | Send the corpse position in chat |
| `general.updateNotice` | `true` | Chat notice about new versions |
| `general.dimensionBlacklist` | `[]` | Dimensions without corpses (vanilla drops there) |
| `despawn.despawnMinutes` | `60` | Minutes until the corpse disappears, `0` = never |
| `despawn.dropItemsOnDespawn` | `true` | Drop the items on despawn instead of deleting them |
| `despawn.removeWhenEmpty` | `true` | Remove the corpse once it's empty |
| `access.ownerProtectionMinutes` | `0` | Owner-only time after death |
| `access.quickLoot` | `true` | Allow sneak + right click to take everything |

## Compatibility

- **Required on both client and server.**
- Curios: optional, fully supported.
- TaCZ and other gun mods: corpses never block bullets.

## Modpacks

You can include Cadaver in any modpack on CurseForge or Modrinth, no need to ask.

---

## На русском

После смерти на месте гибели остаётся ваш труп со всеми вещами. Труп выглядит как ваша модель со скином. Он учитывает проклятие утраты, `keepInventory`, «привязанные» зачарования из других модов и Curios. Пули, стрелы и удары проходят сквозь трупы.

В окне трупа броня, левая рука, инвентарь и аксессуары Curios лежат по отдельности. Shift+ПКМ забирает всё сразу и раскладывает вещи по прежним слотам. Таймер исчезновения работает и в выгруженных чанках. Можно использовать в любых сборках.
