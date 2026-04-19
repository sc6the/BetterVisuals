package org.polyfrost.bettervisuals.features

import cc.polyfrost.oneconfig.utils.Multithreading
import cc.polyfrost.oneconfig.utils.NetworkUtils
import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent
import org.polyfrost.bettervisuals.config.BetterVisualsConfig

object ServerManager {

    private val serverList = hashMapOf<String, String>()

    fun initialize() {
        Multithreading.runAsync {
            try {
                val json = JsonParser().parse(
                    NetworkUtils.getString("https://servermappings.lunarclientcdn.com/servers.json")
                ).asJsonArray
                for (element in json) {
                    val serverJson = element.asJsonObject
                    val addresses = serverJson["addresses"].asJsonArray
                    for (address in addresses) {
                        serverList[address.asString] = serverJson["name"].asString
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun getNameOfServer(ip: String?): String? {
        if (ip == null) return null
        for (server in serverList) {
            if (ip.endsWith(server.key, ignoreCase = true)) {
                return server.value
            }
        }
        return ip
    }

    @SubscribeEvent
    fun onServerJoined(event: ClientConnectedToServerEvent) {
        if (!event.isLocal) {
            BetterVisualsConfig.lastServerIP =
                Minecraft.getMinecraft().currentServerData?.serverIP ?: ""
            BetterVisualsConfig.save()
        }
    }
}
