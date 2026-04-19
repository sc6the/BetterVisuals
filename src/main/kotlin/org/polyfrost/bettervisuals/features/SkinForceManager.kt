package org.polyfrost.bettervisuals.features

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.ResourceLocation
import org.polyfrost.bettervisuals.config.BetterVisualsConfig
import java.io.File
import javax.imageio.ImageIO

object SkinForceManager {

    @Volatile
    private var activeLocation: ResourceLocation? = null
    private var lastLoadedPath: String? = null

    fun getSkin(): ResourceLocation? = activeLocation

    fun loadSkin(path: String?) {
        if (path.isNullOrBlank()) {
            clear()
            return
        }
        val mc = Minecraft.getMinecraft()
        // DynamicTexture upload must happen on render thread.
        mc.addScheduledTask {
            try {
                val file = File(path)
                if (!file.exists() || !file.isFile) {
                    clear()
                    return@addScheduledTask
                }
                val img = ImageIO.read(file)
                if (img == null) {
                    clear()
                    return@addScheduledTask
                }
                val tm: TextureManager = mc.textureManager
                activeLocation?.let { tm.deleteTexture(it) }
                val dyn = DynamicTexture(img)
                activeLocation = tm.getDynamicTextureLocation("bettervisuals_skin", dyn)
                lastLoadedPath = path
                refresh()
            } catch (_: Exception) {
                clear()
            }
        }
    }

    fun ensureLoaded() {
        val path = BetterVisualsConfig.skinFilePath
        if (path.isNotBlank() && (activeLocation == null || lastLoadedPath != path)) {
            loadSkin(path)
        }
    }

    fun clear() {
        val mc = Minecraft.getMinecraft()
        val loc = activeLocation ?: return
        mc.addScheduledTask {
            try {
                mc.textureManager.deleteTexture(loc)
            } catch (_: Exception) {
            }
        }
        activeLocation = null
        lastLoadedPath = null
    }

    /**
     * Forces the player model to re-evaluate its skin/model. Since
     * [org.polyfrost.bettervisuals.mixin.AbstractClientPlayerMixin] reads config
     * every frame, nothing specific is required here beyond ensuring the texture
     * is loaded; this method exists as a hook for future invalidation.
     */
    fun refresh() {
        // No-op: getLocationSkin/getSkinType are called per-render so changes
        // propagate automatically next frame.
    }
}
