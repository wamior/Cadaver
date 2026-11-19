package ru.wamior.cadaver;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class CadaverConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue DESPAWN_MINUTES;
    public static final ModConfigSpec.BooleanValue DROP_ITEMS_ON_DESPAWN;
    public static final ModConfigSpec.BooleanValue REMOVE_WHEN_EMPTY;
    public static final ModConfigSpec.IntValue OWNER_PROTECTION_MINUTES;
    public static final ModConfigSpec.BooleanValue QUICK_LOOT;
    public static final ModConfigSpec.BooleanValue SEND_COORDINATES;
    public static final ModConfigSpec.BooleanValue UPDATE_NOTICE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSION_BLACKLIST;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        ENABLED = b.comment("Spawn a corpse when a player dies with items.")
                .define("enabled", true);
        SEND_COORDINATES = b.comment("Tell the player where their corpse is.")
                .define("sendCoordinates", true);
        UPDATE_NOTICE = b.comment("Client: show a chat message when a newer Cadaver version is available.")
                .define("updateNotice", true);
        DIMENSION_BLACKLIST = b.comment("Dimensions where no corpse is created (items drop as in vanilla). Example: [\"minecraft:the_end\"]")
                .defineListAllowEmpty("dimensionBlacklist", List.of(), () -> "minecraft:the_end", o -> o instanceof String);
        b.pop();

        b.push("despawn");
        DESPAWN_MINUTES = b.comment(
                        "Minutes of server game time until a corpse disappears. 0 = never.",
                        "Counted from the moment of death, also while the chunk is unloaded:",
                        "an overdue corpse is removed as soon as its chunk loads again.")
                .defineInRange("despawnMinutes", 60, 0, 1_000_000);
        DROP_ITEMS_ON_DESPAWN = b.comment("true = items fall on the ground when the corpse despawns, false = items are deleted.")
                .define("dropItemsOnDespawn", true);
        REMOVE_WHEN_EMPTY = b.comment("Remove the corpse once all items have been taken.")
                .define("removeWhenEmpty", true);
        b.pop();

        b.push("access");
        OWNER_PROTECTION_MINUTES = b.comment("For this many minutes after death only the owner (and operators) can open the corpse. 0 = anyone at any time.")
                .defineInRange("ownerProtectionMinutes", 0, 0, 1_000_000);
        QUICK_LOOT = b.comment("Sneak + right click takes everything at once. The owner gets items back into their original slots, armor included.")
                .define("quickLoot", true);
        b.pop();

        SPEC = b.build();
    }

    private CadaverConfig() {
    }
}
