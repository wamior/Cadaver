package ru.wamior.cadaver.version;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.net.URI;

/** Chat helpers, Minecraft 26.1+ (click events are records, overlay / system message methods). */
public final class Chat {
    public static ClickEvent openUrl(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }

    /** Message above the hotbar. */
    public static void actionBar(Player player, Component message) {
        player.sendOverlayMessage(message);
    }

    /** Message in the chat. */
    public static void system(Player player, Component message) {
        player.sendSystemMessage(message);
    }

    private Chat() {
    }
}
