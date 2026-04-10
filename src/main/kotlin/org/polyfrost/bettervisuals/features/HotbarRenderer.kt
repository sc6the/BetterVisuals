package org.polyfrost.bettervisuals.features

import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.polyfrost.bettervisuals.config.BetterVisualsConfig
import org.polyfrost.bettervisuals.mixin.MinecraftAccessor
import org.polyfrost.bettervisuals.utils.BlurUtil
import org.polyfrost.bettervisuals.utils.MathUtil
import org.polyfrost.bettervisuals.utils.RenderUtil

object HotbarRenderer {

    private var cachedW = 0
    private var cachedH = 0
    private var highlightX = -1f
    private var barY = 0f
    private var hiding = false
    private var firstTime = true

    fun initialize() {
        firstTime = true
    }

    fun render() {
        val player = Minecraft.getMinecraft().renderViewEntity as? EntityPlayer ?: return
        val sw = UResolution.scaledWidth
        val sh = UResolution.scaledHeight

        val cfg = BetterVisualsConfig
        val ox = cfg.hotbarX.toFloat()
        val oy = cfg.hotbarY.toFloat()
        val restY = sh - 22f - cfg.bottomOffset + oy
        val slotTarget = sw / 2f - 91f + player.inventory.currentItem * 20 + ox

        if (firstTime) {
            firstTime = false
            highlightX = slotTarget
            barY = restY
            cachedW = sw; cachedH = sh
        }
        if (cachedW != sw || cachedH != sh) {
            highlightX = slotTarget
            barY = restY
            cachedW = sw; cachedH = sh
        }

        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)

        val pt = (mc as MinecraftAccessor).timer.renderPartialTicks
        // Halved so default 10 feels like old 5
        val speed = (cfg.animationSpeed / 200f).coerceIn(0.01f, 0.99f)

        hiding = Minecraft.getMinecraft().currentScreen is GuiChat
        val targetY = if (hiding) sh.toFloat() + 2f else restY
        barY = MathUtil.smoothDamp(barY, targetY, speed * 0.5f, pt)

        highlightX = MathUtil.smoothDamp(highlightX, slotTarget, speed, pt)
        if (cfg.snapDistance > 0f && Math.abs(highlightX - slotTarget) < cfg.snapDistance) {
            highlightX = slotTarget
        }

        val bg = cfg.barColor.toJavaColor()
        val hl = cfg.highlightColor.toJavaColor()
        val r = cfg.cornerRadius.toFloat()
        val sa = (cfg.shadowOpacity * 255 / 100).coerceIn(0, 255)
        val barBgX = sw / 2f - 91f + ox

        if (bg.alpha != 0) {
            if (cfg.blurEnabled) BlurUtil.drawBlurredRect(barBgX, barY, 182f, 22f, cfg.blurRadius)
            if (cfg.shadowEnabled) RenderUtil.drawDropShadow(barBgX, barY, 182f, 22f, r, cfg.shadowSpread, sa)
            RenderUtil.drawRoundedRect(barBgX, barY, 182f, 22f, r, bg)
        }

        if (hl.alpha != 0) {
            if (cfg.shadowEnabled) RenderUtil.drawDropShadow(highlightX, barY, 22f, 22f, r, (cfg.shadowSpread * 0.6f).toInt().coerceAtLeast(4), (sa * 0.7f).toInt())
            RenderUtil.drawRoundedRect(highlightX, barY, 22f, 22f, r, hl)
        }

        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }
}
