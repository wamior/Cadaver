package ru.wamior.cadaver.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.wamior.cadaver.network.OpenCorpse;

import java.util.function.Supplier;

/** Forge SimpleChannel transport of {@link OpenCorpse}. */
public record OpenCorpseMessage(int entityId, boolean quickLoot) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(quickLoot);
    }

    public static OpenCorpseMessage decode(FriendlyByteBuf buf) {
        return new OpenCorpseMessage(buf.readVarInt(), buf.readBoolean());
    }

    /** Registered with consumerMainThread, so this already runs on the server thread. */
    public static void handle(OpenCorpseMessage msg, Supplier<NetworkEvent.Context> ctx) {
        OpenCorpse.handle(ctx.get().getSender(), msg.entityId(), msg.quickLoot());
    }
}
