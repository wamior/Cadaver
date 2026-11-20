package ru.wamior.cadaver.neoforge;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.wamior.cadaver.network.OpenCorpse;

/** NeoForge transport of {@link OpenCorpse}. */
public record OpenCorpsePayload(int entityId, boolean quickLoot) implements CustomPacketPayload {
    public static final Type<OpenCorpsePayload> TYPE = new Type<>(OpenCorpse.ID);

    public static final StreamCodec<ByteBuf, OpenCorpsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenCorpsePayload::entityId,
            ByteBufCodecs.BOOL, OpenCorpsePayload::quickLoot,
            OpenCorpsePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCorpsePayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) OpenCorpse.handle(player, msg.entityId(), msg.quickLoot());
        });
    }
}
