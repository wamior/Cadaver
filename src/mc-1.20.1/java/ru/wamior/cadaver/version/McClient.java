package ru.wamior.cadaver.version;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;

/** Client-only Minecraft 1.20.1 shims. */
public final class McClient {
    /** The game mode's pick range (Forge patches it to follow the reach attribute). */
    public static double interactionRange(Minecraft mc, LocalPlayer player) {
        return mc.gameMode != null ? mc.gameMode.getPickRange() : 4.5D;
    }

    /** In 1.20.1 container screens draw the dimmed background themselves. */
    public static void renderScreenBackground(Screen screen, GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        screen.renderBackground(g);
    }

    public static void renderModel(Model model, PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        model.renderToBuffer(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private McClient() {
    }
}
