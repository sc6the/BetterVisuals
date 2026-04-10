package org.polyfrost.bettervisuals

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.PreShutdownEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.polyfrost.bettervisuals.config.BetterVisualsConfig
import org.polyfrost.bettervisuals.features.HotbarRenderer
import org.polyfrost.bettervisuals.features.StatusBarsEventHandler
import org.polyfrost.bettervisuals.utils.ConfigPersistence
import java.util.concurrent.atomic.AtomicBoolean

@Mod(
    name = BetterVisuals.NAME,
    modid = BetterVisuals.ID,
    version = BetterVisuals.VERSION,
    modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter"
)
object BetterVisuals {

    const val NAME = "@NAME@"
    const val VERSION = "@VER@"
    const val ID = "@ID@"

    private val shutdownHookRegistered = AtomicBoolean(false)

    @Volatile
    var configDirty = false
    private var dirtySinceTick = 0L
    private var tickCounter = 0L

    fun markDirty() {
        configDirty = true
        dirtySinceTick = tickCounter
    }

    @Mod.EventHandler
    fun onInit(@Suppress("UNUSED_PARAMETER") event: FMLInitializationEvent) {
        BetterVisualsConfig.preload()
        if (shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(Thread {
                runCatching { BetterVisualsConfig.saveSettings() }
            })
        }
        EventManager.INSTANCE.register(ShutdownSave)
        EVENT_BUS.register(this)
        EVENT_BUS.register(StatusBarsEventHandler())
        ClientCommandHandler.instance.registerCommand(BvCommand)
    }

    @Mod.EventHandler
    fun onLoadComplete(@Suppress("UNUSED_PARAMETER") event: FMLLoadCompleteEvent) {
        BetterVisualsConfig.loadSettings()
        HotbarRenderer.initialize()
    }

    @Mod.EventHandler
    fun onServerStopping(@Suppress("UNUSED_PARAMETER") event: FMLServerStoppingEvent) {
        BetterVisualsConfig.saveSettings()
    }

    @SubscribeEvent
    fun onTick(@Suppress("UNUSED_PARAMETER") event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        tickCounter++
        if (configDirty && tickCounter - dirtySinceTick >= 60) {
            configDirty = false
            BetterVisualsConfig.saveSettings()
        }
    }
}

private object ShutdownSave {
    @Subscribe
    fun onPreShutdown(@Suppress("UNUSED_PARAMETER") event: PreShutdownEvent) {
        BetterVisualsConfig.saveSettings()
    }
}

private object BvCommand : CommandBase() {
    override fun getCommandName() = "bettervisuals"
    override fun getCommandAliases() = listOf("bv")
    override fun getRequiredPermissionLevel() = 0
    override fun getCommandUsage(sender: ICommandSender?) = "/bettervisuals [save|load|debug]"
    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        val player = net.minecraft.client.Minecraft.getMinecraft().thePlayer ?: return
        when (args?.firstOrNull()?.lowercase()) {
            "save" -> {
                val ok = BetterVisualsConfig.saveSettings()
                val file = ConfigPersistence.configFile()
                if (ok) {
                    player.addChatMessage(ChatComponentText("\u00a7a[BV] Saved to ${file.absolutePath}"))
                } else {
                    player.addChatMessage(ChatComponentText("\u00a7c[BV] Save failed! Check logs."))
                }
            }
            "load" -> {
                val count = BetterVisualsConfig.loadSettings()
                player.addChatMessage(ChatComponentText("\u00a7a[BV] Loaded $count fields."))
            }
            "debug" -> {
                val file = ConfigPersistence.configFile()
                val fields = ConfigPersistence.fieldCount(BetterVisualsConfig)
                player.addChatMessage(ChatComponentText("\u00a7e[BV] Config: ${file.absolutePath}"))
                player.addChatMessage(ChatComponentText("\u00a7e[BV] Exists: ${file.exists()}, Size: ${if (file.exists()) file.length() else 0} bytes"))
                player.addChatMessage(ChatComponentText("\u00a7e[BV] Fields: $fields"))
            }
            else -> BetterVisualsConfig.openGui()
        }
    }
}
