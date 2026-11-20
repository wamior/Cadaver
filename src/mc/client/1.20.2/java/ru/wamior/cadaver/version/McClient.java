package ru.wamior.cadaver.version;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Client-only shims, Minecraft 1.20.2 – 1.20.4. */
public final class McClient {
    /** The game mode's pick range (Forge patches it to follow the reach attribute). */
    public static double interactionRange(Minecraft mc, LocalPlayer player) {
        return mc.gameMode != null ? mc.gameMode.getPickRange() : 4.5D;
    }

    /** 1.20.2+ draws the dimmed background inside Screen#render already. */
    public static void renderScreenBackground(Screen screen, GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    public static void renderModel(Model model, PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        model.renderToBuffer(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void swingMainHand(LocalPlayer player) {
        player.swing(InteractionHand.MAIN_HAND);
    }

    private McClient() {
    }
}
