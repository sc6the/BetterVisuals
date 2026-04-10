package org.polyfrost.bettervisuals.features

import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.SharedMonsterAttributes
import org.lwjgl.opengl.GL11
import org.polyfrost.bettervisuals.config.BetterVisualsConfig
import org.polyfrost.bettervisuals.mixin.MinecraftAccessor
import org.polyfrost.bettervisuals.utils.MathUtil
import org.polyfrost.bettervisuals.utils.RenderUtil
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor

object StatusBarsRenderer {

    private var displayHealth = -1f
    private var displayHunger = -1f
    private var displayArmor = -1f
    private var displayAir = -1f
    private var displayXp = -1f

    private var ghostHealth = -1f
    private var ghostDelayTicks = 0L
    private var lastHealthTarget = -1f

    fun render() {
        val minecraft = Minecraft.getMinecraft()
        val player = minecraft.thePlayer ?: return
        val cfg = BetterVisualsConfig

        val pt = (mc as MinecraftAccessor).timer.renderPartialTicks
        val speed = (cfg.statusBarAnimSpeed / 100f).coerceIn(0.01f, 0.99f)

        // Real values
        val maxHp = player.getEntityAttribute(SharedMonsterAttributes.maxHealth).attributeValue.toFloat()
        val realHp = player.health / maxHp
        val realHunger = player.foodStats.foodLevel / 20f
        val realArmor = player.totalArmorValue / 20f
        val realAir = player.air.coerceAtLeast(0) / 300f
        val realXp = player.experience

        // Init
        if (displayHealth < 0f) {
            displayHealth = realHp; displayHunger = realHunger
            displayArmor = realArmor; displayAir = realAir; displayXp = realXp
            ghostHealth = realHp; lastHealthTarget = realHp
        }

        // Animate
        displayHealth = MathUtil.smoothDamp(displayHealth, realHp, speed, pt)
        displayHunger = MathUtil.smoothDamp(displayHunger, realHunger, speed, pt)
        displayArmor = MathUtil.smoothDamp(displayArmor, realArmor, speed, pt)
        displayAir = MathUtil.smoothDamp(displayAir, realAir, speed, pt)
        displayXp = MathUtil.smoothDamp(displayXp, realXp, speed, pt)

        // Ghost health (trails behind on loss, leads ahead on gain)
        val now = System.currentTimeMillis()
        if (realHp < lastHealthTarget - 0.01f) ghostDelayTicks = now + 400
        if (realHp > lastHealthTarget + 0.01f) ghostDelayTicks = now + 400
        lastHealthTarget = realHp
        if (now > ghostDelayTicks) ghostHealth = MathUtil.smoothDamp(ghostHealth, displayHealth, speed * 0.3f, pt)
        else ghostHealth = if (realHp < displayHealth) maxOf(ghostHealth, displayHealth) else minOf(ghostHealth, displayHealth)
        // Clamp: ghost should always be between fill and target
        if (realHp <= displayHealth) {
            // Losing health — ghost trails above fill
            if (ghostHealth < displayHealth) ghostHealth = displayHealth
        } else {
            // Gaining health — ghost leads at target while fill catches up
            ghostHealth = maxOf(ghostHealth, realHp)
        }

        // Layout
        val sw = UResolution.scaledWidth
        val sh = UResolution.scaledHeight
        val barH = cfg.statusBarHeight.toFloat()
        val r = cfg.statusBarRadius
        val sp = cfg.statusBarSpacing.toFloat()
        val gap = cfg.statusBarGap.toFloat()
        val fullW = 182f
        val halfW = (fullW - gap) / 2f

        val ox = cfg.hotbarX.toFloat()
        val oy = cfg.hotbarY.toFloat()
        val baseX = sw / 2f - 91f + ox
        val hotbarTop = sh - 22f - cfg.bottomOffset + oy

        val track = cfg.trackColor.toJavaColor()
        val sa = if (cfg.statusBarShadow) (cfg.statusBarShadowOpacity * 255 / 100).coerceIn(0, 255) else 0

        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)

        // XP bar — directly above hotbar
        val xpY = hotbarTop - barH - cfg.statusBarGapFromHotbar
        drawBar(baseX, xpY, fullW, barH, r, displayXp.coerceIn(0f, 1f), cfg.xpColor.toJavaColor(), track, sa)

        // Health (left) + Hunger (right)
        val hhY = xpY - sp - barH
        val hpCurrent = player.health.toInt()
        val hpMax = maxHp.toInt()
        val hungerPct = (player.foodStats.foodLevel * 100 / 20)

