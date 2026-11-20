package ru.wamior.cadaver.version;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

/** Client-only shims, Minecraft 26.3+. */
public final class McClient {
    public static double interactionRange(Minecraft mc, LocalPlayer player) {
        return player.blockInteractionRange();
    }

    public static void renderModel(Model model, PoseStack pose, VertexConsumer consumer, int light, int overlay) {
        model.renderToBuffer(pose, consumer, light, overlay, -1);
    }

    public static void swingMainHand(LocalPlayer player) {
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
    }

    private McClient() {
    }
}
