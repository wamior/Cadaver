package ru.wamior.cadaver.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.MenuScreens;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.CadaverConfig;
import ru.wamior.cadaver.client.CorpseRenderer;
import ru.wamior.cadaver.client.CorpseScreen;
import ru.wamior.cadaver.client.UpdateJsonChecker;
import ru.wamior.cadaver.client.UpdateNotice;

public final class CadaverFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CadaverFabric.CORPSE, CorpseRenderer::new);
        MenuScreens.register(CadaverFabric.CORPSE_MENU, CorpseScreen::new);
        startUpdateCheck();
    }

    /** Fabric has no built-in update checker: read the same update.json NeoForge / Forge use. */
    private static void startUpdateCheck() {
        if (!CadaverConfig.updateNotice()) return;
        ModMetadata meta = FabricLoader.getInstance().getModContainer(Cadaver.MODID).orElseThrow().getMetadata();
        CustomValue url = meta.getCustomValue("cadaver:update_json");
        if (url == null || url.getType() != CustomValue.CvType.STRING) return;
        UpdateJsonChecker.start(url.getAsString(), SharedConstants.getCurrentVersion().getName(), meta.getVersion().getFriendlyString());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            UpdateJsonChecker.Result result = UpdateJsonChecker.result();
            if (result != null && client.player != null && !UpdateNotice.alreadyShown()) {
                UpdateNotice.show(client.player, result.current(), result.target(), result.url());
            }
        });
    }
}
