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

/** Minecraft 1.20.1 versions of the few vanilla calls that differ between supported versions. */
public final class Mc {
    public static ResourceLocation id(String path) {
        return new ResourceLocation(Cadaver.MODID, path);
    }

    /** 1.20.1 has no interaction range attribute: vanilla reach is 4.5 blocks, 5 in creative. */
    public static double interactionRange(Player player) {
        return player.isCreative() ? 5.0D : 4.5D;
    }

    public static String textureValue(Property property) {
        return property.getValue();
    }

    public static String textureSignature(Property property) {
        return property.getSignature();
    }

    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, Level level) {
        ContainerHelper.saveAllItems(tag, items);
    }

    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, Level level) {
        ContainerHelper.loadAllItems(tag, items);
    }

    private Mc() {
    }
}
