package ru.wamior.cadaver.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static EntityType<CorpseEntity> CORPSE;
    public static MenuType<CorpseMenu> CORPSE_MENU;

    @Override
    public void onInitialize() {
        Cadaver.init();
        CORPSE = Registry.register(BuiltInRegistries.ENTITY_TYPE, Ids.id(CorpseTypes.CORPSE), CorpseTypes.createCorpseType());
        CORPSE_MENU = Registry.register(BuiltInRegistries.MENU, Ids.id(CorpseTypes.CORPSE),
                // extra menu-open data: section sizes, so the client builds the same slots as the server
                new ExtendedScreenHandlerType<>((id, inv, buf) -> CorpseMenu.client(id, inv, buf.readVarInt(), buf.readVarInt())));

        ServerPlayNetworking.registerGlobalReceiver(OpenCorpse.ID, (server, player, handler, buf, sender) -> {
            int entityId = buf.readVarInt();
            boolean quickLoot = buf.readBoolean();
            server.execute(() -> OpenCorpse.handle(player, entityId, quickLoot));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> FabricDeath.flushStale());
    }
}
