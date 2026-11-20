package ru.wamior.cadaver.fabric;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.entity.CorpseTypes;
import ru.wamior.cadaver.menu.CorpseMenu;
import ru.wamior.cadaver.network.OpenCorpse;
import ru.wamior.cadaver.version.Ids;
import ru.wamior.cadaver.version.Mc;

public final class CadaverFabric implements ModInitializer {
    /** Extra menu-open data: section sizes, so the client builds the same slots as the server. */
    public record MenuData(int curios, int others) {
        public static final StreamCodec<ByteBuf, MenuData> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MenuData::curios,
                ByteBufCodecs.VAR_INT, MenuData::others,
                MenuData::new);
    }

    public static EntityType<CorpseEntity> CORPSE;
    public static MenuType<CorpseMenu> CORPSE_MENU;

    @Override
    public void onInitialize() {
        Cadaver.init();
        CORPSE = Registry.register(BuiltInRegistries.ENTITY_TYPE, Ids.id(CorpseTypes.CORPSE), CorpseTypes.createCorpseType());
        CORPSE_MENU = Registry.register(BuiltInRegistries.MENU, Ids.id(CorpseTypes.CORPSE),
                new ExtendedMenuType<>((id, inv, data) -> CorpseMenu.client(id, inv, data.curios(), data.others()), MenuData.CODEC));

        PayloadTypeRegistry.serverboundPlay().register(OpenCorpsePayload.TYPE, OpenCorpsePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenCorpsePayload.TYPE, (payload, context) ->
                context.server().execute(() -> OpenCorpse.handle(context.player(), payload.entityId(), payload.quickLoot())));

        ServerTickEvents.END_SERVER_TICK.register(server -> FabricDeath.flushStale());
    }
}
