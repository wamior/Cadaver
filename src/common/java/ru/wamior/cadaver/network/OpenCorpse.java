package ru.wamior.cadaver.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.version.Ids;
import ru.wamior.cadaver.version.Mc;

/**
 * Client → server: "I right-clicked this corpse" (entity id + sneak flag). Needed because the corpse
 * is not pickable. Each loader carries the two fields its own way; this is the shared server handler.
 * Must run on the server thread.
 */
public final class OpenCorpse {
    public static final ResourceLocation ID = Ids.id("open_corpse");

    public static void handle(ServerPlayer player, int entityId, boolean quickLoot) {
        if (player == null || player.isSpectator() || !player.isAlive()) return;
        Entity entity = player.level().getEntity(entityId);
        if (!(entity instanceof CorpseEntity corpse) || !corpse.isAlive()) return;
        double reach = Mc.interactionRange(player) + 2.0D;
        if (corpse.getInteractionBox().distanceToSqr(player.getEyePosition()) > reach * reach) return;
        corpse.interact(player, quickLoot);
    }

    private OpenCorpse() {
    }
}
