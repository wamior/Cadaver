package ru.wamior.cadaver.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ru.wamior.cadaver.fabric.OpenCorpsePayload;

/** Fabric 26.1+ client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        ClientPlayNetworking.send(new OpenCorpsePayload(entityId, quickLoot));
    }

    private ClientPlatform() {
    }
}
