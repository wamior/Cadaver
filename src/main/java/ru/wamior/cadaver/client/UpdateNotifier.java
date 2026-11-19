package ru.wamior.cadaver.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.CadaverConfig;

/**
 * Tells the player once per game launch that a newer Cadaver is out. The data comes from NeoForge's
 * update checker (updateJSONURL in neoforge.mods.toml -> update.json on GitHub), which also marks
 * the mod in the mod list.
 */
@EventBusSubscriber(modid = Cadaver.MODID, value = Dist.CLIENT)
public final class UpdateNotifier {
    private static boolean shown;

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (shown || !CadaverConfig.UPDATE_NOTICE.get()) return;
        ModList.get().getModContainerById(Cadaver.MODID).ifPresent(container -> {
            VersionChecker.CheckResult result = VersionChecker.getResult(container.getModInfo());
            if (result.status() != VersionChecker.Status.OUTDATED && result.status() != VersionChecker.Status.BETA_OUTDATED) {
                return;
            }
            shown = true;
            String url = result.url() != null && !result.url().isEmpty() ? result.url() : "https://modrinth.com/mod/cadaver";
            Component link = Component.translatable("message.cadaver.update.link")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
            event.getPlayer().displayClientMessage(Component.translatable("message.cadaver.update",
                    container.getModInfo().getVersion().toString(), String.valueOf(result.target()), link)
                    .withStyle(ChatFormatting.GOLD), false);
        });
    }

    private UpdateNotifier() {
    }
}
