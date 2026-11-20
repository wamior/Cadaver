package ru.wamior.cadaver.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import ru.wamior.cadaver.entity.CorpseEntity;
import ru.wamior.cadaver.version.McClient;

/** Draws the owner's player model lying on its back, with their skin (Minecraft 1.21.2 – 1.21.8 render states). */
public class CorpseRenderer extends EntityRenderer<CorpseEntity, CorpseRenderer.State> {
    private static final float PLAYER_SCALE = 0.9375F;

    public static class State extends EntityRenderState {
        public ResourceLocation texture;
        public boolean slim;
        public float yRot;
    }

    private final PlayerModel wide;
    private final PlayerModel slim;

    public CorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.wide = pose(new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false));
        this.slim = pose(new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true));
        this.shadowRadius = 0.6F;
        this.shadowStrength = 0.6F;
    }

    private static PlayerModel pose(PlayerModel model) {
        model.setAllVisible(true);
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
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CorpseEntity corpse, State state, float partialTick) {
        super.extractRenderState(corpse, state, partialTick);
        CorpseSkins.Skin skin = CorpseSkins.get(corpse);
        state.texture = skin.texture();
        state.slim = skin.slim();
        state.yRot = corpse.getYRot();
    }

    @Override
    public void render(State state, PoseStack pose, MultiBufferSource buffers, int light) {
        PlayerModel model = state.slim ? slim : wide;
        pose.pushPose();
        CorpsePose.transform(pose, state.yRot, PLAYER_SCALE);
        McClient.renderModel(model, pose, buffers.getBuffer(model.renderType(state.texture)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        // no super.render(): it would draw the name tag, the corpse shows no text at all
    }

    @Override
    protected boolean shouldShowName(CorpseEntity corpse, double distanceSq) {
        return false;
    }
}
