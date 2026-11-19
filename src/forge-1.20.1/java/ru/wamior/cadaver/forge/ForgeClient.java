package ru.wamior.cadaver.forge;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.client.CorpseRenderer;
import ru.wamior.cadaver.client.CorpseScreen;
import ru.wamior.cadaver.client.CorpseTargeting;
import ru.wamior.cadaver.client.UpdateNotice;

public final class ForgeClient {

    @Mod.EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CadaverForge.CORPSE.get(), CorpseRenderer::new);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(CadaverForge.CORPSE_MENU.get(), CorpseScreen::new));
        }
    }

    @Mod.EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT)
    public static final class GameEvents {
        @SubscribeEvent
        public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
            if (event.isUseItem() && CorpseTargeting.tryOpen()) {
                event.setCanceled(true);
                event.setSwingHand(true);
            }
        }

        /** Update data comes from Forge's checker (updateJSONURL in mods.toml). */
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

    private ForgeClient() {
    }
}
