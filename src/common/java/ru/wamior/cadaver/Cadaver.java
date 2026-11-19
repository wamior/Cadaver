package ru.wamior.cadaver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wamior.cadaver.platform.Platform;

/** Loader-independent entry point. Each loader's own entry class calls {@link #init()} once. */
public final class Cadaver {
    public static final String MODID = "cadaver";
    public static final Logger LOGGER = LoggerFactory.getLogger("Cadaver");

    public static void init() {
        // COMMON config lives in config/cadaver-common.toml and is re-read when the file changes
        CadaverConfig.init(Platform.configDir().resolve(MODID + "-common.toml"));
    }

    private Cadaver() {
    }
}
