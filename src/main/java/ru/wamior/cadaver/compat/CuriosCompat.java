package ru.wamior.cadaver.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import ru.wamior.cadaver.entity.CurioRef;

import java.util.function.BiConsumer;

/**
 * Safe entry point for Curios: every method checks that Curios is installed before touching
 * {@link CuriosImpl}, so the Curios classes are never loaded without the mod.
 */
public final class CuriosCompat {
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) loaded = ModList.get().isLoaded("curios");
        return loaded;
    }

    /** Reports every non-empty curio stack (regular and cosmetic) with its slot. */
    public static void forEachCurio(LivingEntity entity, BiConsumer<ItemStack, CurioRef> visitor) {
        if (isLoaded()) CuriosImpl.forEachCurio(entity, visitor);
    }

    /** Puts the stack back into its curio slot if that slot exists and is empty. */
    public static boolean tryRestore(Player player, CurioRef ref, ItemStack stack) {
        return isLoaded() && CuriosImpl.tryRestore(player, ref, stack);
    }

    private CuriosCompat() {
    }
}
