package ru.wamior.cadaver.compat;

import ru.wamior.cadaver.Cadaver;

/** Logs an incompatible accessory mod version once instead of crashing the death handling. */
final class AccessoriesFailure {
    private static boolean reported;

    static void report(Throwable e) {
        if (reported) return;
        reported = true;
        Cadaver.LOGGER.error("Accessory mod integration failed, accessories drop as normal items from now on", e);
    }

    private AccessoriesFailure() {
    }
}
