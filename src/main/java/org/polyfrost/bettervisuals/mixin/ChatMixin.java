package org.polyfrost.bettervisuals.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.polyfrost.bettervisuals.utils.BlurUtil;
import org.polyfrost.bettervisuals.utils.RenderUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class ChatMixin extends Gui {

    @Shadow @Final private Minecraft mc;
    @Shadow @Final private List<ChatLine> drawnChatLines;
    @Shadow private int scrollPos;

    @Shadow public abstract boolean getChatOpen();
    @Shadow public abstract int getLineCount();
    @Shadow public abstract float getChatScale();
    @Shadow public abstract int getChatWidth();

    @Unique private boolean bettervisuals$bgDrawn = false;

    @Inject(method = "drawChat", at = @At("HEAD"))
    private void bettervisuals_resetFlag(int updateCounter, CallbackInfo ci) {
        bettervisuals$bgDrawn = false;
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V",
            shift = At.Shift.AFTER))
    private void bettervisuals_drawChatBg(int updateCounter, CallbackInfo ci) {
        if (!BetterVisualsConfig.chatEnabled) return;

        boolean chatOpen = getChatOpen();
        int lineCount = getLineCount();
        float chatOpacity = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
        int chatWidth = MathHelper.ceiling_float_int((float) getChatWidth() / getChatScale());

        int maxAlpha = 0;
        int visibleCount = 0;
        for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; i++) {
            ChatLine line = drawnChatLines.get(i + scrollPos);
            if (line != null) {
                int age = updateCounter - line.getUpdatedCounter();
                if (age < 200 || chatOpen) {
                    double d = (double) age / 200.0;
                    d = 1.0 - d;
                    d *= 10.0;
                    d = Math.max(0.0, Math.min(1.0, d));
                    d *= d;
                    int alpha = (int) (255.0 * d);
                    if (chatOpen) alpha = 255;
                    alpha = (int) ((float) alpha * chatOpacity);
                    if (alpha > 3) {
                        maxAlpha = Math.max(maxAlpha, alpha);
                        visibleCount = i + 1;
                    }
                }
            }
        }

        if (visibleCount <= 0) return;

        float alphaScale = maxAlpha / 255f;
        float r = BetterVisualsConfig.chatRadius;
        Color bg = BetterVisualsConfig.chatBgColor.toJavaColor();
        int adjAlpha = Math.max(1, Math.min(255, (int) (bg.getAlpha() * alphaScale)));
        Color adjBg = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), adjAlpha);

        float bx = -2f;
        float by = -visibleCount * 9f;
        float bw = chatWidth + 8f;
        float bh = visibleCount * 9f;

        if (BetterVisualsConfig.blurEnabled) {
            // Blur needs screen-space coords — convert from chat-local to GUI coords
            float chatScale = getChatScale();
            net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
            float srH = sr.getScaledHeight();
            float sx = 2f + bx * chatScale;
            float sy = srH - 20f + by * chatScale;
            BlurUtil.INSTANCE.drawBlurredRoundedRect(sx, sy, bw * chatScale, bh * chatScale,
                    r * chatScale, BetterVisualsConfig.blurRadius);
        }

        int sa = BetterVisualsConfig.chatGlow ?
                (int) (BetterVisualsConfig.chatGlowOpacity * 255 / 100 * alphaScale) : 0;
        if (sa > 0) RenderUtil.INSTANCE.drawGlow(bx, by, bw, bh, r, adjBg, BetterVisualsConfig.glowSpread, sa);
        RenderUtil.INSTANCE.drawRoundedRect(bx, by, bw, bh, r, adjBg);
        bettervisuals$bgDrawn = true;
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void bettervisuals_redirectRect(int left, int top, int right, int bottom, int color) {
        if (!BetterVisualsConfig.chatEnabled || !bettervisuals$bgDrawn) {
            drawRect(left, top, right, bottom, color);
            return;
        }
        // Keep scroll bar rects (non-zero RGB), suppress per-line backgrounds (pure black alpha)
        if ((color & 0x00FFFFFF) != 0) {
            drawRect(left, top, right, bottom, color);
        }
    }
}
