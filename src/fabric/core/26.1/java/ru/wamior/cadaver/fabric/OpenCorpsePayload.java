package ru.wamior.cadaver.fabric;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import ru.wamior.cadaver.network.OpenCorpse;

/** Fabric transport of {@link OpenCorpse}. */
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
}
