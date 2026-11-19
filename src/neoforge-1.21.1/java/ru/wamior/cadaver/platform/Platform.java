package ru.wamior.cadaver.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.menu.CorpseMenu;
import ru.wamior.cadaver.neoforge.CadaverNeoForge;

import java.nio.file.Path;

/** NeoForge 1.21.1 implementation of the loader hooks used by the shared code. */
public final class Platform {
    public static EntityType<CorpseEntity> corpseType() {
        return CadaverNeoForge.CORPSE.get();
    }

    public static MenuType<CorpseMenu> corpseMenuType() {
        return CadaverNeoForge.CORPSE_MENU.get();
    }

    public static void openCorpseMenu(ServerPlayer player, Component title, Container corpse, int curios, int others) {
        player.openMenu(new SimpleMenuProvider((id, playerInv, p) -> new CorpseMenu(id, playerInv, corpse, curios, others), title),
                buf -> {
                    buf.writeVarInt(curios);
                    buf.writeVarInt(others);
                });
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private Platform() {
    }
}
