package org.polyfrost.bettervisuals.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.polyfrost.bettervisuals.features.SkinForceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    private boolean bettervisuals$isSelf() {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        EntityPlayer mcp = Minecraft.getMinecraft().thePlayer;
        return mcp != null && self.getUniqueID().equals(mcp.getUniqueID());
    }

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void bettervisuals$getLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if (!BetterVisualsConfig.skinForce) return;
        if (!bettervisuals$isSelf()) return;
        ResourceLocation loc = SkinForceManager.INSTANCE.getSkin();
        if (loc != null) {
            cir.setReturnValue(loc);
        }
    }

    @Inject(method = "getSkinType()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void bettervisuals$getSkinType(CallbackInfoReturnable<String> cir) {
        if (!BetterVisualsConfig.skinForce) return;
        if (!bettervisuals$isSelf()) return;
        cir.setReturnValue(BetterVisualsConfig.skinSlim ? "slim" : "default");
    }
}