        if (cfg.healthGhostEnabled) {
            drawBarWithGhost(
                baseX, hhY, halfW, barH, r,
                displayHealth.coerceIn(0f, 1f), ghostHealth.coerceIn(0f, 1f),
                cfg.healthColor.toJavaColor(), cfg.healthGhostColor.toJavaColor(), track,
                "$hpCurrent/$hpMax", sa
            )
        } else {
            drawBar(baseX, hhY, halfW, barH, r, displayHealth.coerceIn(0f, 1f), cfg.healthColor.toJavaColor(), track, sa, "$hpCurrent/$hpMax")
        }

        drawBar(baseX + halfW + gap, hhY, halfW, barH, r, displayHunger.coerceIn(0f, 1f), cfg.hungerColor.toJavaColor(), track, sa, "$hungerPct%")

        // Armor (if > 0)
        var topY = hhY
        if (player.totalArmorValue > 0) {
            topY -= sp + barH
            val armorPct = (player.totalArmorValue * 100 / 20)
            drawBar(baseX, topY, fullW, barH, r, displayArmor.coerceIn(0f, 1f), cfg.armorColor.toJavaColor(), track, sa, "$armorPct%")
        }

        // Air (if underwater)
        if (player.air < 300) {
            topY -= sp + barH
            val airPct = (player.air.coerceAtLeast(0) * 100 / 300)
            drawBar(baseX, topY, fullW, barH, r, displayAir.coerceIn(0f, 1f), cfg.airColor.toJavaColor(), track, sa, "$airPct%")
        }

        // Level text centered on XP bar
        val lvl = player.experienceLevel
        if (lvl > 0) {
            GlStateManager.enableTexture2D()
            val fr = minecraft.fontRendererObj
            val lvlStr = lvl.toString()
            val textX = baseX + fullW / 2f - fr.getStringWidth(lvlStr) / 2f
            val textY = xpY + (barH - 8f) / 2f
            fr.drawStringWithShadow(lvlStr, textX, textY, cfg.levelColor.getRGB())
        }

        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.popMatrix()
    }

    private fun drawBar(
        x: Float, y: Float, w: Float, h: Float, r: Float,
        fraction: Float, fillColor: Color, trackColor: Color,
        shadowAlpha: Int, text: String? = null
    ) {
        if (shadowAlpha > 0) RenderUtil.drawDropShadow(x, y, w, h, r, BetterVisualsConfig.shadowSpread, shadowAlpha)
        RenderUtil.drawRoundedRect(x, y, w, h, r, trackColor)

        if (fraction > 0f && fillColor.alpha > 0) {
            val mc = Minecraft.getMinecraft()
            val sr = ScaledResolution(mc)
            val sf = sr.scaleFactor
            val sx = floor(x * sf).toInt()
            val sy = floor((sr.scaledHeight - y - h) * sf).toInt()
            val sw = ceil(w * fraction.coerceIn(0f, 1f) * sf).toInt().coerceAtLeast(1)
            val sh = ceil(h * sf).toInt().coerceAtLeast(1)
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(sx, sy, sw, sh)
            RenderUtil.drawRoundedRect(x, y, w, h, r, fillColor)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }

        if (text != null) {
            GlStateManager.enableTexture2D()
            val fr = Minecraft.getMinecraft().fontRendererObj
            fr.drawStringWithShadow(text, x + 3f, y + (h - 8f) / 2f, 0xFFFFFF)
        }
    }

    private fun drawBarWithGhost(
        x: Float, y: Float, w: Float, h: Float, r: Float,
        fraction: Float, ghostFrac: Float,
        fillColor: Color, ghostColor: Color, trackColor: Color,
        text: String, shadowAlpha: Int
    ) {
        if (shadowAlpha > 0) RenderUtil.drawDropShadow(x, y, w, h, r, BetterVisualsConfig.shadowSpread, shadowAlpha)
        RenderUtil.drawRoundedRect(x, y, w, h, r, trackColor)

        val mc = Minecraft.getMinecraft()
        val sr = ScaledResolution(mc)
        val sf = sr.scaleFactor
        val sx = floor(x * sf).toInt()
        val sy = floor((sr.scaledHeight - y - h) * sf).toInt()
        val sh = ceil(h * sf).toInt().coerceAtLeast(1)

        // Ghost
        val gf = ghostFrac.coerceIn(0f, 1f)
        if (gf > 0f && ghostColor.alpha > 0) {
            val sw = ceil(w * gf * sf).toInt().coerceAtLeast(1)
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(sx, sy, sw, sh)
            RenderUtil.drawRoundedRect(x, y, w, h, r, ghostColor)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }

        // Fill
        val f = fraction.coerceIn(0f, 1f)
        if (f > 0f && fillColor.alpha > 0) {
            val sw = ceil(w * f * sf).toInt().coerceAtLeast(1)
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(sx, sy, sw, sh)
            RenderUtil.drawRoundedRect(x, y, w, h, r, fillColor)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }

        // Text
        GlStateManager.enableTexture2D()
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
            text, x + 3f, y + (h - 8f) / 2f, 0xFFFFFF
        )
    }
}
