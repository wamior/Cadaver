package ru.wamior.cadaver.version;

import com.mojang.authlib.properties.Property;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Minecraft 1.21.6+ versions of the few vanilla calls that differ between supported versions. */
public final class Mc {
    public static double interactionRange(Player player) {
        return player.blockInteractionRange();
    }

    public static String textureValue(Property property) {
        return property.value();
    }

    public static String textureSignature(Property property) {
        return property.signature();
    }

    public static int minY(Level level) {
        return level.getMinY();
    }

    public static void moveTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        entity.snapTo(x, y, z, yRot, xRot);
    }

    public static ServerLevel serverLevel(ServerPlayer player) {
        return player.level();
    }

    /** "1.21.1" etc., the key used in update.json. */
    public static String gameVersionName() {
        return SharedConstants.getCurrentVersion().name();
    }

    private Mc() {
    }
}
