package ru.wamior.cadaver.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Version-neutral saved state of a corpse. {@code layout} is {@link CorpseLayout#VERSION} for current
 * saves; older (layout 1) saves carry the 54-slot chest layout in {@code items}. {@code deathTime} is
 * null when the save has none.
 */
public record CorpseState(Optional<UUID> owner, String ownerName, String skinValue, String skinSignature, Long deathTime,
                          int layout, int curioCount, int otherCount, Map<Integer, CurioRef> curioRefs,
                          NonNullList<ItemStack> items) {
    /** Number of item slots stored for a save of this layout version. */
    public static int itemSlots(int layout) {
        return layout >= CorpseLayout.VERSION ? CorpseLayout.SIZE : 54;
    }
}
