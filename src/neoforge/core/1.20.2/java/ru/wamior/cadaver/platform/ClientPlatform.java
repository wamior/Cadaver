package ru.wamior.cadaver.platform;

import ru.wamior.cadaver.neoforge.CadaverNeoForge;
import ru.wamior.cadaver.neoforge.OpenCorpseMessage;

/** NeoForge 1.20.2 client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        CadaverNeoForge.CHANNEL.sendToServer(new OpenCorpseMessage(entityId, quickLoot));
    }

    private ClientPlatform() {
    }
}
