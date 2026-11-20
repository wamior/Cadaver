package ru.wamior.cadaver.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import ru.wamior.cadaver.network.OpenCorpse;

/** NeoForge 1.20.2 SimpleChannel transport of {@link OpenCorpse}. */
public record OpenCorpseMessage(int entityId, boolean quickLoot) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(quickLoot);
    }

    public static OpenCorpseMessage decode(FriendlyByteBuf buf) {
        return new OpenCorpseMessage(buf.readVarInt(), buf.readBoolean());
    }

    /** Registered with consumerMainThread, so this already runs on the server thread. */
    public static void handle(OpenCorpseMessage msg, NetworkEvent.Context ctx) {
        OpenCorpse.handle(ctx.getSender(), msg.entityId(), msg.quickLoot());
    }
}
