package ru.wamior.cadaver.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.forge.CadaverForge;
import ru.wamior.cadaver.menu.CorpseMenu;

import java.nio.file.Path;

/** Forge 1.20.1 implementation of the loader hooks used by the shared code. */
public final class Platform {
    public static EntityType<CorpseEntity> corpseType() {
        return CadaverForge.CORPSE.get();
    }

    public static MenuType<CorpseMenu> corpseMenuType() {
        return CadaverForge.CORPSE_MENU.get();
    }

    public static void openCorpseMenu(ServerPlayer player, Component title, Container corpse, int curios, int others) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, playerInv, p) -> new CorpseMenu(id, playerInv, corpse, curios, others), title),
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
