package ru.wamior.cadaver.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import ru.wamior.cadaver.entity.CorpseEntity;

/** Draws the owner's player model lying on its back, with their skin. */
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
        model.rightArm.zRot = 0.25F;
        model.rightSleeve.zRot = 0.25F;
        model.leftArm.zRot = -0.25F;
        model.leftSleeve.zRot = -0.25F;
        model.rightLeg.zRot = 0.08F;
        model.rightPants.zRot = 0.08F;
        model.leftLeg.zRot = -0.08F;
        model.leftPants.zRot = -0.08F;
        model.head.xRot = -0.15F;
        model.hat.xRot = -0.15F;
        return model;
    }

    @Override
    public void render(CorpseEntity corpse, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        PlayerSkin skin = CorpseSkins.get(corpse);
        PlayerModel<LivingEntity> model = skin.model() == PlayerSkin.Model.SLIM ? slim : wide;

        pose.pushPose();
        pose.translate(0.0D, 0.13D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - corpse.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));      // fall on the back, face up
        pose.translate(0.0D, -0.85D, 0.0D);                 // center the body on the entity position
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
        pose.translate(0.0D, -1.501D, 0.0D);
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(skin.texture())), light, OverlayTexture.NO_OVERLAY, -1);
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
