package ru.wamior.cadaver.version;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.net.URI;

/** Chat helpers, Minecraft 1.21.5 – 1.21.11 (click events are records). */
public final class Chat {
    public static ClickEvent openUrl(String url) {
        return new ClickEvent.OpenUrl(URI.create(url));
    }

    /** Message above the hotbar. */
    public static void actionBar(Player player, Component message) {
        player.displayClientMessage(message, true);
    }

    /** Message in the chat. */
    public static void system(Player player, Component message) {
        player.displayClientMessage(message, false);
    }

    private Chat() {
    }
}
