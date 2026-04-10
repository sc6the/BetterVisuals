package org.polyfrost.bettervisuals.mixin;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;
import org.polyfrost.bettervisuals.utils.BlurUtil;
import org.polyfrost.bettervisuals.utils.RenderUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

@Mixin(GuiIngame.class)
public abstract class ScoreboardMixin {

    @Shadow @Final protected Minecraft mc;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void bettervisuals_scoreboard(ScoreObjective objective, ScaledResolution sr,
                                          CallbackInfo ci) {
        if (!BetterVisualsConfig.scoreboardEnabled) return;
        ci.cancel();

        FontRenderer fr = mc.fontRendererObj;
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> raw = scoreboard.getSortedScores(objective);

        List<Score> filtered = Lists.newArrayList(
                Iterables.filter(raw, s -> s.getPlayerName() != null
                        && !s.getPlayerName().startsWith("#")));
        if (filtered.size() > 15) {
            filtered = Lists.newArrayList(Iterables.limit(filtered, 15));
        }

        String title = objective.getDisplayName();
        int titleW = fr.getStringWidth(title);
        int maxW = titleW;

        for (Score score : filtered) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            if (BetterVisualsConfig.scoreboardNumbers) {
                line += ": " + EnumChatFormatting.RED + score.getScorePoints();
            }
            maxW = Math.max(maxW, fr.getStringWidth(line));
        }

        int count = filtered.size();
        int lineH = fr.FONT_HEIGHT;
        float pad = 4f;
        float r = BetterVisualsConfig.scoreboardRadius;
        float bgW = maxW + pad * 2 + 4;
        float bgH = (count + 1) * lineH + pad * 2 + 2;

        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        float userX = BetterVisualsConfig.scoreboardX;
        float userY = BetterVisualsConfig.scoreboardY;

        float bgX = BetterVisualsConfig.scoreboardSide == 1
                ? 3f + userX
                : sw - bgW - 3f + userX;
        float bgY = sh / 2f - bgH / 2f + userY;

        Color bg = BetterVisualsConfig.scoreboardBgColor.toJavaColor();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (BetterVisualsConfig.blurEnabled) {
            BlurUtil.INSTANCE.drawBlurredRect(bgX, bgY, bgW, bgH, BetterVisualsConfig.blurRadius);
        }
        if (BetterVisualsConfig.scoreboardShadow) {
            int sa = (int) (BetterVisualsConfig.scoreboardShadowOpacity * 255 / 100);
            if (sa > 0) RenderUtil.INSTANCE.drawDropShadow(bgX, bgY, bgW, bgH, r, BetterVisualsConfig.shadowSpread, sa);
        }
        RenderUtil.INSTANCE.drawRoundedRect(bgX, bgY, bgW, bgH, r, bg);

        GlStateManager.enableTexture2D();
        fr.drawStringWithShadow(title, bgX + bgW / 2f - titleW / 2f, bgY + pad, 0xFFFFFFFF);

        for (int i = 0; i < count; i++) {
            Score score = filtered.get(count - 1 - i);
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String name = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            float lineY = bgY + pad + (i + 1) * lineH + 2;

            fr.drawStringWithShadow(name, bgX + pad, lineY, 0xFFFFFFFF);
            if (BetterVisualsConfig.scoreboardNumbers) {
                String num = "" + EnumChatFormatting.RED + score.getScorePoints();
                fr.drawStringWithShadow(num, bgX + bgW - pad - fr.getStringWidth(num),
                        lineY, 0xFFFFFFFF);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
