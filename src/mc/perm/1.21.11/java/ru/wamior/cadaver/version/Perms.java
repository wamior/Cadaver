package ru.wamior.cadaver.version;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

/** Permission and dimension-id access, Minecraft 1.21.11+ (permission sets, ResourceKey#identifier). */
public final class Perms {
    /** Operator ("gamemaster" permissions): may open any corpse. */
    public static boolean isOperator(ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /** "minecraft:overworld" etc. */
    public static String dimensionId(Level level) {
        return level.dimension().identifier().toString();
    }

    private Perms() {
    }
}
