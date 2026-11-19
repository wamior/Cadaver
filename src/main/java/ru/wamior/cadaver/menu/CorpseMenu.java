package ru.wamior.cadaver.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.ModMenus;
import ru.wamior.cadaver.entity.CorpseLayout;

/**
 * Corpse loot menu with separate sections: armor, offhand, inventory, Curios and other drops.
 * Corpse slots are take-only, so a corpse can't be used as free storage.
 */
public class CorpseMenu extends AbstractContainerMenu {
    private static final ResourceLocation[] ARMOR_ICONS = {
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
    };

    private final Container corpse;
    private final CorpseGuiLayout layout;
    private final int corpseSlotCount;

    /** Client side: the counts come from the server so both sides build identical slots. */
    public static CorpseMenu client(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        int curios = buf.readVarInt();
        int others = buf.readVarInt();
        return new CorpseMenu(containerId, playerInventory, new SimpleContainer(CorpseLayout.SIZE), curios, others);
    }

    public CorpseMenu(int containerId, Inventory playerInventory, Container corpse, int curios, int others) {
        super(ModMenus.CORPSE.get(), containerId);
        checkContainerSize(corpse, CorpseLayout.SIZE);
        this.corpse = corpse;
        this.layout = new CorpseGuiLayout(curios, others);

        for (int i = 0; i < CorpseLayout.ARMOR_COUNT; i++) {
            addSlot(new LootSlot(corpse, CorpseLayout.ARMOR + i, CorpseGuiLayout.ARMOR_X + i * 18, CorpseGuiLayout.ARMOR_Y, ARMOR_ICONS[i]));
        }
        addSlot(new LootSlot(corpse, CorpseLayout.OFFHAND, CorpseGuiLayout.OFFHAND_X, CorpseGuiLayout.ARMOR_Y, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new LootSlot(corpse, CorpseLayout.MAIN + row * 9 + col, 8 + col * 18, CorpseGuiLayout.MAIN_Y + row * 18, null));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new LootSlot(corpse, CorpseLayout.HOTBAR + col, 8 + col * 18, CorpseGuiLayout.HOTBAR_Y, null));
        }
        for (int k = 0; k < layout.curios; k++) {
            addSlot(new LootSlot(corpse, CorpseLayout.EXTRA + k, layout.curioSlotX(k), layout.curioSlotY(k), null));
        }
        for (int k = 0; k < layout.others; k++) {
            addSlot(new LootSlot(corpse, CorpseLayout.EXTRA + layout.curios + k, layout.otherSlotX(k), layout.otherSlotY(k), null));
        }
        corpseSlotCount = slots.size();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, CorpseGuiLayout.PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, CorpseGuiLayout.PLAYER_HOTBAR_Y));
        }

        corpse.startOpen(playerInventory.player);
    }

    public CorpseGuiLayout layout() {
        return layout;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= corpseSlotCount) return ItemStack.EMPTY; // nothing goes into a corpse
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!moveItemStackTo(stack, corpseSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return corpse.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        corpse.stopOpen(player);
    }

    private static final class LootSlot extends Slot {
        private final ResourceLocation emptyIcon;

        LootSlot(Container container, int index, int x, int y, ResourceLocation emptyIcon) {
            super(container, index, x, y);
            this.emptyIcon = emptyIcon;
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
}
