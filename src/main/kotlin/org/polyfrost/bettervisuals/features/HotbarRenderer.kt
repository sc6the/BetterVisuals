package org.polyfrost.bettervisuals.features

import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.polyfrost.bettervisuals.config.BetterVisualsConfig
import org.polyfrost.bettervisuals.mixin.MinecraftAccessor
import org.lwjgl.opengl.GL11
import org.polyfrost.bettervisuals.utils.BlurUtil
import org.polyfrost.bettervisuals.utils.GuiScaleBypass
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
        // Bypass GuiScaleMod so BV reads vanilla dimensions — its HUD should
        // not inherit the user's HUD-wide scale multiplier.
        val vanilla = GuiScaleBypass.wrap { ScaledResolution(Minecraft.getMinecraft()) }
        val sw = vanilla.scaledWidth
        val sh = vanilla.scaledHeight

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
        // Counteract GuiScaleMod's compressed ortho so BV renders at vanilla pixel size.
        val gsMul = GuiScaleBypass.multiplier()
        if (gsMul != 1f) {
            val inv = 1f / gsMul
            GlStateManager.scale(inv, inv, 1f)
        }
        // Apply BV's own HUD scale around hotbar bottom-center pivot.
        val hs = cfg.hudScale
        if (hs != 1f) {
            val px = sw / 2f
            val py = sh.toFloat()
            GlStateManager.translate(px, py, 0f)
            GlStateManager.scale(hs, hs, 1f)
            GlStateManager.translate(-px, -py, 0f)
        }
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        // Clear stray GL scissor from status bars (raw GL11) so the hotbar is not clipped next frame.
        GL11.glDisable(GL11.GL_SCISSOR_TEST)

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
        val sa = (cfg.glowOpacity * 255 / 100).coerceIn(0, 255)
        val barBgX = sw / 2f - 91f + ox

        if (bg.alpha != 0) {
            if (cfg.blurEnabled) BlurUtil.drawBlurredRoundedRect(barBgX, barY, 182f, 22f, r, cfg.blurRadius)
            RenderUtil.drawBlackShadow(barBgX, barY, 182f, 22f, r, cfg.glowSpread, 80)
            if (cfg.glowEnabled) RenderUtil.drawGlow(barBgX, barY, 182f, 22f, r, bg, cfg.glowSpread, sa)
            RenderUtil.drawRoundedRect(barBgX, barY, 182f, 22f, r, bg)
        }

        if (hl.alpha != 0) {
            RenderUtil.drawBlackShadow(highlightX, barY, 22f, 22f, r, (cfg.glowSpread * 0.6f).toInt().coerceAtLeast(4), 80)
            if (cfg.glowEnabled) RenderUtil.drawGlow(highlightX, barY, 22f, 22f, r, hl, (cfg.glowSpread * 0.6f).toInt().coerceAtLeast(4), (sa * 0.7f).toInt())
            RenderUtil.drawRoundedRect(highlightX, barY, 22f, 22f, r, hl)
        }

        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }
}
