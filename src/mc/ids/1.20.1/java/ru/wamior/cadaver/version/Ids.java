package ru.wamior.cadaver.version;

import net.minecraft.resources.ResourceLocation;
import ru.wamior.cadaver.Cadaver;

/** cadaver:&lt;path&gt; ids, Minecraft 1.20.1 – 1.20.6. */
public final class Ids {
    public static ResourceLocation id(String path) {
        return new ResourceLocation(Cadaver.MODID, path);
    }

    private Ids() {
    }
}
