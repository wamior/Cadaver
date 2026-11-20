package ru.wamior.cadaver.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Pose of a lying corpse, shared by every renderer version. */
public final class CorpsePose {
    /** Slightly spread limbs so the pose reads as "fallen", not "standing" (radians). */
    public static final float ARM_Z = 0.25F;
    public static final float LEG_Z = 0.08F;
    public static final float HEAD_X = -0.15F;

    /** Lays the player model on its back, face up, centred on the entity position. */
    public static void transform(PoseStack pose, float yRot, float scale) {
        pose.translate(0.0D, 0.13D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));      // fall on the back, face up
        pose.translate(0.0D, -0.85D, 0.0D);                 // center the body on the entity position
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.scale(scale, scale, scale);
        pose.translate(0.0D, -1.501D, 0.0D);
    }

    private CorpsePose() {
    }
}
