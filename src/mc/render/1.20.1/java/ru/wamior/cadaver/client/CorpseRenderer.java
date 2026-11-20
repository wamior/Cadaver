package ru.wamior.cadaver.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.version.McClient;

/** Draws the owner's player model lying on its back, with their skin (Minecraft 1.20.1 – 1.21.1). */
public class CorpseRenderer extends EntityRenderer<CorpseEntity> {
    private static final float PLAYER_SCALE = 0.9375F;

    private final PlayerModel<LivingEntity> wide;
    private final PlayerModel<LivingEntity> slim;

    public CorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.wide = createModel(context, false);
        this.slim = createModel(context, true);
        this.shadowRadius = 0.6F;
        this.shadowStrength = 0.6F;
    }

    private static PlayerModel<LivingEntity> createModel(EntityRendererProvider.Context context, boolean slim) {
        PlayerModel<LivingEntity> model = new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
        model.young = false;
        model.setAllVisible(true);
        // slightly spread limbs so the pose reads as "fallen", not "standing"
        model.rightArm.zRot = CorpsePose.ARM_Z;
        model.rightSleeve.zRot = CorpsePose.ARM_Z;
        model.leftArm.zRot = -CorpsePose.ARM_Z;
        model.leftSleeve.zRot = -CorpsePose.ARM_Z;
        model.rightLeg.zRot = CorpsePose.LEG_Z;
        model.rightPants.zRot = CorpsePose.LEG_Z;
        model.leftLeg.zRot = -CorpsePose.LEG_Z;
        model.leftPants.zRot = -CorpsePose.LEG_Z;
        model.head.xRot = CorpsePose.HEAD_X;
        model.hat.xRot = CorpsePose.HEAD_X;
        return model;
    }

    @Override
    public void render(CorpseEntity corpse, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        CorpseSkins.Skin skin = CorpseSkins.get(corpse);
        PlayerModel<LivingEntity> model = skin.slim() ? slim : wide;

        pose.pushPose();
        CorpsePose.transform(pose, corpse.getYRot(), PLAYER_SCALE);
        McClient.renderModel(model, pose, buffers.getBuffer(model.renderType(skin.texture())), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        // no super.render(): it would draw the name tag (and the leash/fire), the corpse shows no text at all
    }

    @Override
    protected boolean shouldShowName(CorpseEntity corpse) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseEntity corpse) {
        return CorpseSkins.get(corpse).texture();
    }
}
