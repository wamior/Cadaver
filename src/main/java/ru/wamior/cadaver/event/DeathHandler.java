package ru.wamior.cadaver.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.CadaverConfig;
import ru.wamior.cadaver.entity.CorpseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import ru.wamior.cadaver.compat.CuriosCompat;
import ru.wamior.cadaver.entity.CurioRef;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The corpse is built from the final death drops, not from the raw inventory. By the time
 * LivingDropsEvent fires (we listen at LOWEST priority) vanilla has already destroyed Curse of
 * Vanishing items and honoured keepInventory, and other mods have removed soulbound / keep-on-death
 * items or added their own (Curios, backpacks...). Whatever would have hit the ground goes into the corpse.
 */
@EventBusSubscriber(modid = Cadaver.MODID)
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Inventory inv = player.getInventory();
        Snapshot snap = new Snapshot();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) snap.add(stack, i);
        }
        CuriosCompat.forEachCurio(player, snap::add);
        SNAPSHOTS.put(player.getUUID(), snap);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Snapshot snap = SNAPSHOTS.remove(player.getUUID());

        if (!CadaverConfig.ENABLED.get() || event.getDrops().isEmpty()) return;
        String dimension = player.level().dimension().location().toString();
        if (CadaverConfig.DIMENSION_BLACKLIST.get().contains(dimension)) return;

        CorpseEntity corpse = CorpseEntity.create(player);
        List<Map.Entry<ItemStack, CurioRef>> curios = new ArrayList<>();
        List<ItemStack> others = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) continue;
            Object origin = snap != null ? snap.find(stack) : null;
            ItemStack copy = stack.copy();
            if (origin instanceof Integer slot && corpse.addToPlayerSlot(copy, slot)) continue;
            if (origin instanceof CurioRef ref) curios.add(Map.entry(copy, ref));
            else others.add(copy);
        }
        List<ItemStack> overflow = corpse.fillExtras(curios, others);
        event.getDrops().clear();

        ServerLevel level = player.serverLevel();
        if (!corpse.getInventory().isEmpty()) {
            level.addFreshEntity(corpse);
            if (CadaverConfig.SEND_COORDINATES.get()) {
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

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        SNAPSHOTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SNAPSHOTS.remove(event.getEntity().getUUID());
    }

    private DeathHandler() {
    }
}
