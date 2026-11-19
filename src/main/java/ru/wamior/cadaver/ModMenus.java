package ru.wamior.cadaver;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.wamior.cadaver.menu.CorpseMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cadaver.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CorpseMenu>> CORPSE =
            MENUS.register("corpse", () -> IMenuTypeExtension.create(CorpseMenu::client));

    private ModMenus() {
    }
}
