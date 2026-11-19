package ru.wamior.cadaver.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import ru.wamior.cadaver.menu.CorpseGuiLayout;
import ru.wamior.cadaver.menu.CorpseMenu;

/** Vanilla-styled corpse screen, drawn in code (no texture), with a side panel for Curios / other drops. */
public class CorpseScreen extends AbstractContainerScreen<CorpseMenu> {
    private static final int LABEL = 0xFF404040;
    private static final Component ARMOR = Component.translatable("gui.cadaver.armor");
    private static final Component OFFHAND = Component.translatable("gui.cadaver.offhand");
    private static final Component INVENTORY = Component.translatable("gui.cadaver.inventory");
    private static final Component CURIOS = Component.translatable("gui.cadaver.curios");
    private static final Component OTHER = Component.translatable("gui.cadaver.other");

    private final CorpseGuiLayout layout;

    public CorpseScreen(CorpseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.layout = menu.layout();
        this.imageWidth = layout.width;
        this.imageHeight = layout.height;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = CorpseGuiLayout.PLAYER_LABEL_Y;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        panel(g, leftPos, topPos, CorpseGuiLayout.MAIN_WIDTH, CorpseGuiLayout.MAIN_HEIGHT);
        if (layout.sideWidth > 0) {
            panel(g, leftPos + layout.sideX, topPos, layout.sideWidth, layout.sideHeight);
        }
        for (Slot slot : menu.slots) {
            slot(g, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        g.drawString(font, ARMOR, CorpseGuiLayout.ARMOR_X, CorpseGuiLayout.SECTION_LABEL_Y, LABEL, false);
        g.drawString(font, OFFHAND, CorpseGuiLayout.OFFHAND_X, CorpseGuiLayout.SECTION_LABEL_Y, LABEL, false);
        g.drawString(font, INVENTORY, 8, CorpseGuiLayout.INVENTORY_LABEL_Y, LABEL, false);
        if (layout.curios > 0) {
            g.drawString(font, CURIOS, layout.sideX + 7, layout.curioLabelY, LABEL, false);
        }
        if (layout.others > 0) {
            g.drawString(font, OTHER, layout.sideX + 7, layout.otherLabelY, LABEL, false);
        }
    }

    /** Vanilla window look: black outline with cut corners, white top-left bevel, gray bottom-right bevel. */
    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        int black = 0xFF000000, light = 0xFFFFFFFF, face = 0xFFC6C6C6, shade = 0xFF555555;
        g.fill(x + 2, y, x + w - 2, y + 1, black);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, black);
        g.fill(x, y + 2, x + 1, y + h - 2, black);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, black);
        g.fill(x + 1, y + 1, x + 2, y + 2, black);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, black);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, black);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, black);

        g.fill(x + 1, y + 2, x + w - 1, y + h - 2, face);
        g.fill(x + 2, y + 1, x + w - 2, y + h - 1, face);

        g.fill(x + 2, y + 1, x + w - 3, y + 3, light);
        g.fill(x + 1, y + 2, x + 3, y + h - 3, light);
        g.fill(x + 3, y + h - 3, x + w - 2, y + h - 1, shade);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 2, shade);
    }

    /** Vanilla slot: dark top-left edge, white bottom-right edge, gray inside. */
    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        g.fill(x, y, x + 17, y + 17, 0xFFFFFFFF);
        g.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }
}
