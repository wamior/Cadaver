package ru.wamior.cadaver.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.entity.CurioRef;
import ru.wamior.cadaver.platform.Platform;

import java.util.function.BiConsumer;

/**
 * Safe entry point for the accessory mod of the current loader: Curios on NeoForge / Forge,
 * Trinkets on Fabric. Every method checks that the mod is installed before touching the
 * per-target {@link Accessories} class, so the accessory mod's classes are never loaded without it.
 */
public final class AccessoryCompat {
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) loaded = Platform.isModLoaded(Accessories.MOD_ID);
        return loaded;
    }

    /** Translation key of the GUI section title ("Curios" / "Trinkets"). */
    public static String sectionTitleKey() {
        return Accessories.SECTION_TITLE_KEY;
    }

    /** Reports every non-empty accessory stack (regular and cosmetic) with its slot. */
    public static void forEachAccessory(LivingEntity entity, BiConsumer<ItemStack, CurioRef> visitor) {
        if (!isLoaded()) return;
        try {
            Accessories.forEach(entity, visitor);
        } catch (LinkageError | RuntimeException e) {
            AccessoriesFailure.report(e);
        }
    }

    /** Puts the stack back into its accessory slot if that slot exists and is empty. */
    public static boolean tryRestore(Player player, CurioRef ref, ItemStack stack) {
        if (!isLoaded()) return false;
        try {
            return Accessories.tryRestore(player, ref, stack);
        } catch (LinkageError | RuntimeException e) {
            AccessoriesFailure.report(e);
            return false;
        }
    }

    private AccessoryCompat() {
    }
}
