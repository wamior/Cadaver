package ru.wamior.cadaver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * COMMON config in {@code config/cadaver-common.toml}, the same file on every loader.
 * <p>
 * The format is the TOML subset NeoForge's ModConfigSpec writes (sections, booleans, integers,
 * string arrays, # comments), so files written by Cadaver 1.0.0 on NeoForge keep working.
 * The file is checked for changes at most once a second, so edits apply without a restart.
 */
public final class CadaverConfig {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static final Entry ENABLED = bool("general", "enabled", true,
            "Spawn a corpse when a player dies with items.");
    private static final Entry SEND_COORDINATES = bool("general", "sendCoordinates", true,
            "Tell the player where their corpse is.");
    private static final Entry UPDATE_NOTICE = bool("general", "updateNotice", true,
            "Client: show a chat message when a newer Cadaver version is available.");
    private static final Entry DIMENSION_BLACKLIST = list("general", "dimensionBlacklist",
            "Dimensions where no corpse is created (items drop as in vanilla). Example: [\"minecraft:the_end\"]");

    private static final Entry DESPAWN_MINUTES = integer("despawn", "despawnMinutes", 60, 0, 1_000_000,
            "Minutes of server game time until a corpse disappears. 0 = never.",
            "Counted from the moment of death, also while the chunk is unloaded:",
            "an overdue corpse is removed as soon as its chunk loads again.");
    private static final Entry DROP_ITEMS_ON_DESPAWN = bool("despawn", "dropItemsOnDespawn", true,
            "true = items fall on the ground when the corpse despawns, false = items are deleted.");
    private static final Entry REMOVE_WHEN_EMPTY = bool("despawn", "removeWhenEmpty", true,
            "Remove the corpse once all items have been taken.");

    private static final Entry OWNER_PROTECTION_MINUTES = integer("access", "ownerProtectionMinutes", 0, 0, 1_000_000,
            "For this many minutes after death only the owner (and operators) can open the corpse. 0 = anyone at any time.");
    private static final Entry QUICK_LOOT = bool("access", "quickLoot", true,
            "Sneak + right click takes everything at once. The owner gets items back into their original slots, armor included.");

    private static final long CHECK_INTERVAL_MS = 1000L;

    private static Path file;
    private static volatile Map<String, Object> values = defaults();
    private static volatile long lastModified = Long.MIN_VALUE;
    private static volatile long lastCheck;

    // ---------------------------------------------------------------- getters

    public static boolean enabled() {
        return (Boolean) get(ENABLED);
    }

    public static boolean sendCoordinates() {
        return (Boolean) get(SEND_COORDINATES);
    }

    public static boolean updateNotice() {
        return (Boolean) get(UPDATE_NOTICE);
    }

    @SuppressWarnings("unchecked")
    public static List<String> dimensionBlacklist() {
        return (List<String>) get(DIMENSION_BLACKLIST);
    }

    public static int despawnMinutes() {
        return (Integer) get(DESPAWN_MINUTES);
    }

    public static boolean dropItemsOnDespawn() {
        return (Boolean) get(DROP_ITEMS_ON_DESPAWN);
    }

    public static boolean removeWhenEmpty() {
        return (Boolean) get(REMOVE_WHEN_EMPTY);
    }

    public static int ownerProtectionMinutes() {
        return (Integer) get(OWNER_PROTECTION_MINUTES);
    }

    public static boolean quickLoot() {
        return (Boolean) get(QUICK_LOOT);
    }

    // ---------------------------------------------------------------- loading

    public static synchronized void init(Path path) {
        file = path;
        reload(true);
    }

    private static Object get(Entry entry) {
        long now = System.currentTimeMillis();
        if (file != null && now - lastCheck > CHECK_INTERVAL_MS) {
            lastCheck = now;
            reload(false);
        }
        return values.get(entry.key());
    }

