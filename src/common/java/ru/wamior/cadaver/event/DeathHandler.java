package ru.wamior.cadaver.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import ru.wamior.cadaver.CadaverConfig;
import ru.wamior.cadaver.compat.AccessoryCompat;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.entity.CurioRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The corpse is built from the final death drops, not from the raw inventory. By the time the
 * loader hands us the drops (NeoForge / Forge: LivingDropsEvent at LOWEST priority; Fabric: the end
 * of LivingEntity#dropAllDeathLoot) vanilla has already destroyed Curse of Vanishing items and
 * honoured keepInventory, and other mods have removed soulbound / keep-on-death items or added their
 * own (Curios, Trinkets, backpacks...). Whatever would have hit the ground goes into the corpse.
 */
public final class DeathHandler {
    /** Inventory snapshot taken on death, used only to remember which slot each stack came from. */
    private static final Map<UUID, Snapshot> SNAPSHOTS = new HashMap<>();

    /** Where a stack was before death: an Integer player inventory slot or a {@link CurioRef}. */
    private record Entry(ItemStack original, ItemStack copy, Object origin) {
    }

    private static final class Snapshot {
        final List<Entry> entries = new ArrayList<>();

        void add(ItemStack stack, Object origin) {
            entries.add(new Entry(stack, stack.copy(), origin));
        }

        /** Same stack object first (the usual case), then an equal stack (mods that copy on drop). */
        Object find(ItemStack stack) {
            for (Iterator<Entry> it = entries.iterator(); it.hasNext(); ) {
                Entry e = it.next();
                if (e.original == stack) {
                    it.remove();
                    return e.origin;
                }
            }
            for (Iterator<Entry> it = entries.iterator(); it.hasNext(); ) {
                Entry e = it.next();
                if (ItemStack.matches(e.copy, stack)) {
                    it.remove();
                    return e.origin;
                }
            }
            return null;
        }
    }

    /** Call once the player is dead, before the drops are made. */
    public static void snapshot(ServerPlayer player) {
        Inventory inv = player.getInventory();
        Snapshot snap = new Snapshot();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) snap.add(stack, i);
        }
        AccessoryCompat.forEachAccessory(player, snap::add);
        SNAPSHOTS.put(player.getUUID(), snap);
    }

    /** Drops a stale snapshot (respawn / logout without a drops call). */
    public static void forget(UUID player) {
        SNAPSHOTS.remove(player);
    }

    /**
     * Moves the final death drops into a new corpse. Drops that were taken are removed from
     * {@code drops}; what is left there (nothing, unless corpses are off here) drops as usual.
     */
    public static void onDrops(ServerPlayer player, Collection<ItemEntity> drops) {
        Snapshot snap = SNAPSHOTS.remove(player.getUUID());

        if (!CadaverConfig.enabled() || drops.isEmpty()) return;
        String dimension = player.level().dimension().location().toString();
        if (CadaverConfig.dimensionBlacklist().contains(dimension)) return;

        CorpseEntity corpse = CorpseEntity.create(player);
        List<Map.Entry<ItemStack, CurioRef>> curios = new ArrayList<>();
        List<ItemStack> others = new ArrayList<>();
        for (ItemEntity drop : drops) {
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) continue;
            Object origin = snap != null ? snap.find(stack) : null;
            ItemStack copy = stack.copy();
            if (origin instanceof Integer slot && corpse.addToPlayerSlot(copy, slot)) continue;
            if (origin instanceof CurioRef ref) curios.add(Map.entry(copy, ref));
            else others.add(copy);
        }
        List<ItemStack> overflow = corpse.fillExtras(curios, others);
        drops.clear();

        ServerLevel level = player.serverLevel();
        if (!corpse.getInventory().isEmpty()) {
            level.addFreshEntity(corpse);
            if (CadaverConfig.sendCoordinates()) {
                player.sendSystemMessage(Component.translatable("message.cadaver.location",
                        corpse.getBlockX(), corpse.getBlockY(), corpse.getBlockZ(), dimension));
            }
        }
        for (ItemStack stack : overflow) {
            ItemEntity item = new ItemEntity(level, corpse.getX(), corpse.getY() + 0.5D, corpse.getZ(), stack);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
    }

    private DeathHandler() {
    }
}
