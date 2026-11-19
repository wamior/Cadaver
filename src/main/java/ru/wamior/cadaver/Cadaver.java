package ru.wamior.cadaver;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.wamior.cadaver.network.OpenCorpsePayload;

@Mod(Cadaver.MODID)
public final class Cadaver {
    public static final String MODID = "cadaver";

    public Cadaver(IEventBus modBus, ModContainer container) {
        ModEntities.ENTITY_TYPES.register(modBus);
        ModMenus.MENUS.register(modBus);
        modBus.addListener(Cadaver::registerPayloads);
        // COMMON config lives in config/cadaver-common.toml and is re-read every check, so edits apply live.
        container.registerConfig(ModConfig.Type.COMMON, CadaverConfig.SPEC);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(OpenCorpsePayload.TYPE, OpenCorpsePayload.STREAM_CODEC, OpenCorpsePayload::handle);
    }
}
