package ru.wamior.cadaver.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.entity.CurioRef;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.function.BiConsumer;

/** Direct Curios API calls. Only reached through {@link CuriosCompat}. */
final class CuriosImpl {
    static void forEachCurio(LivingEntity entity, BiConsumer<ItemStack, CurioRef> visitor) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inv -> inv.getCurios().forEach((id, handler) -> {
            visit(handler.getStacks(), id, false, visitor);
            visit(handler.getCosmeticStacks(), id, true, visitor);
        }));
    }

    private static void visit(IDynamicStackHandler stacks, String id, boolean cosmetic, BiConsumer<ItemStack, CurioRef> visitor) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (!stack.isEmpty()) visitor.accept(stack, new CurioRef(id, i, cosmetic));
        }
    }

    static boolean tryRestore(Player player, CurioRef ref, ItemStack stack) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> inv.getStacksHandler(ref.slotId()))
                .map(handler -> {
                    IDynamicStackHandler stacks = ref.cosmetic() ? handler.getCosmeticStacks() : handler.getStacks();
                    if (ref.index() < stacks.getSlots() && stacks.getStackInSlot(ref.index()).isEmpty()) {
                        stacks.setStackInSlot(ref.index(), stack);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    private CuriosImpl() {
    }
}
