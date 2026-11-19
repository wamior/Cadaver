package ru.wamior.cadaver.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.platform.ClientPlatform;
import ru.wamior.cadaver.version.McClient;

import java.util.Optional;

/**
 * The corpse is not pickable, so vanilla never targets it. On right click the loader glue
 * (NeoForge / Forge: InteractionKeyMappingTriggered, Fabric: a mixin on Minecraft#startUseItem)
 * calls {@link #tryOpen()}: we do our own look check and only take over if the corpse is closer
 * than whatever vanilla is aiming at.
 */
public final class CorpseTargeting {

    /** @return true if a corpse was targeted and the open request was sent (cancel vanilla use + swing) */
    public static boolean tryOpen() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isSpectator()) return false;

        CorpseEntity corpse = findLookedAt(mc, player);
        if (corpse == null) return false;

        ClientPlatform.sendOpenCorpse(corpse.getId(), player.isShiftKeyDown());
        return true;
    }

    private static CorpseEntity findLookedAt(Minecraft mc, LocalPlayer player) {
        double reach = McClient.interactionRange(mc, player);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));

        double best = reach * reach;
        HitResult vanilla = mc.hitResult;
        if (vanilla != null && vanilla.getType() != HitResult.Type.MISS) {
            best = Math.min(best, vanilla.getLocation().distanceToSqr(eye));
        }

        CorpseEntity found = null;
        AABB search = new AABB(eye, end).inflate(1.5D);
        for (CorpseEntity corpse : mc.level.getEntitiesOfClass(CorpseEntity.class, search, CorpseEntity::isAlive)) {
            AABB box = corpse.getInteractionBox();
            Optional<Vec3> hit = box.contains(eye) ? Optional.of(eye) : box.clip(eye, end);
            if (hit.isEmpty()) continue;
            double d = hit.get().distanceToSqr(eye);
            if (d < best) {
                best = d;
                found = corpse;
            }
        }
        return found;
    }

    private CorpseTargeting() {
    }
}
