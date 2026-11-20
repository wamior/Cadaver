package ru.wamior.cadaver.platform;

import ru.wamior.cadaver.forge.CadaverForge;
import ru.wamior.cadaver.forge.OpenCorpseMessage;

/** Forge 1.20.1 client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        CadaverForge.CHANNEL.sendToServer(new OpenCorpseMessage(entityId, quickLoot));
    }

    private ClientPlatform() {
    }
}
