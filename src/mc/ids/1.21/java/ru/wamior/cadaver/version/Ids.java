package ru.wamior.cadaver.version;

import net.minecraft.resources.ResourceLocation;
import ru.wamior.cadaver.Cadaver;

/** cadaver:&lt;path&gt; ids, Minecraft 1.21+. */
public final class Ids {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Cadaver.MODID, path);
    }

    private Ids() {
    }
}
