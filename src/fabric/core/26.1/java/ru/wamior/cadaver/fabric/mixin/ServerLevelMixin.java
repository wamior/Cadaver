package ru.wamior.cadaver.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.wamior.cadaver.fabric.FabricDeath;

/** Captures item drops while a player's death drops are being made (see {@link FabricDeath}). */
@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void cadaver$captureDeathDrop(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (FabricDeath.capture((ServerLevel) (Object) this, entity)) cir.setReturnValue(true);
    }
}
