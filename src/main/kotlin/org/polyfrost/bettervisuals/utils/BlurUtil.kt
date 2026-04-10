package org.polyfrost.bettervisuals.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object BlurUtil {

    private var shaderProgram = 0
    private var texelSizeLoc = 0
    private var directionLoc = 0
    private var radiusLoc = 0
    private var textureLoc = 0

    private var drawProgram = 0
    private var drawTexLoc = 0
    private var drawRectLoc = 0
    private var drawFeatherLoc = 0
    private var drawInitFailed = false

    private var fboH: Framebuffer? = null
    private var fboV: Framebuffer? = null
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastBlurNanos = 0L
    private var blurReady = false
    private var initFailed = false

    private const val VERTEX_SRC = """#version 120
void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = ftransform();
}"""

    private const val FRAGMENT_SRC = """#version 120
uniform sampler2D DiffuseSampler;
uniform vec2 texelSize;
uniform vec2 direction;
uniform float radius;
void main() {
    vec4 col = vec4(0.0);
    float total = 0.0;
    float sigma = max(radius * 0.4, 1.0);
    for (float i = -radius; i <= radius; i += 1.0) {
        float w = exp(-(i * i) / (2.0 * sigma * sigma));
        col += texture2D(DiffuseSampler, gl_TexCoord[0].st + direction * texelSize * i) * w;
        total += w;
    }
    gl_FragColor = col / total;
}"""

    private const val DRAW_FRAGMENT_SRC = """#version 120
uniform sampler2D DiffuseSampler;
uniform vec4 rect;
uniform vec2 feather;
void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 color = texture2D(DiffuseSampler, uv);
    float dx = max(rect.x - uv.x, uv.x - rect.z);
    float dy = max(rect.y - uv.y, uv.y - rect.w);
    float fx = clamp(max(dx, 0.0) / max(feather.x, 0.001), 0.0, 1.0);
    float fy = clamp(max(dy, 0.0) / max(feather.y, 0.001), 0.0, 1.0);
    float d = clamp(length(vec2(fx, fy)), 0.0, 1.0);
    float t = 1.0 - d;
    float alpha = t * t * (3.0 - 2.0 * t);
    gl_FragColor = vec4(color.rgb, alpha);
}"""

    private fun init(): Boolean {
        if (shaderProgram != 0) return true
        if (initFailed) return false
        return try {
            val vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
            GL20.glShaderSource(vs, VERTEX_SRC)
            GL20.glCompileShader(vs)
            if (GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == 0) {
                GL20.glDeleteShader(vs); initFailed = true; return false
            }

            val fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
            GL20.glShaderSource(fs, FRAGMENT_SRC)
            GL20.glCompileShader(fs)
            if (GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == 0) {
                GL20.glDeleteShader(vs); GL20.glDeleteShader(fs); initFailed = true; return false
            }

            shaderProgram = GL20.glCreateProgram()
            GL20.glAttachShader(shaderProgram, vs)
            GL20.glAttachShader(shaderProgram, fs)
            GL20.glLinkProgram(shaderProgram)
            GL20.glDeleteShader(vs)
            GL20.glDeleteShader(fs)

            if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == 0) {
                GL20.glDeleteProgram(shaderProgram); shaderProgram = 0; initFailed = true; return false
            }

            texelSizeLoc = GL20.glGetUniformLocation(shaderProgram, "texelSize")
            directionLoc = GL20.glGetUniformLocation(shaderProgram, "direction")
            radiusLoc = GL20.glGetUniformLocation(shaderProgram, "radius")
            textureLoc = GL20.glGetUniformLocation(shaderProgram, "DiffuseSampler")
            true
        } catch (e: Exception) {
            initFailed = true; false
        }
    }

    private fun initDraw(): Boolean {
        if (drawProgram != 0) return true
        if (drawInitFailed) return false
        return try {
            val vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
            GL20.glShaderSource(vs, VERTEX_SRC)
            GL20.glCompileShader(vs)
            if (GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == 0) {
                GL20.glDeleteShader(vs); drawInitFailed = true; return false
            }

            val fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
            GL20.glShaderSource(fs, DRAW_FRAGMENT_SRC)
            GL20.glCompileShader(fs)
            if (GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == 0) {
                GL20.glDeleteShader(vs); GL20.glDeleteShader(fs); drawInitFailed = true; return false
            }

            drawProgram = GL20.glCreateProgram()
            GL20.glAttachShader(drawProgram, vs)
            GL20.glAttachShader(drawProgram, fs)
            GL20.glLinkProgram(drawProgram)
            GL20.glDeleteShader(vs)
            GL20.glDeleteShader(fs)

            if (GL20.glGetProgrami(drawProgram, GL20.GL_LINK_STATUS) == 0) {
                GL20.glDeleteProgram(drawProgram); drawProgram = 0; drawInitFailed = true; return false
            }

            drawTexLoc = GL20.glGetUniformLocation(drawProgram, "DiffuseSampler")
            drawRectLoc = GL20.glGetUniformLocation(drawProgram, "rect")
            drawFeatherLoc = GL20.glGetUniformLocation(drawProgram, "feather")
            true
        } catch (e: Exception) {
            drawInitFailed = true; false
        }
    }

    private fun ensureFBOs(w: Int, h: Int) {
        if (fboH != null && lastWidth == w && lastHeight == h) return
        fboH?.deleteFramebuffer()
        fboV?.deleteFramebuffer()
        fboH = Framebuffer(w, h, false)
        fboV = Framebuffer(w, h, false)
        // Set linear filtering
        for (fbo in listOf(fboH!!, fboV!!)) {
            GlStateManager.bindTexture(fbo.framebufferTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        }
        lastWidth = w
        lastHeight = h
    }

    private fun prepareBlur(blurRadius: Float) {
        if (!init()) return
        val mc = Minecraft.getMinecraft()
        val w = mc.displayWidth
        val h = mc.displayHeight

        val now = System.nanoTime()
        if (blurReady && now - lastBlurNanos < 1_000_000L && lastWidth == w && lastHeight == h) return

        ensureFBOs(w, h)
        val src = mc.framebuffer.framebufferTexture

        GL20.glUseProgram(shaderProgram)
        GL20.glUniform1i(textureLoc, 0)
        GL20.glUniform2f(texelSizeLoc, 1f / w, 1f / h)
        GL20.glUniform1f(radiusLoc, blurRadius)

        // Horizontal pass: MC framebuffer -> fboH
        fboH!!.bindFramebuffer(true)
        GlStateManager.bindTexture(src)
        GL20.glUniform2f(directionLoc, 1f, 0f)
        drawFullscreenQuad(w, h)

        // Vertical pass: fboH -> fboV
        fboV!!.bindFramebuffer(true)
        GlStateManager.bindTexture(fboH!!.framebufferTexture)
        GL20.glUniform2f(directionLoc, 0f, 1f)
        drawFullscreenQuad(w, h)

        GL20.glUseProgram(0)
        mc.framebuffer.bindFramebuffer(true)

        lastBlurNanos = now
        blurReady = true
    }

    fun drawBlurredRect(x: Float, y: Float, w: Float, h: Float, blurRadius: Float) {
        if (blurRadius <= 0f) return
        prepareBlur(blurRadius)
        val fbo = fboV ?: return
        if (!blurReady) return

        val sr = ScaledResolution(Minecraft.getMinecraft())
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()

        val u0 = x / sw
        val v0 = 1f - (y + h) / sh
        val u1 = (x + w) / sw
        val v1 = 1f - y / sh

        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.bindTexture(fbo.framebufferTexture)

        val wr = Tessellator.getInstance().worldRenderer
        wr.begin(7, DefaultVertexFormats.POSITION_TEX)
        wr.pos(x.toDouble(), (y + h).toDouble(), 0.0).tex(u0.toDouble(), v0.toDouble()).endVertex()
        wr.pos((x + w).toDouble(), (y + h).toDouble(), 0.0).tex(u1.toDouble(), v0.toDouble()).endVertex()
        wr.pos((x + w).toDouble(), y.toDouble(), 0.0).tex(u1.toDouble(), v1.toDouble()).endVertex()
        wr.pos(x.toDouble(), y.toDouble(), 0.0).tex(u0.toDouble(), v1.toDouble()).endVertex()
        Tessellator.getInstance().draw()

        GlStateManager.disableBlend()
    }

    fun drawBlurredRectFeathered(x: Float, y: Float, w: Float, h: Float, blurRadius: Float, feather: Float) {
        if (blurRadius <= 0f) return
        prepareBlur(blurRadius)
        val fbo = fboV ?: return
        if (!blurReady) return
        if (!initDraw()) {
            drawBlurredRect(x, y, w, h, blurRadius)
            return
        }

        val sr = ScaledResolution(Minecraft.getMinecraft())
        val sw = sr.scaledWidth.toFloat()
        val sh = sr.scaledHeight.toFloat()
        val f = feather.coerceAtLeast(0.5f)

        val uLeft = x / sw
        val uRight = (x + w) / sw
        val vBottom = 1f - (y + h) / sh
        val vTop = 1f - y / sh
        val featherU = f / sw
        val featherV = f / sh

        val ox = x - f
        val oy = y - f
        val ow = w + f * 2
        val oh = h + f * 2
        val ou0 = ox / sw
        val ov0 = 1f - (oy + oh) / sh
        val ou1 = (ox + ow) / sw
        val ov1 = 1f - oy / sh

        GL20.glUseProgram(drawProgram)
        GL20.glUniform1i(drawTexLoc, 0)
        GL20.glUniform4f(drawRectLoc, uLeft, vBottom, uRight, vTop)
        GL20.glUniform2f(drawFeatherLoc, featherU, featherV)

        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.bindTexture(fbo.framebufferTexture)

        val wr = Tessellator.getInstance().worldRenderer
        wr.begin(7, DefaultVertexFormats.POSITION_TEX)
        wr.pos(ox.toDouble(), (oy + oh).toDouble(), 0.0).tex(ou0.toDouble(), ov0.toDouble()).endVertex()
        wr.pos((ox + ow).toDouble(), (oy + oh).toDouble(), 0.0).tex(ou1.toDouble(), ov0.toDouble()).endVertex()
        wr.pos((ox + ow).toDouble(), oy.toDouble(), 0.0).tex(ou1.toDouble(), ov1.toDouble()).endVertex()
        wr.pos(ox.toDouble(), oy.toDouble(), 0.0).tex(ou0.toDouble(), ov1.toDouble()).endVertex()
        Tessellator.getInstance().draw()

        GL20.glUseProgram(0)
        GlStateManager.disableBlend()
    }

    private fun drawFullscreenQuad(fw: Int, fh: Int) {
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, fw.toDouble(), fh.toDouble(), 0.0, 1000.0, 3000.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glTranslatef(0f, 0f, -2000f)

        val wr = Tessellator.getInstance().worldRenderer
        wr.begin(7, DefaultVertexFormats.POSITION_TEX)
        wr.pos(0.0, fh.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        wr.pos(fw.toDouble(), fh.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
        wr.pos(fw.toDouble(), 0.0, 0.0).tex(1.0, 1.0).endVertex()
        wr.pos(0.0, 0.0, 0.0).tex(0.0, 1.0).endVertex()
        Tessellator.getInstance().draw()

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPopMatrix()

        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
    }
}
