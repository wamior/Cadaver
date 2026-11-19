package ru.wamior.cadaver.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.entity.CorpseTypes;
import ru.wamior.cadaver.event.DeathHandler;
import ru.wamior.cadaver.menu.CorpseMenu;

@Mod(Cadaver.MODID)
public final class CadaverNeoForge {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Cadaver.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cadaver.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CorpseEntity>> CORPSE =
            ENTITY_TYPES.register(CorpseTypes.CORPSE, CorpseTypes::createCorpseType);
    public static final DeferredHolder<MenuType<?>, MenuType<CorpseMenu>> CORPSE_MENU = MENUS.register(CorpseTypes.CORPSE,
            () -> IMenuTypeExtension.create((id, inv, buf) -> CorpseMenu.client(id, inv, buf.readVarInt(), buf.readVarInt())));

    public CadaverNeoForge(IEventBus modBus) {
        Cadaver.init();
        ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(CadaverNeoForge::registerPayloads);

        // LOWEST: by now vanilla and other mods have decided what really drops (vanishing, keepInventory, soulbound)
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LivingDeathEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) DeathHandler.snapshot(player);
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LivingDropsEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) DeathHandler.onDrops(player, event.getDrops());
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> DeathHandler.forget(event.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> DeathHandler.forget(event.getEntity().getUUID()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(OpenCorpsePayload.TYPE, OpenCorpsePayload.STREAM_CODEC, OpenCorpsePayload::handle);
    }
}
