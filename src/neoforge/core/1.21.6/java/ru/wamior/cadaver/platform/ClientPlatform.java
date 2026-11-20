package ru.wamior.cadaver.platform;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import ru.wamior.cadaver.neoforge.OpenCorpsePayload;

/** NeoForge 1.21.6+ client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        ClientPacketDistributor.sendToServer(new OpenCorpsePayload(entityId, quickLoot));
    }

    private ClientPlatform() {
    }
}
