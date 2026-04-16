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
import org.polyfrost.bettervisuals.utils.GuiScaleBypass;
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

        // Compute vanilla dimensions (unaffected by GuiScaleMod) for positioning.
        GuiScaleBypass.INSTANCE.enable();
        ScaledResolution vanilla = new ScaledResolution(mc);
        GuiScaleBypass.INSTANCE.disable();

        // Draw custom hotbar background + highlight (handles its own matrix)
        HotbarRenderer.INSTANCE.render();

        // Render hotbar items on top, compensating for GuiScaleMod's compressed ortho.
        GlStateManager.pushMatrix();
        float gsMul = GuiScaleBypass.INSTANCE.multiplier();
        if (gsMul != 1.0f) {
            float inv = 1.0f / gsMul;
            GlStateManager.scale(inv, inv, 1.0f);
        }
        // Apply BV's HUD scale around hotbar bottom-center pivot.
        float hs = BetterVisualsConfig.hudScale;
        if (hs != 1.0f) {
            float px = vanilla.getScaledWidth() / 2.0f;
            float py = vanilla.getScaledHeight();
            GlStateManager.translate(px, py, 0.0f);
            GlStateManager.scale(hs, hs, 1.0f);
            GlStateManager.translate(-px, -py, 0.0f);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        for (int slot = 0; slot < 9; ++slot) {
            int x = vanilla.getScaledWidth() / 2 - 91 + slot * 20 + 3 + ox;
            int y = vanilla.getScaledHeight() - 16 - 3 - BetterVisualsConfig.bottomOffset + oy;
            renderHotbarItem(slot, x, y, partialTicks, player);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();

        // Slot numbers (top-right of each slot)
        if (BetterVisualsConfig.slotNumbersEnabled) {
            bettervisuals$drawSlotNumbers(vanilla, ox, oy);
        }

        // Held item name (vanilla style)
        bettervisuals$drawHeldItemName(vanilla, oy);

        GlStateManager.popMatrix();
    }

    @Unique
    private void bettervisuals$drawSlotNumbers(ScaledResolution res, int ox, int oy) {
        FontRenderer fr = mc.fontRendererObj;
        float scale = BetterVisualsConfig.slotNumberScale;
        int nx = BetterVisualsConfig.slotNumberX;
        int ny = BetterVisualsConfig.slotNumberY;
        java.awt.Color numCol = BetterVisualsConfig.slotNumberColor.toJavaColor();
        int numArgb = (numCol.getAlpha() << 24) | (numCol.getRed() << 16)
                | (numCol.getGreen() << 8) | numCol.getBlue();

        boolean glow = BetterVisualsConfig.slotNumberGlow;
        java.awt.Color glowCol = BetterVisualsConfig.slotNumberGlowColor.toJavaColor();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableDepth();

        for (int slot = 0; slot < 9; ++slot) {
            String label = String.valueOf(slot + 1);
            // Slot box top-left in vanilla coords
            int slotX = res.getScaledWidth() / 2 - 91 + slot * 20 + ox;
            int slotY = res.getScaledHeight() - 22 - BetterVisualsConfig.bottomOffset + oy;

            int strW = fr.getStringWidth(label);
            // Top-right corner of the slot (slot is ~20 wide, number scale shrinks text)
            float tx = slotX + 20 - strW * scale - 1 + nx;
            float ty = slotY + 1 + ny;

            GlStateManager.pushMatrix();
            GlStateManager.translate(tx, ty, 0f);
            GlStateManager.scale(scale, scale, 1f);

            if (glow) {
                int gArgb = (glowCol.getAlpha() << 24) | (glowCol.getRed() << 16)
                        | (glowCol.getGreen() << 8) | glowCol.getBlue();
                // Soft halo: draw at 8 offsets with reduced alpha
                int halfA = (glowCol.getAlpha() / 3) & 0xFF;
                int haloArgb = (halfA << 24) | (glowCol.getRed() << 16)
                        | (glowCol.getGreen() << 8) | glowCol.getBlue();
                float[][] offs = { {-1,0}, {1,0}, {0,-1}, {0,1}, {-1,-1}, {1,-1}, {-1,1}, {1,1} };
                for (float[] o : offs) {
                    fr.drawString(label, o[0], o[1], haloArgb, false);
                }
                // Core outline
                fr.drawString(label, -1, 0, gArgb, false);
                fr.drawString(label, 1, 0, gArgb, false);
                fr.drawString(label, 0, -1, gArgb, false);
                fr.drawString(label, 0, 1, gArgb, false);
            }

            fr.drawStringWithShadow(label, 0f, 0f, numArgb);
            GlStateManager.popMatrix();
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
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
                BlurUtil.INSTANCE.drawBlurredRoundedRect(bgX, bgY, bgW, bgH, r, BetterVisualsConfig.blurRadius);
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
