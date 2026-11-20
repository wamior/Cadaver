package ru.wamior.cadaver.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.entity.CurioRef;

import java.util.function.BiConsumer;

/** No accessory mod for this Minecraft version (Curios was never released for it): accessories drop as normal items. */
final class Accessories {
    static final String MOD_ID = "curios";
    static final String SECTION_TITLE_KEY = "gui.cadaver.curios";

    static void forEach(LivingEntity entity, BiConsumer<ItemStack, CurioRef> visitor) {
    }

    static boolean tryRestore(Player player, CurioRef ref, ItemStack stack) {
        return false;
    }

    private Accessories() {
    }
}
