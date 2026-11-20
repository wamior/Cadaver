package ru.wamior.cadaver.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Take-only corpse slot with an optional empty-slot icon (Minecraft 1.20.1 – 1.21.3: atlas + sprite pair). */
final class LootSlot extends Slot {
    private static final ResourceLocation[] ARMOR_ICONS = {
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
    };

    private final ResourceLocation emptyIcon;

    LootSlot(Container container, int index, int x, int y, ResourceLocation emptyIcon) {
        super(container, index, x, y);
        this.emptyIcon = emptyIcon;
    }

    /** Icon of armor slot 0..3 (helmet, chestplate, leggings, boots). */
    static ResourceLocation armorIcon(int i) {
        return ARMOR_ICONS[i];
    }

    static ResourceLocation offhandIcon() {
        return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return emptyIcon == null ? null : Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon);
    }
}
