package ru.wamior.cadaver.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import ru.wamior.cadaver.network.OpenCorpse;

/** NeoForge 1.20.4 transport of {@link OpenCorpse}. */
public record OpenCorpsePayload(int entityId, boolean quickLoot) implements CustomPacketPayload {
    public OpenCorpsePayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(quickLoot);
    }

    @Override
    public ResourceLocation id() {
        return OpenCorpse.ID;
    }

    public static void handle(OpenCorpsePayload msg, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> ctx.player().ifPresent(player -> {
            if (player instanceof ServerPlayer serverPlayer) OpenCorpse.handle(serverPlayer, msg.entityId(), msg.quickLoot());
        }));
    }
}
