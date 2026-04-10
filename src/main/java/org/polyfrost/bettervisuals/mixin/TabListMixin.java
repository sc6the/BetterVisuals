package org.polyfrost.bettervisuals.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.polyfrost.bettervisuals.utils.BlurUtil;
import org.polyfrost.bettervisuals.utils.RenderUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class TabListMixin {

    @Shadow @Final private Minecraft mc;
    @Shadow private IChatComponent header;
    @Shadow private IChatComponent footer;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    private void bettervisuals_drawTabBg(int width, Scoreboard scoreboard,
                                         ScoreObjective objective, CallbackInfo ci) {
        if (!BetterVisualsConfig.tabEnabled) return;

        NetHandlerPlayClient handler = mc.getNetHandler();
        if (handler == null) return;
        Collection<NetworkPlayerInfo> players = handler.getPlayerInfoMap();
        int count = Math.min(players.size(), 80);
        if (count == 0) return;

        FontRenderer fr = mc.fontRendererObj;

        // Calculate columns / rows (mirrors vanilla)
        int columns = count;
        int rows = 1;
        while (columns > 20) {
            rows++;
            columns = (count + rows - 1) / rows;
        }

        // Max name width
        int maxNameW = 0;
        for (NetworkPlayerInfo info : players) {
            String name = info.getDisplayName() != null
                    ? info.getDisplayName().getFormattedText()
                    : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(),
                    info.getGameProfile().getName());
            maxNameW = Math.max(maxNameW, fr.getStringWidth(name));
        }

        // Column width: 9px player head + name width + 13px ping indicator
        int colWidth = Math.min(columns * (9 + maxNameW + 13), width - 50) / columns;
        int totalWidth = colWidth * columns + (columns - 1) * 5;

        // If there's an objective, account for score display width
        if (objective != null) {
            String sampleScore = EnumChatFormatting.RED + "999";
            totalWidth = Math.max(totalWidth, colWidth * columns + (columns - 1) * 5
                    + fr.getStringWidth(sampleScore) + 2);
        }

        // Widen for header / footer
        if (header != null) {
            for (String s : fr.listFormattedStringToWidth(header.getFormattedText(), width - 50)) {
                totalWidth = Math.max(totalWidth, fr.getStringWidth(s));
            }
        }
        if (footer != null) {
            for (String s : fr.listFormattedStringToWidth(footer.getFormattedText(), width - 50)) {
                totalWidth = Math.max(totalWidth, fr.getStringWidth(s));
            }
        }

        // Heights
        int headerH = 0;
        if (header != null)
            headerH = fr.listFormattedStringToWidth(header.getFormattedText(), width - 50).size()
                    * fr.FONT_HEIGHT;
        int bodyH = rows * 9;
        int footerH = 0;
        if (footer != null)
            footerH = fr.listFormattedStringToWidth(footer.getFormattedText(), width - 50).size()
                    * fr.FONT_HEIGHT;

        int pad = 4;
        float bgX = width / 2f - totalWidth / 2f - pad;
        float bgY = 10f - pad;
        float bgW = totalWidth + pad * 2;
        float bgH = headerH + (headerH > 0 ? 1 : 0) + bodyH + (footerH > 0 ? 1 : 0) + footerH + pad * 2;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (BetterVisualsConfig.blurEnabled) {
            BlurUtil.INSTANCE.drawBlurredRect(bgX, bgY, bgW, bgH, BetterVisualsConfig.blurRadius);
        }

        float r = BetterVisualsConfig.tabRadius;
        Color bg = BetterVisualsConfig.tabBgColor.toJavaColor();
        int sa = BetterVisualsConfig.tabShadow
                ? (int) (BetterVisualsConfig.tabShadowOpacity * 255 / 100) : 0;
        if (sa > 0) RenderUtil.INSTANCE.drawDropShadow(bgX, bgY, bgW, bgH, r, BetterVisualsConfig.shadowSpread, sa);
        RenderUtil.INSTANCE.drawRoundedRect(bgX, bgY, bgW, bgH, r, bg);
    }

    @ModifyArg(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawRect(IIIII)V"),
            index = 4)
    private int bettervisuals_suppressRect(int color) {
        if (BetterVisualsConfig.tabEnabled) return 0;
        return color;
    }
}
