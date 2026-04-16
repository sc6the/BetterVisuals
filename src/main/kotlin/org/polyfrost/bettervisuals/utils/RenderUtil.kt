package org.polyfrost.bettervisuals.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object RenderUtil {

    private fun quad(x1: Float, y1: Float, x2: Float, y2: Float) {
        val wr = Tessellator.getInstance().worldRenderer
        wr.begin(7, DefaultVertexFormats.POSITION)
        wr.pos(x1.toDouble(), y2.toDouble(), 0.0).endVertex()
        wr.pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        wr.pos(x2.toDouble(), y1.toDouble(), 0.0).endVertex()
        wr.pos(x1.toDouble(), y1.toDouble(), 0.0).endVertex()
        Tessellator.getInstance().draw()
    }

    fun drawRoundedRect(x: Float, y: Float, w: Float, h: Float, r: Float, colour: Color) {
        if (w <= 0f || h <= 0f || colour.alpha == 0) return
        val rad = r.coerceAtMost(w / 2f).coerceAtMost(h / 2f)

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

        if (rad < 0.5f) {
            quad(x, y, x + w, y + h)
        } else {
            val wr = Tessellator.getInstance().worldRenderer
            val segments = (rad / 0.125f).toInt().coerceAtLeast(4)
            val step = rad / segments

            wr.begin(7, DefaultVertexFormats.POSITION)

            // Top arc — use dx at the bottom edge of each strip (closer to center = wider)
            for (i in 0 until segments) {
                val y0 = y + step * i
                val y1 = y + step * (i + 1)
                val dy = rad - step * (i + 1)
                val dx = sqrt(rad * rad - dy * dy)
                wr.pos((x + rad - dx).toDouble(), y1.toDouble(), 0.0).endVertex()
                wr.pos((x + w - rad + dx).toDouble(), y1.toDouble(), 0.0).endVertex()
                wr.pos((x + w - rad + dx).toDouble(), y0.toDouble(), 0.0).endVertex()
                wr.pos((x + rad - dx).toDouble(), y0.toDouble(), 0.0).endVertex()
            }

            // Middle rectangle
            if (h - 2 * rad > 0f) {
                wr.pos(x.toDouble(), (y + h - rad).toDouble(), 0.0).endVertex()
                wr.pos((x + w).toDouble(), (y + h - rad).toDouble(), 0.0).endVertex()
                wr.pos((x + w).toDouble(), (y + rad).toDouble(), 0.0).endVertex()
                wr.pos(x.toDouble(), (y + rad).toDouble(), 0.0).endVertex()
            }

            // Bottom arc — use dx at the top edge of each strip (closer to center = wider)
            for (i in 0 until segments) {
                val y0 = y + h - rad + step * i
                val y1 = y + h - rad + step * (i + 1)
                val dy = step * i
                val dx = if (dy < 0.001f) rad else sqrt(rad * rad - dy * dy)
                wr.pos((x + rad - dx).toDouble(), y1.toDouble(), 0.0).endVertex()
                wr.pos((x + w - rad + dx).toDouble(), y1.toDouble(), 0.0).endVertex()
                wr.pos((x + w - rad + dx).toDouble(), y0.toDouble(), 0.0).endVertex()
                wr.pos((x + rad - dx).toDouble(), y0.toDouble(), 0.0).endVertex()
            }

            Tessellator.getInstance().draw()
        }

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    /**
     * Outer glow that adapts its color to the background.
     *
     * Colored background → saturated halo matching the background hue.
     * Neutral/gray background → luma-driven halo (dark bg glows bright, bright bg glows dark)
     * so the effect stays visible regardless of the underlying terrain.
     */
    fun drawGlow(x: Float, y: Float, w: Float, h: Float, r: Float, bgColor: Color, layers: Int = 16, baseAlpha: Int = 30) {
        if (baseAlpha <= 0) return
        val n = layers.coerceAtLeast(1)
        val glow = deriveGlowColor(bgColor)
        // Outer rings fade to near-zero on linear falloff; floor the outer half so the halo reads in-game.
        val outerFloor = max(2, min(12, (baseAlpha * 0.25f).roundToInt()))
        for (i in n downTo 1) {
            val spread = i * 0.5f
            val depth = (i - 1f) / n
            // Quadratic falloff — softer halo, more presence near the element than a linear fade
            val falloff = 1f - depth
            val linear = baseAlpha * falloff * falloff
            var a = linear.roundToInt().coerceIn(0, 255)
            if (depth >= 0.5f && a < outerFloor) a = outerFloor
            if (a <= 0) continue
            drawRoundedRect(
                x - spread, y - spread, w + spread * 2, h + spread * 2,
                r + spread * 0.6f,
                Color(glow.red, glow.green, glow.blue, a)
            )
        }
    }

    private fun deriveGlowColor(bg: Color): Color {
        val r = bg.red
        val g = bg.green
        val b = bg.blue
        val mx = max(r, max(g, b))
        val mn = min(r, min(g, b))
        val chroma = mx - mn
        // Near-grayscale: luma-driven halo. Dark bg → soft warm white, bright bg → deep shadow.
        if (chroma < 24) {
            val luma = (r * 0.299f + g * 0.587f + b * 0.114f).toInt()
            return if (luma < 128) Color(255, 250, 230) else Color(0, 0, 0)
        }
        // Colored bg: boost toward full saturation so the halo reads as the bg's hue,
        // not a washed-out version of it.
        if (mx == 0) return Color(255, 255, 255)
        val scale = 255f / mx
        val nr = (r * scale).roundToInt().coerceIn(0, 255)
        val ng = (g * scale).roundToInt().coerceIn(0, 255)
        val nb = (b * scale).roundToInt().coerceIn(0, 255)
        return Color(nr, ng, nb)
    }
}
