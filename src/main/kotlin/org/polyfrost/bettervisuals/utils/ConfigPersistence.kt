package org.polyfrost.bettervisuals.utils

import cc.polyfrost.oneconfig.config.core.OneColor
import com.google.gson.*
import net.minecraftforge.fml.common.Loader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object ConfigPersistence {

    private val logger = LogManager.getLogger("BetterVisuals")

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun configFile(): File {
        val configDir = Loader.instance().configDir
        return File(configDir, "bettervisuals-settings.json")
    }

    private fun getConfigFields(target: Any): List<Field> {
        return target.javaClass.declaredFields.filter { field ->
            !Modifier.isTransient(field.modifiers) &&
            !Modifier.isFinal(field.modifiers) &&
            (field.type == Boolean::class.javaPrimitiveType ||
             field.type == Int::class.javaPrimitiveType ||
             field.type == Float::class.javaPrimitiveType ||
             field.type == Double::class.javaPrimitiveType ||
             field.type == String::class.java ||
             OneColor::class.java.isAssignableFrom(field.type))
        }.onEach { it.isAccessible = true }
    }

    fun fieldCount(target: Any): Int = getConfigFields(target).size

    fun saveConfig(target: Any): Boolean {
        return try {
            val fields = getConfigFields(target)
            val json = JsonObject()
            for (field in fields) {
                val obj = if (Modifier.isStatic(field.modifiers)) null else target
                val value = field.get(obj) ?: continue
                when (value) {
                    is Boolean -> json.addProperty(field.name, value)
                    is Int -> json.addProperty(field.name, value)
                    is Float -> json.addProperty(field.name, value)
                    is Double -> json.addProperty(field.name, value)
                    is String -> json.addProperty(field.name, value)
                    is OneColor -> json.addProperty(field.name, value.rgb)
                }
            }
            val file = configFile()
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(json))
            logger.info("Saved {} fields to {}", fields.size, file.absolutePath)
            true
        } catch (e: Exception) {
            logger.error("Failed to save config", e)
            false
        }
    }

    fun loadConfig(target: Any): Int {
        return try {
            val file = configFile()
            if (!file.exists()) {
                logger.info("Config file not found: {}", file.absolutePath)
                return 0
            }
            val text = file.readText()
            if (text.isBlank()) return 0
            val json = JsonParser().parse(text).asJsonObject
            val fields = getConfigFields(target)
            var loaded = 0

            for (field in fields) {
                if (!json.has(field.name)) continue
                val element = json.get(field.name)
                val obj = if (Modifier.isStatic(field.modifiers)) null else target
                try {
                    when (field.type) {
                        Boolean::class.javaPrimitiveType -> { field.setBoolean(obj, element.asBoolean); loaded++ }
                        Int::class.javaPrimitiveType -> { field.setInt(obj, element.asInt); loaded++ }
                        Float::class.javaPrimitiveType -> { field.setFloat(obj, element.asFloat); loaded++ }
                        Double::class.javaPrimitiveType -> { field.setDouble(obj, element.asDouble); loaded++ }
                        String::class.java -> { field.set(obj, element.asString); loaded++ }
                        else -> {
                            if (OneColor::class.java.isAssignableFrom(field.type)) {
                                field.set(obj, OneColor(element.asInt))
                                loaded++
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to load field {}: {}", field.name, e.message)
                }
            }
            logger.info("Loaded {}/{} fields from {}", loaded, fields.size, file.absolutePath)
            loaded
        } catch (e: Exception) {
            logger.error("Failed to load config", e)
            0
        }
    }
}
