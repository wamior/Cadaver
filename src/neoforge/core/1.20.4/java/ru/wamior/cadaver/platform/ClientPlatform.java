package ru.wamior.cadaver.platform;

import net.neoforged.neoforge.network.PacketDistributor;
import ru.wamior.cadaver.neoforge.OpenCorpsePayload;

/** NeoForge 1.20.4 client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        PacketDistributor.SERVER.noArg().send(new OpenCorpsePayload(entityId, quickLoot));
    }

    private ClientPlatform() {
    }
}
