package ru.wamior.cadaver.compat;

import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.entity.CurioRef;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Fabric: Trinkets, reached by reflection so Cadaver has no compile or runtime dependency on it.
 * Only Trinkets' own method names are looked up (they are not remapped); its inventories are
 * vanilla {@link Container}s. A slot is stored as {@code CurioRef("group/slot", index, false)}.
 * Only reached through {@link AccessoryCompat}.
 */
final class Accessories {
    static final String MOD_ID = "trinkets";
    static final String SECTION_TITLE_KEY = "gui.cadaver.trinkets";

    private static Method getComponent;
    private static Method getInventory;

    static void forEach(LivingEntity entity, BiConsumer<ItemStack, CurioRef> visitor) {
        for (Map.Entry<String, Map<String, Object>> group : inventory(entity).entrySet()) {
            for (Map.Entry<String, Object> slot : group.getValue().entrySet()) {
                if (!(slot.getValue() instanceof Container stacks)) continue;
                String id = group.getKey() + "/" + slot.getKey();
                for (int i = 0; i < stacks.getContainerSize(); i++) {
                    ItemStack stack = stacks.getItem(i);
                    if (!stack.isEmpty()) visitor.accept(stack, new CurioRef(id, i, false));
                }
            }
        }
    }

    static boolean tryRestore(Player player, CurioRef ref, ItemStack stack) {
        int split = ref.slotId().indexOf('/');
        if (split < 0 || ref.cosmetic()) return false;
        Map<String, Object> group = inventory(player).get(ref.slotId().substring(0, split));
        if (group == null || !(group.get(ref.slotId().substring(split + 1)) instanceof Container stacks)) return false;
        if (ref.index() >= stacks.getContainerSize() || !stacks.getItem(ref.index()).isEmpty()) return false;
        stacks.setItem(ref.index(), stack);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> inventory(LivingEntity entity) {
        try {
            if (getComponent == null) {
                getComponent = Class.forName("dev.emi.trinkets.api.TrinketsApi").getMethod("getTrinketComponent", LivingEntity.class);
                getInventory = Class.forName("dev.emi.trinkets.api.TrinketComponent").getMethod("getInventory");
            }
            Optional<?> component = (Optional<?>) getComponent.invoke(null, entity);
            if (component.isEmpty()) return Map.of();
            return (Map<String, Map<String, Object>>) getInventory.invoke(component.get());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Trinkets call failed", e.getCause());
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("Unsupported Trinkets version", e);
        }
    }

    private Accessories() {
    }
}
