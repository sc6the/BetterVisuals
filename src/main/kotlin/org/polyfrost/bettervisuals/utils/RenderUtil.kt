package org.polyfrost.bettervisuals.utils

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import java.awt.Color
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

    fun drawDropShadow(x: Float, y: Float, w: Float, h: Float, r: Float, layers: Int = 16, baseAlpha: Int = 30) {
        for (i in layers downTo 1) {
            val spread = i * 0.375f
            val a = (baseAlpha * (1f - (i - 1f) / layers)).toInt().coerceIn(1, 255)
            drawRoundedRect(x - spread, y - spread + i * 0.125f, w + spread * 2, h + spread * 2, r + spread * 0.5f, Color(0, 0, 0, a))
        }
    }
}
