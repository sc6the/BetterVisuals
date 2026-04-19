package org.polyfrost.bettervisuals.features

import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.polyfrost.bettervisuals.config.BetterVisualsConfig

/**
 * Reimplementation of ProjectL (https://www.curseforge.com/minecraft/mc-mods/projectl)
 * by OrangeMarshall. Copies the player's current rotation into the previous-tick
 * rotation fields as soon as the mouse moves, so hit registration uses the
 * post-move rotation instead of the pre-move (MC-67665 / 1.7-style hitreg).
 */
object HitRegHandler {

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (!BetterVisualsConfig.oldHitReg) return
        if (event.dx == 0 && event.dy == 0) return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        player.prevRenderYawOffset = player.renderYawOffset
        player.prevRotationYawHead = player.rotationYawHead
        player.prevRotationYaw = player.rotationYaw
        player.prevRotationPitch = player.rotationPitch
    }
}
