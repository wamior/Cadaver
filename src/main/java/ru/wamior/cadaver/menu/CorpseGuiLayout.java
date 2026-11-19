package ru.wamior.cadaver.menu;

/**
 * Slot and label positions of the corpse screen, shared by the menu (server + client) and the screen.
 * <pre>
 *  main panel (176 wide)                         side panel (only if there are extras)
 *  ┌──────────────────────────────────────┐ ┌──────────┐
 *  │ Corpse of X                          │ │ Curios   │
 *  │ Armor              Offhand           │ │ [][][]   │
 *  │ [][][][]           []                │ │ Other    │
 *  │ Inventory                            │ │ [][]     │
 *  │ 3×9 main + hotbar                    │ └──────────┘
 *  │ Inventory (viewer)                   │
 *  │ 3×9 + hotbar                         │
 *  └──────────────────────────────────────┘
 * </pre>
 */
public final class CorpseGuiLayout {
    public static final int MAIN_WIDTH = 176;
    public static final int MAIN_HEIGHT = 232;

    public static final int SECTION_LABEL_Y = 18;
    public static final int ARMOR_X = 8;
    public static final int ARMOR_Y = 28;
    public static final int OFFHAND_X = 98;
    public static final int INVENTORY_LABEL_Y = 50;
    public static final int MAIN_Y = 60;
    public static final int HOTBAR_Y = 118;
    public static final int PLAYER_LABEL_Y = 140;
    public static final int PLAYER_Y = 150;
    public static final int PLAYER_HOTBAR_Y = 208;

    private static final int SIDE_GAP = 2;
    private static final int SIDE_ROWS = 10;

    public final int curios;
    public final int others;
    public final int columns;
    public final int sideX;
    public final int sideWidth;
    public final int sideHeight;
    public final int curioLabelY;
    public final int curioY;
    public final int otherLabelY;
    public final int otherY;
    public final int width;
    public final int height;

    public CorpseGuiLayout(int curios, int others) {
        this.curios = Math.max(0, curios);
        this.others = Math.max(0, others);
        int total = this.curios + this.others;

        if (total == 0) {
            columns = 0;
            sideX = sideWidth = sideHeight = 0;
            curioLabelY = curioY = otherLabelY = otherY = 0;
            width = MAIN_WIDTH;
            height = MAIN_HEIGHT;
            return;
        }

        // at least 3 columns so the section titles ("Аксессуары") fit inside the panel
        columns = Math.max(3, Math.min(4, (total + SIDE_ROWS - 1) / SIDE_ROWS));
        sideX = MAIN_WIDTH + SIDE_GAP;
        sideWidth = 14 + columns * 18;

        int y = 6;
        if (this.curios > 0) {
            curioLabelY = y;
            curioY = y + 10;
            y = curioY + rows(this.curios) * 18 + 4;
        } else {
            curioLabelY = curioY = 0;
        }
        if (this.others > 0) {
            otherLabelY = y;
            otherY = y + 10;
            y = otherY + rows(this.others) * 18 + 4;
        } else {
            otherLabelY = otherY = 0;
        }
        sideHeight = y + 3;
        width = sideX + sideWidth;
        height = Math.max(MAIN_HEIGHT, sideHeight);
    }

    private int rows(int count) {
        return (count + columns - 1) / columns;
    }

    public int curioSlotX(int k) {
        return sideX + 8 + (k % columns) * 18;
    }

    public int curioSlotY(int k) {
        return curioY + (k / columns) * 18;
    }

    public int otherSlotX(int k) {
        return sideX + 8 + (k % columns) * 18;
    }

    public int otherSlotY(int k) {
        return otherY + (k / columns) * 18;
    }
}
