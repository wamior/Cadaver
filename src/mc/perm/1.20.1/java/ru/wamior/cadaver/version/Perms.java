package ru.wamior.cadaver.version;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Permission and dimension-id access, Minecraft 1.20.1 – 1.21.10. */
public final class Perms {
    /** Operator (permission level 2, "gamemaster"): may open any corpse. */
    public static boolean isOperator(ServerPlayer player) {
        return player.hasPermissions(2);
    }

    /** "minecraft:overworld" etc. */
    public static String dimensionId(Level level) {
        return level.dimension().location().toString();
    }

    private Perms() {
    }
}
