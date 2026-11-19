# Cadaver

**When you die, your body stays where you fell, and it keeps everything you had on you.** Go back, open the corpse, take your gear.

Cadaver is a lightweight corpse mod. It doesn't get in your way in a fight, its despawn timer actually works, and items from other mods end up exactly where they should.

---

## ✨ Features

### 🧍 A real body
The corpse is your own player model lying on its back, wearing your skin. Skins also work on offline-mode servers with SkinsRestorer.

### 🎒 Only what would have dropped
Cadaver collects the **final** death drops, so everything other systems decide still applies:
- **Curse of Vanishing** items are gone, as in vanilla
- **`keepInventory`** — no corpse, you keep your items
- **Soulbound-style enchantments** from other mods keep their items on you
- **Curios** drop rules are respected

### 🔫 No hitbox in combat
Bullets (**TaCZ** and other gun mods), arrows, melee hits and your crosshair pass straight through corpses. A pile of bodies never eats your shots or blocks the player standing behind it.

### 🗂️ Clear loot screen
Every part of the inventory has its own section:
- **Armor**, with empty-slot icons
- **Offhand**
- **Inventory** and **hotbar**, in their original positions
- **Curios accessories** and **other modded drops**, in a side panel

Items can only be taken out, so a corpse can't be used as free storage.

### ⚡ Quick loot
**Sneak + right click** takes everything at once. The owner gets every item back into **its original slot**: armor is put back on, the hotbar is restored and accessories return to their Curios slots.

### ⏳ A despawn timer that works
The timer counts from the moment of death, **even while the chunk is unloaded**: an overdue corpse disappears as soon as its chunk loads. When it does, its items either drop on the ground or are deleted, depending on the config.

### 🛡️ Extras
- **Owner protection** (optional): for the first N minutes only the owner and operators can open the corpse
- **Death coordinates** in chat
- **Update notice** in chat when a new version is out (can be turned off)
- Corpses are immune to fire, lava, explosions and damage, and they never fall into the void

---

## ⚙️ Config

File: `config/cadaver-common.toml`. Changes apply **without a restart**.

| Option | Default | What it does |
|---|---|---|
| `general.enabled` | `true` | Create corpses on death |
| `general.sendCoordinates` | `true` | Send the corpse position in chat |
| `general.updateNotice` | `true` | Chat notice about new versions |
| `general.dimensionBlacklist` | `[]` | Dimensions without corpses (items drop as in vanilla) |
| `despawn.despawnMinutes` | `60` | Minutes until the corpse disappears, `0` = never |
| `despawn.dropItemsOnDespawn` | `true` | `true` = items drop on despawn, `false` = deleted |
| `despawn.removeWhenEmpty` | `true` | Remove the corpse once it's empty |
| `access.ownerProtectionMinutes` | `0` | Minutes only the owner can loot, `0` = off |
| `access.quickLoot` | `true` | Allow sneak + right click to take everything |

---

## 🔌 Compatibility

- Required on **both client and server**
- **Minecraft 1.21.1** (NeoForge, Fabric) and **1.20.1** (Forge, Fabric)
- **Curios API** (NeoForge / Forge) and **Trinkets** (Fabric): optional, fully supported
- **TaCZ** and other gun mods: corpses never block shots
- Works alongside mods that keep items on death; Cadaver only takes what would have dropped

## 📦 Modpacks

Feel free to include Cadaver in any modpack on CurseForge or Modrinth, no need to ask.

## 🐞 Bugs and ideas

Report them on [GitHub Issues](https://github.com/wamior/Cadaver/issues).
