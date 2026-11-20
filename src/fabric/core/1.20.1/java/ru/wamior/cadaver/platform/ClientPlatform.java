package ru.wamior.cadaver.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import ru.wamior.cadaver.network.OpenCorpse;

/** Fabric 1.20.1 client-side loader hooks. */
public final class ClientPlatform {
    public static void sendOpenCorpse(int entityId, boolean quickLoot) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entityId);
        buf.writeBoolean(quickLoot);
        ClientPlayNetworking.send(OpenCorpse.ID, buf);
    }

    private ClientPlatform() {
    }
}
