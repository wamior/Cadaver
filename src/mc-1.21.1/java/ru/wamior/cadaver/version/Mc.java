package ru.wamior.cadaver.version;

import com.mojang.authlib.properties.Property;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.wamior.cadaver.Cadaver;

/** Minecraft 1.21.1 versions of the few vanilla calls that differ between supported versions. */
public final class Mc {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Cadaver.MODID, path);
    }

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

    private Mc() {
    }
}
