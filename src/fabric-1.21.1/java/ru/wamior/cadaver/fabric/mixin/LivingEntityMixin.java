package ru.wamior.cadaver.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.wamior.cadaver.fabric.FabricDeath;

/** Wraps the player's death drops (see {@link FabricDeath}). Low priority so our RETURN hook runs late. */
@Mixin(value = LivingEntity.class, priority = 2000)
abstract class LivingEntityMixin {
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void cadaver$beginDeathDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) FabricDeath.begin(player);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void cadaver$endDeathDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) FabricDeath.end(player);
    }
}
