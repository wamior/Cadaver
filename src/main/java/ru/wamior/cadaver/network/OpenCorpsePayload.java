package ru.wamior.cadaver.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.entity.CorpseEntity;

/** Client → server: "I right-clicked this corpse". Needed because the corpse is not pickable. */
public record OpenCorpsePayload(int entityId, boolean quickLoot) implements CustomPacketPayload {
    public static final Type<OpenCorpsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Cadaver.MODID, "open_corpse"));

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
            if (!(ctx.player() instanceof ServerPlayer player) || player.isSpectator() || !player.isAlive()) return;
            Entity entity = player.level().getEntity(msg.entityId());
            if (!(entity instanceof CorpseEntity corpse) || !corpse.isAlive()) return;
            double reach = player.blockInteractionRange() + 2.0D;
            if (corpse.getInteractionBox().distanceToSqr(player.getEyePosition()) > reach * reach) return;
            corpse.interact(player, msg.quickLoot());
        });
    }
}
