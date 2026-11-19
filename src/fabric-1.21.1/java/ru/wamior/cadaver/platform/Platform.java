package ru.wamior.cadaver.platform;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.fabric.CadaverFabric;
import ru.wamior.cadaver.menu.CorpseMenu;

import java.nio.file.Path;

/** Fabric 1.21.1 implementation of the loader hooks used by the shared code. */
public final class Platform {
    public static EntityType<CorpseEntity> corpseType() {
        return CadaverFabric.CORPSE;
    }

    public static MenuType<CorpseMenu> corpseMenuType() {
        return CadaverFabric.CORPSE_MENU;
    }

    public static void openCorpseMenu(ServerPlayer player, Component title, Container corpse, int curios, int others) {
        player.openMenu(new ExtendedScreenHandlerFactory<CadaverFabric.MenuData>() {
            @Override
            public CadaverFabric.MenuData getScreenOpeningData(ServerPlayer p) {
                return new CadaverFabric.MenuData(curios, others);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player p) {
                return new CorpseMenu(id, playerInv, corpse, curios, others);
            }
        });
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    private Platform() {
    }
}
