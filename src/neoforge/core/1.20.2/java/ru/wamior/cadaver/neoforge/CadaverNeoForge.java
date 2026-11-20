package ru.wamior.cadaver.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import ru.wamior.cadaver.Cadaver;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.entity.CorpseTypes;
import ru.wamior.cadaver.event.DeathHandler;
import ru.wamior.cadaver.menu.CorpseMenu;
import ru.wamior.cadaver.version.Ids;
import ru.wamior.cadaver.version.Mc;

@Mod(Cadaver.MODID)
public final class CadaverNeoForge {
    private static final String PROTOCOL = "1";

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Cadaver.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cadaver.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CorpseEntity>> CORPSE =
            ENTITY_TYPES.register(CorpseTypes.CORPSE, CorpseTypes::createCorpseType);
    public static final DeferredHolder<MenuType<?>, MenuType<CorpseMenu>> CORPSE_MENU = MENUS.register(CorpseTypes.CORPSE,
            () -> IMenuTypeExtension.create((id, inv, buf) -> CorpseMenu.client(id, inv, buf.readVarInt(), buf.readVarInt())));

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Ids.id("main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public CadaverNeoForge() {
        Cadaver.init();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);

        CHANNEL.messageBuilder(OpenCorpseMessage.class, 0, PlayNetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenCorpseMessage::encode)
                .decoder(OpenCorpseMessage::decode)
                .consumerMainThread(OpenCorpseMessage::handle)
                .add();

        // LOWEST: by now vanilla and other mods have decided what really drops (vanishing, keepInventory, soulbound)
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) DeathHandler.snapshot(player);
        });
        bus.addListener(EventPriority.LOWEST, false, LivingDropsEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) DeathHandler.onDrops(player, event.getDrops());
        });
        bus.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class,
                event -> DeathHandler.forget(event.getEntity().getUUID()));
        bus.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedOutEvent.class,
                event -> DeathHandler.forget(event.getEntity().getUUID()));
    }
}
