package ru.wamior.cadaver.entity;

/**
 * Corpse container layout, split by section:
 * <pre>
 *  0..3   armor: helmet, chestplate, leggings, boots   (player slots 39, 38, 37, 36)
 *  4      offhand                                      (player slot 40)
 *  5..31  main inventory                               (player slots 9..35)
 *  32..40 hotbar                                       (player slots 0..8)
 *  41..76 extras: Curios first, then other modded drops
 * </pre>
 */
public final class CorpseLayout {
    public static final int ARMOR = 0;
    public static final int ARMOR_COUNT = 4;
    public static final int OFFHAND = 4;
    public static final int MAIN = 5;
    public static final int HOTBAR = 32;
    public static final int EXTRA = 41;
    public static final int EXTRA_COUNT = 36;
    public static final int SIZE = EXTRA + EXTRA_COUNT;

    /** Bumped when the layout changes; corpses saved without it use the old 54-slot chest layout. */
    public static final int VERSION = 2;

    /** Player inventory slot this corpse slot came from, or -1 for extras. */
    public static int playerSlot(int corpseIndex) {
        if (corpseIndex >= ARMOR && corpseIndex < ARMOR + ARMOR_COUNT) return 39 - corpseIndex;
        if (corpseIndex == OFFHAND) return 40;
        if (corpseIndex >= MAIN && corpseIndex < HOTBAR) return corpseIndex - MAIN + 9;
        if (corpseIndex >= HOTBAR && corpseIndex < EXTRA) return corpseIndex - HOTBAR;
        return -1;
    }

    /** Corpse slot for a player inventory slot (0..40), or -1. */
    public static int corpseIndex(int playerSlot) {
        if (playerSlot >= 0 && playerSlot <= 8) return HOTBAR + playerSlot;
        if (playerSlot >= 9 && playerSlot <= 35) return MAIN + playerSlot - 9;
        if (playerSlot >= 36 && playerSlot <= 39) return ARMOR + (39 - playerSlot);
        if (playerSlot == 40) return OFFHAND;
        return -1;
    }

    /** Player slot for an index of the old (v1) 54-slot layout, or -1 if it was an extra slot. */
    public static int legacyPlayerSlot(int oldIndex) {
        if (oldIndex >= 0 && oldIndex <= 3) return 39 - oldIndex;
        if (oldIndex == 4) return 40;
        if (oldIndex >= 9 && oldIndex <= 35) return oldIndex;
        if (oldIndex >= 36 && oldIndex <= 44) return oldIndex - 36;
        return -1;
    }

    private CorpseLayout() {
    }
}
