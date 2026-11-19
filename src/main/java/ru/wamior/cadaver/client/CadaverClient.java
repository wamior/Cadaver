package ru.wamior.cadaver.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.ModEntities;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.network.OpenCorpsePayload;

import java.util.Optional;

public final class CadaverClient {

    @EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.CORPSE.get(), CorpseRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            event.register(ru.wamior.cadaver.ModMenus.CORPSE.get(), CorpseScreen::new);
        }
    }

    @EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        /**
         * The corpse is not pickable, so vanilla never targets it. We do our own look check on
         * right click and only take over if the corpse is closer than whatever vanilla is aiming at.
         */
        @SubscribeEvent
        public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
            if (!event.isUseItem()) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.level == null || player.isSpectator()) return;

            CorpseEntity corpse = findLookedAt(mc, player);
            if (corpse == null) return;

            event.setCanceled(true);
            event.setSwingHand(true);
            PacketDistributor.sendToServer(new OpenCorpsePayload(corpse.getId(), player.isShiftKeyDown()));
        }

        private static CorpseEntity findLookedAt(Minecraft mc, LocalPlayer player) {
            double reach = player.blockInteractionRange();
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
    }

    private CadaverClient() {
    }
}
