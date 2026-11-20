package ru.wamior.cadaver.fabric;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import ru.wamior.cadaver.event.DeathHandler;
import ru.wamior.cadaver.version.Mc;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric has no drops event, so we do what Forge's LivingDropsEvent does internally: while a dying
 * player runs LivingEntity#dropAllDeathLoot, every ItemEntity added to their level is captured
 * instead of spawned (vanilla inventory, Trinkets, soulbound mods... all drop through there).
 * At the end of that method the captured drops go to {@link DeathHandler}; whatever it leaves
 * is spawned normally.
 */
public final class FabricDeath {
    private static ServerPlayer player;
    private static List<ItemEntity> captured;

    /** Mixin: HEAD of dropAllDeathLoot. */
    public static void begin(ServerPlayer dying) {
        flushStale();
        DeathHandler.snapshot(dying);
        player = dying;
        captured = new ArrayList<>();
    }

    /** Mixin: HEAD of ServerLevel#addFreshEntity. @return true if the entity was captured */
    public static boolean capture(ServerLevel level, Entity entity) {
        if (player == null || !(entity instanceof ItemEntity item) || level != Mc.serverLevel(player)) return false;
        captured.add(item);
        return true;
    }

    /** Mixin: RETURN of dropAllDeathLoot. */
    public static void end(ServerPlayer dying) {
        if (player != dying) return;
        List<ItemEntity> drops = captured;
        player = null;
        captured = null;
        DeathHandler.onDrops(dying, drops);
        spawn(Mc.serverLevel(dying), drops);
    }

    /** Safety net: if dropAllDeathLoot threw before RETURN, release the captured items at the end of the tick. */
    public static void flushStale() {
        if (player == null) return;
        ServerPlayer stale = player;
        List<ItemEntity> drops = captured;
        player = null;
        captured = null;
        DeathHandler.forget(stale.getUUID());
        spawn(Mc.serverLevel(stale), drops);
    }

    private static void spawn(ServerLevel level, List<ItemEntity> drops) {
        for (ItemEntity item : drops) level.addFreshEntity(item);
    }

    private FabricDeath() {
    }
}
