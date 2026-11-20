package ru.wamior.cadaver.version;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Client-only shims, Minecraft 1.20.5 – 1.20.6. */
public final class McClient {
    public static double interactionRange(Minecraft mc, LocalPlayer player) {
        return player.blockInteractionRange();
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
