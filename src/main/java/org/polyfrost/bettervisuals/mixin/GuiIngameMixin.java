package org.polyfrost.bettervisuals.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.polyfrost.bettervisuals.features.HotbarRenderer;
import org.polyfrost.bettervisuals.utils.BlurUtil;
import org.polyfrost.bettervisuals.utils.RenderUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos,
                                             float partialTicks, EntityPlayer player);

    @Shadow @Final protected Minecraft mc;
    @Shadow private int remainingHighlightTicks;
    @Shadow private ItemStack highlightingItemStack;

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void bettervisuals_replaceHotbar(ScaledResolution res, float partialTicks,
                                             CallbackInfo ci) {
        if (!BetterVisualsConfig.enabled) return;
        ci.cancel();
        if (!(mc.getRenderViewEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        int ox = BetterVisualsConfig.hotbarX;
        int oy = BetterVisualsConfig.hotbarY;

        // Draw custom hotbar background + highlight
        HotbarRenderer.INSTANCE.render();

        // Render hotbar items on top
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        for (int slot = 0; slot < 9; ++slot) {
            int x = res.getScaledWidth() / 2 - 91 + slot * 20 + 3 + ox;
            int y = res.getScaledHeight() - 16 - 3 - BetterVisualsConfig.bottomOffset + oy;
            renderHotbarItem(slot, x, y, partialTicks, player);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();

        // Held item name (vanilla style)
        bettervisuals$drawHeldItemName(res, oy);
    }

    @Unique
    private void bettervisuals$drawHeldItemName(ScaledResolution res, int oy) {
        if (remainingHighlightTicks <= 0 || highlightingItemStack == null) return;
        if (highlightingItemStack.getItem() == null) return;

        String name = highlightingItemStack.getDisplayName();
        if (name == null || name.isEmpty()) return;

        int opacity = (int) ((float) remainingHighlightTicks * 256.0F / 10.0F);
        if (opacity > 255) opacity = 255;
        if (opacity <= 4) return;

        FontRenderer fr = mc.fontRendererObj;
        int x = (res.getScaledWidth() - fr.getStringWidth(name)) / 2 + BetterVisualsConfig.heldItemX;
        int y = res.getScaledHeight() - 59 + oy + BetterVisualsConfig.heldItemY;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (BetterVisualsConfig.heldItemBg) {
            float bgX = x - 4f;
            float bgY = y - 2f;
            float bgW = fr.getStringWidth(name) + 8f;
            float bgH = 12f;
            float r = BetterVisualsConfig.heldItemRadius;
            java.awt.Color raw = BetterVisualsConfig.heldItemBgColor.toJavaColor();
            int bgAlpha = Math.min((int) (raw.getAlpha() * opacity / 255f), 255);
            java.awt.Color adjBg = new java.awt.Color(raw.getRed(), raw.getGreen(), raw.getBlue(), Math.max(bgAlpha, 1));

            if (BetterVisualsConfig.blurEnabled) {
                BlurUtil.INSTANCE.drawBlurredRect(bgX, bgY, bgW, bgH, BetterVisualsConfig.blurRadius);
            }
            RenderUtil.INSTANCE.drawRoundedRect(bgX, bgY, bgW, bgH, r, adjBg);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }

        fr.drawStringWithShadow(name, x, y, 0xFFFFFF | (opacity << 24));
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
