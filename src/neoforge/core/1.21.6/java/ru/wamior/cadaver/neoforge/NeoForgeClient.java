package ru.wamior.cadaver.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.client.CorpseRenderer;
import ru.wamior.cadaver.client.CorpseScreen;
import ru.wamior.cadaver.client.CorpseTargeting;
import ru.wamior.cadaver.client.UpdateNotice;

public final class NeoForgeClient {

    @EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CadaverNeoForge.CORPSE.get(), CorpseRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(CadaverNeoForge.CORPSE_MENU.get(), CorpseScreen::new);
        }
    }

    @EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        @SubscribeEvent
        public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
            if (event.isUseItem() && CorpseTargeting.tryOpen()) {
                event.setCanceled(true);
                event.setSwingHand(true);
            }
        }

        /** Update data comes from NeoForge's checker (updateJSONURL in neoforge.mods.toml). */
        @SubscribeEvent
        public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            if (UpdateNotice.alreadyShown()) return;
            ModList.get().getModContainerById(Cadaver.MODID).ifPresent(container -> {
                VersionChecker.CheckResult result = VersionChecker.getResult(container.getModInfo());
                if (result.status() != VersionChecker.Status.OUTDATED && result.status() != VersionChecker.Status.BETA_OUTDATED) return;
                UpdateNotice.show(event.getPlayer(), container.getModInfo().getVersion().toString(),
                        String.valueOf(result.target()), result.url());
            });
        }
    }

    private NeoForgeClient() {
    }
}
