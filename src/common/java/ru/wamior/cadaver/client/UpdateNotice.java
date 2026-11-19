package ru.wamior.cadaver.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import ru.wamior.cadaver.CadaverConfig;

/**
 * Tells the player once per game launch that a newer Cadaver is out. Where the data comes from
 * depends on the loader: NeoForge / Forge use their built-in update checker (updateJSONURL in
 * mods.toml → update.json on GitHub, which also marks the mod in the mod list); Fabric has none,
 * so {@link UpdateJsonChecker} reads the same update.json.
 */
public final class UpdateNotice {
    public static final String FALLBACK_URL = "https://modrinth.com/mod/cadaver";
    private static boolean shown;

    public static void show(LocalPlayer player, String current, String target, String url) {
        if (shown || player == null || !CadaverConfig.updateNotice()) return;
        shown = true;
        String link = url != null && !url.isEmpty() ? url : FALLBACK_URL;
        Component button = Component.translatable("message.cadaver.update.link")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
        player.displayClientMessage(Component.translatable("message.cadaver.update", current, target, button)
                .withStyle(ChatFormatting.GOLD), false);
    }

    public static boolean alreadyShown() {
        return shown;
    }

    private UpdateNotice() {
    }
}
