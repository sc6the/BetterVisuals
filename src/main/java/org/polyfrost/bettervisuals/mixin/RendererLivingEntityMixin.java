package org.polyfrost.bettervisuals.mixin;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class RendererLivingEntityMixin {
    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
    private void bettervisuals$hideNicknames(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        if (BetterVisualsConfig.noNicknames) {
            ci.cancel();
        }
    }
}
