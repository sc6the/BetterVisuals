package org.polyfrost.bettervisuals.features

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.polyfrost.bettervisuals.config.BetterVisualsConfig

object NoCreativeDriftHandler {

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        if (!BetterVisualsConfig.noCreativeDrift) return
        if (event.phase != TickEvent.Phase.END) return
        val player = event.player ?: return
        val mc = Minecraft.getMinecraft()
        if (player !== mc.thePlayer) return
        if (!player.capabilities.isFlying) return

        val gs = mc.gameSettings
        if (isPressed(gs.keyBindForward) || isPressed(gs.keyBindBack)
            || isPressed(gs.keyBindLeft) || isPressed(gs.keyBindRight)
            || isPressed(gs.keyBindJump) || isPressed(gs.keyBindSneak)
        ) return

        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0
    }

    private fun isPressed(key: net.minecraft.client.settings.KeyBinding?): Boolean {
        return key != null && key.isKeyDown
    }
}
