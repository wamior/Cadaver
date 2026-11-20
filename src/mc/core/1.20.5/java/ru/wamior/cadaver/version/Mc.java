package ru.wamior.cadaver.version;

import com.mojang.authlib.properties.Property;
import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Minecraft 1.20.5 – 1.21.1 versions of the few vanilla calls that differ between supported versions. */
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

    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, Level level) {
        ContainerHelper.saveAllItems(tag, items, level.registryAccess());
    }

    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, Level level) {
        ContainerHelper.loadAllItems(tag, items, level.registryAccess());
    }

    public static int minY(Level level) {
        return level.getMinBuildHeight();
    }

    public static void moveTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        entity.moveTo(x, y, z, yRot, xRot);
    }

    public static ServerLevel serverLevel(ServerPlayer player) {
        return player.serverLevel();
    }

    /** "1.21.1" etc., the key used in update.json. */
    public static String gameVersionName() {
        return SharedConstants.getCurrentVersion().getName();
    }

    private Mc() {
    }
}
