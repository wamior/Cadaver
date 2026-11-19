package ru.wamior.cadaver.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.wamior.cadaver.client.CorpseTargeting;

/** Right click on a corpse (it is not pickable, so vanilla can't target it). Fabric twin of NeoForge's InteractionKeyMappingTriggered. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void cadaver$openCorpse(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.gameMode == null || mc.gameMode.isDestroying() || mc.player == null || mc.player.isHandsBusy()) return;
        if (CorpseTargeting.tryOpen()) {
            rightClickDelay = 4;
            mc.player.swing(InteractionHand.MAIN_HAND);
            ci.cancel();
        }
    }
}