    private static synchronized void reload(boolean force) {
        try {
            if (!Files.exists(file)) {
                values = defaults();
                write(values);
                lastModified = Files.getLastModifiedTime(file).toMillis();
                return;
            }
            long modified = Files.getLastModifiedTime(file).toMillis();
            if (!force && modified == lastModified) return;
            lastModified = modified;

            Map<String, String> raw = parse(Files.readAllLines(file, StandardCharsets.UTF_8));
            Map<String, Object> parsed = new LinkedHashMap<>();
            boolean complete = true;
            for (Entry entry : ENTRIES) {
                String text = raw.get(entry.key());
                Object value = text == null ? null : entry.parse(text);
                if (value == null) {
                    if (text != null) Cadaver.LOGGER.warn("Invalid value '{}' for {} in {}, using {}", text, entry.key(), file, entry.def());
                    complete = false;
                    value = entry.def();
                }
                parsed.put(entry.key(), value);
            }
            values = parsed;
            if (!complete && force) {
                // fill in keys added by newer versions, keeping the player's values
                write(parsed);
                lastModified = Files.getLastModifiedTime(file).toMillis();
            }
        } catch (IOException | RuntimeException e) {
            Cadaver.LOGGER.error("Could not read {}, keeping previous values", file, e);
        }
    }

    private static Map<String, String> parse(List<String> lines) {
        Map<String, String> raw = new LinkedHashMap<>();
        String section = "";
        for (String line : lines) {
            String s = stripComment(line).trim();
            if (s.isEmpty()) continue;
            if (s.startsWith("[") && s.endsWith("]") && !s.contains("=")) {
                section = s.substring(1, s.length() - 1).trim();
                continue;
            }
            int eq = s.indexOf('=');
            if (eq <= 0) continue;
            String key = s.substring(0, eq).trim();
            raw.put(section.isEmpty() ? key : section + "." + key, s.substring(eq + 1).trim());
        }
        return raw;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') quoted = !quoted;
            else if (c == '#' && !quoted) return line.substring(0, i);
        }
        return line;
    }

    private static void write(Map<String, Object> current) throws IOException {
        StringBuilder out = new StringBuilder();
        String section = null;
        for (Entry entry : ENTRIES) {
            if (!entry.section().equals(section)) {
                if (section != null) out.append('\n');
                section = entry.section();
                out.append('[').append(section).append("]\n");
            }
            for (String line : entry.comment()) out.append("\t#").append(line).append('\n');
            if (entry.min() != null) out.append("\t#Range: ").append(entry.min()).append(" ~ ").append(entry.max()).append('\n');
            out.append('\t').append(entry.name()).append(" = ").append(format(current.get(entry.key()))).append('\n');
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
    }

    private static String format(Object value) {
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object o : list) parts.add('"' + o.toString().replace("\\", "\\\\").replace("\"", "\\\"") + '"');
            return "[" + String.join(", ", parts) + "]";
        }
        return String.valueOf(value);
    }

    private static Map<String, Object> defaults() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) map.put(entry.key(), entry.def());
        return map;
    }

    // ---------------------------------------------------------------- entries

    private enum Kind { BOOL, INT, LIST }

    private record Entry(String section, String name, Kind kind, Object def, Integer min, Integer max, String[] comment) {
        String key() {
            return section + "." + name;
        }

        Object parse(String text) {
            switch (kind) {
                case BOOL -> {
                    if (text.equalsIgnoreCase("true")) return Boolean.TRUE;
                    if (text.equalsIgnoreCase("false")) return Boolean.FALSE;
                    return null;
                }
                case INT -> {
                    try {
                        long v = Long.parseLong(text.replace("_", ""));
                        return (int) Math.max(min, Math.min(max, v));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                default -> {
                    if (!text.startsWith("[") || !text.endsWith("]")) return null;
                    List<String> list = new ArrayList<>();
                    String body = text.substring(1, text.length() - 1);
                    StringBuilder cur = null;
                    for (int i = 0; i < body.length(); i++) {
                        char c = body.charAt(i);
                        if (cur == null) {
                            if (c == '"') cur = new StringBuilder();
                        } else if (c == '\\' && i + 1 < body.length()) {
                            cur.append(body.charAt(++i));
                        } else if (c == '"') {
                            list.add(cur.toString());
                            cur = null;
                        } else {
                            cur.append(c);
                        }
                    }
                    return List.copyOf(list);
                }
            }
        }
    }

    private static Entry bool(String section, String name, boolean def, String... comment) {
        return add(new Entry(section, name, Kind.BOOL, def, null, null, comment));
    }

    private static Entry integer(String section, String name, int def, int min, int max, String... comment) {
        return add(new Entry(section, name, Kind.INT, def, min, max, comment));
    }

    private static Entry list(String section, String name, String... comment) {
        return add(new Entry(section, name, Kind.LIST, List.of(), null, null, comment));
    }

    private static Entry add(Entry entry) {
        ENTRIES.add(entry);
        return entry;
    }

    private CadaverConfig() {
    }
}
